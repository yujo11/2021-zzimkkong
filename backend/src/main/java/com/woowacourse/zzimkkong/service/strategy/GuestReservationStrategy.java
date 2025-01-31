package com.woowacourse.zzimkkong.service.strategy;

import com.woowacourse.zzimkkong.domain.Map;
import com.woowacourse.zzimkkong.domain.Member;
import com.woowacourse.zzimkkong.domain.Reservation;
import com.woowacourse.zzimkkong.dto.slack.SlackResponse;
import com.woowacourse.zzimkkong.exception.reservation.ReservationPasswordException;

public class GuestReservationStrategy implements ReservationStrategy {
    @Override
    public void validateManagerOfMap(final Map map, final Member manager) {
        // guest는 맵의 관리자 확인과정 생략
    }

    @Override
    public void checkCorrectPassword(final Reservation reservation, final String password) {
        if (reservation.isWrongPassword(password)) {
            throw new ReservationPasswordException();
        }
    }

    @Override
    public SlackResponse createSlackResponse(final Reservation reservation) {
        return null;
    }
}
