package kr.ac.cbnu.tux.domain.user.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.cbnu.tux.domain.user.controller.docs.AuthControllerDocs;
import kr.ac.cbnu.tux.domain.user.dto.request.LoginRequest;
import kr.ac.cbnu.tux.domain.user.dto.response.LoginResponse;
import kr.ac.cbnu.tux.domain.user.dto.response.UserResponse;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.domain.user.service.UserService;
import kr.ac.cbnu.tux.global.utility.CookieUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Controller
public class AuthController implements AuthControllerDocs {

    private final UserService userService;

    @PostMapping("/api/auth")
    @ResponseStatus(code = HttpStatus.OK)
    @ResponseBody
    public LoginResponse login(HttpServletResponse response, @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse result = userService.tryLogin(loginRequest);

            // 액세스 토큰 쿠키 설정
            response.addCookie(CookieUtils.createAccessTokenCookie(result.getAccessToken().getToken()));
            // 리프레시 토큰 쿠키 설정
            response.addCookie(CookieUtils.createRefreshTokenCookie(result.getRefreshToken().getToken()));

            return result;

        } catch (Exception e) {
            // 실패 시 쿠키 삭제
            response.addCookie(CookieUtils.deleteAccessTokenCookie());
            response.addCookie(CookieUtils.deleteRefreshTokenCookie());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/api/auth")
    @ResponseStatus(code = HttpStatus.OK)
    public void logout(HttpServletRequest request, HttpServletResponse response, @AuthenticationPrincipal User user) {
        // DB에서 리프레시 토큰 삭제
        String refreshToken = CookieUtils.getCookieValue(request, CookieUtils.REFRESH_TOKEN_KEY);
        if (user != null && refreshToken != null) {
            userService.logout(refreshToken);
        }

        // 쿠키 삭제
        response.addCookie(CookieUtils.deleteAccessTokenCookie());
        response.addCookie(CookieUtils.deleteRefreshTokenCookie());
    }

    @GetMapping("/api/auth")
    @ResponseBody
    public UserResponse getCurrentUser(@AuthenticationPrincipal User user) {
        return UserResponse.of(user);
    }
}
