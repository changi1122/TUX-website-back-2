package kr.ac.cbnu.tux.domain.user.factory;

import kr.ac.cbnu.tux.domain.user.dto.request.SignupRequest;

public class UserFactory {

    public static SignupRequest createSignupRequest(String username, String password, String email) {
        return SignupRequest.builder()
                .username(username)
                .nickname("닉네임")
                .password(password)
                .email(email)
                .department("테스트학과")
                .studentNumber("2000000000")
                .phoneNumber("010-0000-0000")
                .build();
    }
}
