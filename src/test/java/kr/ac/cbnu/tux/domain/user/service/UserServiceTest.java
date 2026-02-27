package kr.ac.cbnu.tux.domain.user.service;

import kr.ac.cbnu.tux.domain.user.dto.request.SignupRequest;
import kr.ac.cbnu.tux.domain.user.dto.request.UserDataRequest;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.domain.user.enums.UserRole;
import kr.ac.cbnu.tux.domain.user.exception.UserException;
import kr.ac.cbnu.tux.domain.user.factory.UserFactory;
import kr.ac.cbnu.tux.domain.user.repository.UserRepository;
import kr.ac.cbnu.tux.utility.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.List;

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
                .isInstanceOf(UserException.class)
                .hasMessage("비밀번호 영문자와 숫자를 포함하여 최소 8자 이상이어야 합니다.");
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
                .isInstanceOf(UserException.class)
                .hasMessage("이미 사용 중인 아이디입니다.");
    }

    @Test
    @DisplayName("회원 정보를 수정한다")
    void updateUser() {
        // given
        userService.createUser(UserFactory.createSignupRequest(
                        "test1", "password1", "a@a.com"),
                OffsetDateTime.now()
        );
        User existingUser = userRepository.findUserByUsername("test1").orElseThrow();
        String oldPassword = existingUser.getPassword();

        UserDataRequest request = UserDataRequest.builder()
                .nickname("새닉네임")
                .password("password2")
                .build();

        // when
        userService.updateUser(existingUser.getId(), request);

        // then
        User foundUser = userRepository.findUserByUsername("test1").orElseThrow();
        assertThat(foundUser).extracting("nickname", "email", "department", "studentNumber", "phoneNumber")
                .contains("새닉네임", existingUser.getEmail(), existingUser.getDepartment(), existingUser.getStudentNumber(), existingUser.getPhoneNumber());
        assertThat(foundUser.getPassword()).isNotEqualTo(oldPassword);
    }

    @Test
    @DisplayName("회원을 탈퇴한다")
    void deleteUser() {
        // given
        userService.createUser(UserFactory.createSignupRequest(
                        "test1", "password1", "a@a.com"),
                OffsetDateTime.now()
        );
        User existingUser = userRepository.findUserByUsername("test1").orElseThrow();
        OffsetDateTime now = OffsetDateTime.now();

        // when
        userService.deleteUserSoftly(existingUser.getId(), now);

        // then
        User foundUser = userRepository.findUserByUsername("test1").orElseThrow();
        assertThat(foundUser).extracting("password", "email", "department", "phoneNumber", "isDeleted", "deletedDate")
                .contains("-", "-@-", "-", "-", true, now);
    }

    @Test
    @DisplayName("가입 승인 대기 중(GUEST)인 회원 목록을 조회한다")
    void listAllWaitingUser() {
        // given
        User user1 = UserFactory.createTestUser("guest1", UserRole.GUEST);
        User user2 = UserFactory.createTestUser("user1", UserRole.USER);
        User user3 = UserFactory.createTestUser("manager1", UserRole.MANAGER);
        User user4 = UserFactory.createTestUser("admin1", UserRole.ADMIN);
        userRepository.saveAll(List.of(user1, user2, user3, user4));

        // when
        List<User> waitingUsers = userService.listAllWaitingUser();

        // then
        assertThat(waitingUsers).hasSize(1)
                .extracting("username")
                .contains("guest1");
    }

    @Test
    @DisplayName("가입 승인된 상태의 회원 목록을 조회한다")
    void listAllUserNotGuest() {
        // given
        User user1 = UserFactory.createTestUser("guest1", UserRole.GUEST);
        User user2 = UserFactory.createTestUser("user1", UserRole.USER);
        User user3 = UserFactory.createTestUser("manager1", UserRole.MANAGER);
        User user4 = UserFactory.createTestUser("admin1", UserRole.ADMIN);
        userRepository.saveAll(List.of(user1, user2, user3, user4));

        // when
        List<User> acceptedUsers = userService.listAllUserNotGuest();

        // then
        assertThat(acceptedUsers).hasSize(3)
                .extracting("username")
                .containsExactlyInAnyOrder("user1", "manager1", "admin1");
    }
}