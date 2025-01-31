package com.woowacourse.zzimkkong.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.woowacourse.zzimkkong.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;

class PresetTest {
    private final Setting setting = Setting.builder()
            .availableStartTime(BE_AVAILABLE_START_TIME)
            .availableEndTime(BE_AVAILABLE_END_TIME)
            .reservationTimeUnit(BE_RESERVATION_TIME_UNIT)
            .reservationMinimumTimeUnit(BE_RESERVATION_MINIMUM_TIME_UNIT)
            .reservationMaximumTimeUnit(BE_RESERVATION_MAXIMUM_TIME_UNIT)
            .reservationEnable(BE_RESERVATION_ENABLE)
            .enabledDayOfWeek(BE_ENABLED_DAY_OF_WEEK)
            .build();

    @Test
    @DisplayName("preset이 생성되면 member의 preset을 추가한다.")
    void addPreset() {
        //given
        Member member = new Member(1L, EMAIL, PW, ORGANIZATION);

        //when
        assertThat(member.getPresets().size()).isZero();
        new Preset(1L, PRESET_NAME1, setting, member);

        //then
        assertThat(member.getPresets().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("preset의 member가 동일하다면 false, 그렇지 않다면 true를 반환한다.")
    void isNotOwnedBy() {
        //given
        Member member = new Member(EMAIL, PW, ORGANIZATION);
        Member another = new Member("another@email.com", PW, ORGANIZATION);
        Preset preset = new Preset(PRESET_NAME1, setting, member);

        //when, then
        assertThat(preset.isNotOwnedBy(member)).isTrue();
        assertThat(preset.isNotOwnedBy(another)).isFalse();
    }

    @Test
    @DisplayName("preset의 id가 동일하다면 true, 그렇지 않다면 false를 반환한다.")
    void hasSameId() {
        //given
        Member member = new Member(EMAIL, PW, ORGANIZATION);
        long presetId = 1L;
        Preset preset = new Preset(presetId, PRESET_NAME1, setting, member);

        //when, then
        assertThat(preset.hasSameId(presetId)).isTrue();
        assertThat(preset.hasSameId(presetId + 1)).isFalse();
    }
}
