package kr.ac.cbnu.tux.domain.user.factory;

import kr.ac.cbnu.tux.domain.user.dto.request.SignupRequest;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.domain.user.enums.UserRole;

import java.time.OffsetDateTime;

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

    public static User createTestUser(String username, UserRole role) {
        return User.builder()
                .username(username)
                .nickname("")
                .password("")
                .role(role)
                .email("")
                .department("")
                .studentNumber("")
                .phoneNumber("")
                .isLocked(false)
                .isBanned(false)
                .isDeleted(false)
                .createdDate(OffsetDateTime.now())
                .build();
    }
}
