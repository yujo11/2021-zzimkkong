package com.woowacourse.zzimkkong.dto.reservation;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.woowacourse.zzimkkong.domain.Reservation;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.woowacourse.zzimkkong.dto.ValidatorMessage.DATETIME_FORMAT;

@Getter
@NoArgsConstructor
public class ReservationResponse {
    @JsonProperty
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
    private LocalDateTime startDateTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
    private LocalDateTime endDateTime;
    @JsonProperty
    private String name;
    @JsonProperty
    private String description;

    private ReservationResponse(
            final Long id,
            final LocalDateTime startDateTime,
            final LocalDateTime endDateTime,
            final String name,
            final String description) {
        this.id = id;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.name = name;
        this.description = description;
    }

    public static ReservationResponse from(final Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getUserName(),
                reservation.getDescription()
        );
    }
}
