package kr.ac.cbnu.tux.domain.user.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.cbnu.tux.domain.user.dto.request.SignupRequest;
import kr.ac.cbnu.tux.domain.user.dto.request.UserDataRequest;
import kr.ac.cbnu.tux.domain.user.dto.response.UserResponse;
import kr.ac.cbnu.tux.domain.user.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.Objects;

@Tag(name = "회원(user)", description = "회원 API")
public interface UserControllerDocs {

    @Operation(method = "POST", summary = "회원가입", description = "새로운 회원을 생성합니다.")
    void createUser(@Validated @RequestBody SignupRequest signupRequest);

    @Operation(method = "PUT", summary = "회원 정보 수정", description = "회원 정보를 수정합니다.")
    void updateUser(@PathVariable Long id, @Validated @RequestBody UserDataRequest request, @AuthenticationPrincipal User currentUser);

    @Operation(method = "GET", summary = "회원 정보 조회", description = "회원 정보를 조회합니다. (MANAGER, ADMIN 권한 보유시 타인 정보 조회 가능)")
    UserResponse readUser(@PathVariable Long id, @AuthenticationPrincipal User currentUser) throws AccessDeniedException;

    @Operation(method = "DELETE", summary = "회원 탈퇴", description = "회원을 탈퇴합니다.")
    void deleteUser(@PathVariable Long id, @AuthenticationPrincipal User currentUser, final HttpServletResponse response);

    @Operation(method = "GET", summary = "아이디 사용 가능 여부 조회", description = "아이디를 사용 가능한지 조회합니다.")
    boolean canUseAsUsername(@RequestParam String username);

    @Operation(method = "GET", summary = "닉네임 사용 가능 여부 조회", description = "닉네임을 사용 가능한지 조회합니다.")
    boolean canUseAsNickname(@RequestParam String nickname);
}
