package com.woowacourse.zzimkkong.service;

import com.woowacourse.zzimkkong.domain.Map;
import com.woowacourse.zzimkkong.domain.Member;
import com.woowacourse.zzimkkong.domain.Space;
import com.woowacourse.zzimkkong.dto.map.MapCreateResponse;
import com.woowacourse.zzimkkong.dto.map.MapCreateUpdateRequest;
import com.woowacourse.zzimkkong.dto.map.MapFindAllResponse;
import com.woowacourse.zzimkkong.dto.map.MapFindResponse;
import com.woowacourse.zzimkkong.exception.authorization.NoAuthorityOnMapException;
import com.woowacourse.zzimkkong.exception.map.NoSuchMapException;
import com.woowacourse.zzimkkong.exception.space.ReservationExistOnSpaceException;
import com.woowacourse.zzimkkong.infrastructure.SharingIdGenerator;
import com.woowacourse.zzimkkong.infrastructure.ThumbnailManager;
import com.woowacourse.zzimkkong.repository.MapRepository;
import com.woowacourse.zzimkkong.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@Service
@Transactional
public class MapService {
    private final MapRepository maps;
    private final ReservationRepository reservations;
    private final ThumbnailManager thumbnailManager;
    private final SharingIdGenerator sharingIdGenerator;

    public MapService(
            final MapRepository maps,
            final ReservationRepository reservations,
            final ThumbnailManager thumbnailManager,
            final SharingIdGenerator sharingIdGenerator) {
        this.maps = maps;
        this.reservations = reservations;
        this.thumbnailManager = thumbnailManager;
        this.sharingIdGenerator = sharingIdGenerator;
    }

    public MapCreateResponse saveMap(final MapCreateUpdateRequest mapCreateUpdateRequest, final Member manager) {
        Map saveMap = maps.save(new Map(
                mapCreateUpdateRequest.getMapName(),
                mapCreateUpdateRequest.getMapDrawing(),
                mapCreateUpdateRequest.getMapImageSvg().substring(0, 10),
                manager));

        String thumbnailUrl = thumbnailManager.uploadMapThumbnail(mapCreateUpdateRequest.getMapImageSvg(), saveMap);
        saveMap.updateImageUrl(thumbnailUrl);

        return MapCreateResponse.from(saveMap);
    }

    @Transactional(readOnly = true)
    public MapFindResponse findMap(final Long mapId, final Member manager) {
        Map map = maps.findById(mapId)
                .orElseThrow(NoSuchMapException::new);
        validateManagerOfMap(map, manager);
        return MapFindResponse.of(map, sharingIdGenerator.from(map));
    }

    @Transactional(readOnly = true)
    public MapFindAllResponse findAllMaps(final Member manager) {
        List<Map> findMaps = maps.findAllByMember(manager);
        return findMaps.stream()
                .map(map -> MapFindResponse.of(map, sharingIdGenerator.from(map)))
                .collect(collectingAndThen(toList(), mapFindResponses -> MapFindAllResponse.of(mapFindResponses, manager)));
    }

    public void updateMap(final Long mapId, final MapCreateUpdateRequest mapCreateUpdateRequest,
                          final Member manager) {
        Map map = maps.findById(mapId)
                .orElseThrow(NoSuchMapException::new);
        validateManagerOfMap(map, manager);

        thumbnailManager.uploadMapThumbnail(mapCreateUpdateRequest.getMapImageSvg(), map);

        map.update(
                mapCreateUpdateRequest.getMapName(),
                mapCreateUpdateRequest.getMapDrawing());
    }

    public void deleteMap(final Long mapId, final Member manager) {
        Map map = maps.findById(mapId)
                .orElseThrow(NoSuchMapException::new);
        validateManagerOfMap(map, manager);

        validateExistReservations(map);

        maps.delete(map);

        thumbnailManager.deleteThumbnail(map);
    }

    private void validateExistReservations(final Map map) {
        List<Space> findSpaces = map.getSpaces();

        boolean isExistReservationInAnySpace = findSpaces.stream()
                .anyMatch(space -> reservations.existsBySpaceIdAndEndTimeAfter(space.getId(), LocalDateTime.now()));

        if (isExistReservationInAnySpace) {
            throw new ReservationExistOnSpaceException();
        }
    }

    public static void validateManagerOfMap(final Map map, final Member manager) {
        if (map.isNotOwnedBy(manager)) {
            throw new NoAuthorityOnMapException();
        }
    }

    public MapFindResponse findMapBySharingId(final String sharingMapId) {
        Long mapId = sharingIdGenerator.parseIdFrom(sharingMapId);
        Map map = maps.findById(mapId)
                .orElseThrow(NoSuchMapException::new);
        return MapFindResponse.of(map, sharingIdGenerator.from(map));
    }
}
