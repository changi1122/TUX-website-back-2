package kr.ac.cbnu.tux.domain.user.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.cbnu.tux.domain.user.dto.request.LoginRequest;
import kr.ac.cbnu.tux.domain.user.dto.response.UserResponse;
import kr.ac.cbnu.tux.domain.user.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "인증(auth)", description = "인증 API")
public interface AuthControllerDocs {

    @Operation(method = "POST", summary = "로그인", description = "로그인을 통해 토큰을 발급합니다.")
    UserResponse login(HttpServletResponse response, @RequestBody LoginRequest loginRequest);

    @Operation(method = "DELETE", summary = "로그아웃", description = "로그아웃하여 토큰이 저장된 쿠키를 삭제합니다.")
    void logout(HttpServletResponse response);

    @Operation(method = "GET", summary = "현재 로그인한 회원 정보 조회", description = "로그인된 회원의 정보를 조회합니다.")
    UserResponse getCurrentUser(@AuthenticationPrincipal User user);
}
