package kr.ac.cbnu.tux.domain.user.service;

import kr.ac.cbnu.tux.domain.user.dto.request.SignupRequest;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.domain.user.enums.UserRole;
import kr.ac.cbnu.tux.domain.user.factory.UserFactory;
import kr.ac.cbnu.tux.domain.user.repository.UserRepository;
import kr.ac.cbnu.tux.utility.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("새로운 회원을 생성한다")
    void createUser() {
        // given
        SignupRequest signupRequest = UserFactory.createSignupRequest("test1", "password1", "a@a.com");
        OffsetDateTime now = OffsetDateTime.now();

        // when
        userService.createUser(signupRequest, now);

        // then
        User createdUser = userRepository.findUserByUsername("test1").orElseThrow();
        assertThat(createdUser)
                .extracting("username", "role", "email", "isLocked", "isBanned", "isDeleted", "createdDate")
                .contains("test1", UserRole.GUEST, "a@a.com", false, false, false, now);
        assertThat(createdUser.getPassword()).isNotEqualTo(signupRequest.getPassword());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "short1!",        // 1. 8자 미만 (7자)
            "onlyletters",    // 2. 숫자 없음
            "12345678",       // 3. 영문자 없음
            "        ",       // 4. 공백만 있음
            "special!!!!",    // 5. 숫자 없음
            "한글비밀번호12"    // 6. 허용되지 않은 문자셋
    })
    @DisplayName("비밀번호 규칙을 어기면 예외가 발생한다")
    void createUser_invalidPassword(String password) {
        // given
        SignupRequest signupRequest = UserFactory.createSignupRequest("test1", password, "a@a.com");
        OffsetDateTime now = OffsetDateTime.now();

        // when then
        assertThatThrownBy(() -> userService.createUser(signupRequest, now))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("password rule not matched");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "test1",
            "anonymousUser"
    })
    @DisplayName("중복된 username이 존재하면 예외가 발생한다.")
    void createUser_duplicateUsername(String username) {
        // given
        userService.createUser(UserFactory.createSignupRequest(
                "test1", "password1", "a@a.com"),
                OffsetDateTime.now()
        );

        SignupRequest signupRequest = UserFactory.createSignupRequest(
                username, "password1", "a@a.com"
        );
        OffsetDateTime now = OffsetDateTime.now();

        // when then
        assertThatThrownBy(() -> userService.createUser(signupRequest, now))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("username is not unique");
    }
}