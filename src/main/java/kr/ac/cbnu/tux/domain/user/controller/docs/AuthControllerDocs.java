package kr.ac.cbnu.tux.domain.user.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.cbnu.tux.domain.user.dto.request.LoginRequest;
import kr.ac.cbnu.tux.domain.user.dto.response.UserResponse;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "인증(auth)", description = "인증 API")
public interface AuthControllerDocs {

    @Operation(method = "POST", summary = "로그인", description = "로그인을 통해 토큰을 발급합니다.")
    UserResponse login(HttpServletResponse response, @RequestBody LoginRequest loginRequest);

    @Operation(method = "DELETE", summary = "로그아웃", description = "로그아웃하여 토큰이 저장된 쿠키를 삭제합니다.")
    void logout(HttpServletResponse response);
}
