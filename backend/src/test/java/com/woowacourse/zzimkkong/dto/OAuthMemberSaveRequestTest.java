package com.woowacourse.zzimkkong.dto;

import com.woowacourse.zzimkkong.dto.member.LoginRequest;
import com.woowacourse.zzimkkong.dto.member.MemberSaveRequest;
import com.woowacourse.zzimkkong.dto.member.OAuthMemberSaveRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;

import static com.woowacourse.zzimkkong.dto.ValidatorMessage.*;
import static org.assertj.core.api.Assertions.assertThat;

class OAuthMemberSaveRequestTest extends RequestTest {
    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("oauth 회원가입 이메일에 빈 문자열이 들어오면 처리한다.")
    void blankEmail(String email) {
        OAuthMemberSaveRequest oAuthMemberSaveRequest = new OAuthMemberSaveRequest(email, "ORGANIZTION", "GOOGLE");

        assertThat(getConstraintViolations(oAuthMemberSaveRequest).stream()
                .anyMatch(violation -> violation.getMessage().equals(EMPTY_MESSAGE)))
                .isTrue();
    }

    @ParameterizedTest
    @CsvSource(value = {"email:true", "email@email:false", "email@email.com:false"}, delimiter = ':')
    @DisplayName("oauth 회원가입 이메일에 옳지 않은 이메일 형식의 문자열이 들어오면 처리한다.")
    void invalidEmail(String email, boolean flag) {
        OAuthMemberSaveRequest oAuthMemberSaveRequest = new OAuthMemberSaveRequest(email, "ORGANIZTION", "GOOGLE");

        assertThat(getConstraintViolations(oAuthMemberSaveRequest).stream()
                .anyMatch(violation -> violation.getMessage().equals(EMAIL_MESSAGE)))
                .isEqualTo(flag);
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("회원가입 조직명에 빈 문자열이 들어오면 처리한다.")
    void blankOrganization(String organization) {
        OAuthMemberSaveRequest oAuthMemberSaveRequest = new OAuthMemberSaveRequest("email@email.com", organization, "GOOGLE");

        assertThat(getConstraintViolations(oAuthMemberSaveRequest).stream()
                .anyMatch(violation -> violation.getMessage().equals(EMPTY_MESSAGE)))
                .isTrue();
    }

    @ParameterizedTest
    @CsvSource(value = {"hihellomorethantwenty:true", "한글조직:false", "hihello:false", "안 녕 하 세 요:false", "ㄱㄴ 힣 ㄷㄹ:false"}, delimiter = ':')
    @DisplayName("회원가입 조직명에 옳지 않은 형식의 문자열이 들어오면 처리한다.")
    void invalidOrganization(String organization, boolean flag) {
        OAuthMemberSaveRequest oAuthMemberSaveRequest = new OAuthMemberSaveRequest("email@email.com", organization, "GOOGLE");

        assertThat(getConstraintViolations(oAuthMemberSaveRequest).stream()
                .anyMatch(violation -> violation.getMessage().equals(ORGANIZATION_MESSAGE)))
                .isEqualTo(flag);
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("회원가입한 oauth 제공사에 빈 문자열이 들어오면 처리한다.")
    void blankOauthProvider(String oauthProvider) {
        OAuthMemberSaveRequest oAuthMemberSaveRequest = new OAuthMemberSaveRequest("email@email.com", "organization", oauthProvider);

        assertThat(getConstraintViolations(oAuthMemberSaveRequest).stream()
                .anyMatch(violation -> violation.getMessage().equals(EMPTY_MESSAGE)))
                .isTrue();
    }
}
