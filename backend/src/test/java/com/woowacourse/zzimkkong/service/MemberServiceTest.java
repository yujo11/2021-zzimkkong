package com.woowacourse.zzimkkong.service;

import com.woowacourse.zzimkkong.domain.Member;
import com.woowacourse.zzimkkong.domain.OauthProvider;
import com.woowacourse.zzimkkong.domain.oauth.OauthUserInfo;
import com.woowacourse.zzimkkong.dto.member.MemberSaveRequest;
import com.woowacourse.zzimkkong.dto.member.MemberSaveResponse;
import com.woowacourse.zzimkkong.dto.member.MemberUpdateRequest;
import com.woowacourse.zzimkkong.dto.member.oauth.OauthMemberSaveRequest;
import com.woowacourse.zzimkkong.dto.member.oauth.OauthReadyResponse;
import com.woowacourse.zzimkkong.exception.member.DuplicateEmailException;
import com.woowacourse.zzimkkong.exception.member.ReservationExistsOnMemberException;
import com.woowacourse.zzimkkong.infrastructure.oauth.OauthHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static com.woowacourse.zzimkkong.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class MemberServiceTest extends ServiceTest {
    @Autowired
    private MemberService memberService;

    @MockBean
    private OauthHandler oauthHandler;

    @Test
    @DisplayName("회원이 올바르게 저장을 요청하면 저장한다.")
    void saveMember() {
        //given
        MemberSaveRequest memberSaveRequest = new MemberSaveRequest(EMAIL, PW, ORGANIZATION);
        Member member = new Member(
                memberSaveRequest.getEmail(),
                memberSaveRequest.getPassword(),
                memberSaveRequest.getOrganization()
        );

        //when
        Member savedMember = new Member(
                1L,
                member.getEmail(),
                member.getPassword(),
                member.getOrganization());

        given(members.save(any(Member.class)))
                .willReturn(savedMember);

        //then
        MemberSaveResponse memberSaveResponse = MemberSaveResponse.from(savedMember);
        assertThat(memberService.saveMember(memberSaveRequest)).usingRecursiveComparison()
                .isEqualTo(memberSaveResponse);
    }

    @Test
    @DisplayName("회원이 중복된 이메일로 저장을 요청하면 오류가 발생한다.")
    void saveMemberException() {
        //given
        MemberSaveRequest memberSaveRequest = new MemberSaveRequest(EMAIL, PW, ORGANIZATION);

        //when
        given(members.existsByEmail(anyString()))
                .willReturn(true);

        //then
        assertThatThrownBy(() -> memberService.saveMember(memberSaveRequest))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @ParameterizedTest
    @EnumSource(OauthProvider.class)
    @DisplayName("Oauth를 통해 얻을 수 없는 정보를 응답하며 회원가입 과정을 진행한다.")
    void getUserInfoFromOauth(OauthProvider oauthProvider) {
        //given
        OauthUserInfo mockOauthUserInfo = mock(OauthUserInfo.class);
        given(oauthHandler.getUserInfoFromCode(any(OauthProvider.class), anyString()))
                .willReturn(mockOauthUserInfo);
        given(mockOauthUserInfo.getEmail())
                .willReturn(EMAIL);
        given(members.existsByEmail(EMAIL))
                .willReturn(false);

        //when
        OauthReadyResponse actual = memberService.getUserInfoFromOauth(oauthProvider, "code-example");
        OauthReadyResponse expected = OauthReadyResponse.of(EMAIL, oauthProvider);

        //then
        assertThat(actual).usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @ParameterizedTest
    @EnumSource(OauthProvider.class)
    @DisplayName("이미 존재하는 이메일로 oauth 정보를 가져오면 에러가 발생한다.")
    void getUserInfoFromOauthException(OauthProvider oauthProvider) {
        //given
        OauthUserInfo mockOauthUserInfo = mock(OauthUserInfo.class);
        given(oauthHandler.getUserInfoFromCode(any(OauthProvider.class), anyString()))
                .willReturn(mockOauthUserInfo);
        given(mockOauthUserInfo.getEmail())
                .willReturn(EMAIL);
        given(members.existsByEmail(EMAIL))
                .willReturn(true);

        //when, then
        assertThatThrownBy(() -> memberService.getUserInfoFromOauth(oauthProvider, "code-example"))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GOOGLE", "GITHUB"})
    @DisplayName("소셜 로그인을 이용해 회원가입한다.")
    void saveMemberByOauth(String oauth) {
        //given
        OauthMemberSaveRequest oauthMemberSaveRequest = new OauthMemberSaveRequest(EMAIL, ORGANIZATION, oauth);
        Member member = new Member(
                oauthMemberSaveRequest.getEmail(),
                oauthMemberSaveRequest.getOrganization(),
                oauthMemberSaveRequest.getOauthProvider()
        );
        given(members.existsByEmail(anyString()))
                .willReturn(false);

        //when
        Member savedMember = new Member(
                1L,
                member.getEmail(),
                member.getOrganization(),
                member.getOauthProvider());
        given(members.save(any(Member.class)))
                .willReturn(savedMember);

        //then
        MemberSaveResponse memberSaveResponse = MemberSaveResponse.from(savedMember);
        assertThat(memberService.saveMemberByOauth(oauthMemberSaveRequest)).usingRecursiveComparison()
                .isEqualTo(memberSaveResponse);
    }


    @ParameterizedTest
    @ValueSource(strings = {"GOOGLE", "GITHUB"})
    @DisplayName("이미 존재하는 이메일로 소셜 로그인을 이용해 회원가입하면 에러가 발생한다.")
    void saveMemberByOauthException(String oauth) {
        //given
        OauthMemberSaveRequest oauthMemberSaveRequest = new OauthMemberSaveRequest(EMAIL, ORGANIZATION, oauth);

        //when
        given(members.existsByEmail(anyString()))
                .willReturn(true);

        //then
        assertThatThrownBy(() -> memberService.saveMemberByOauth(oauthMemberSaveRequest))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    @DisplayName("회원은 자신의 정보를 수정할 수 있다.")
    void updateMember() {
        // given
        Member member = new Member(EMAIL, PW, ORGANIZATION);
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest("woowabros");

        given(members.findByEmail(any(String.class)))
                .willReturn(Optional.of(member));

        // when
        memberService.updateMember(member, memberUpdateRequest);

        assertThat(members.findByEmail(EMAIL).orElseThrow().getOrganization()).isEqualTo("woowabros");
    }

    @Test
    @DisplayName("회원을 삭제할 수 있다.")
    void deleteMember() {
        // given
        Member member = new Member(1L, EMAIL, PW, ORGANIZATION);
        given(reservations.existsReservationsByMemberFromToday(any(Member.class)))
                .willReturn(false);

        // when, then
        memberService.deleteMember(member);
    }

    @Test
    @DisplayName("회원이 소유한 공간에 예약이 있다면 탈퇴할 수 없다.")
    void deleteMemberFailWhenAnyReservationsExists() {
        // given
        Member member = new Member(1L, EMAIL, PW, ORGANIZATION);
        given(reservations.existsReservationsByMemberFromToday(any(Member.class)))
                .willReturn(true);

        // when, then
        assertThatThrownBy(() -> memberService.deleteMember(member))
                .isInstanceOf(ReservationExistsOnMemberException.class);
    }
}
