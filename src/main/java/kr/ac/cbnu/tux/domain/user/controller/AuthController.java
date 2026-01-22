package kr.ac.cbnu.tux.domain.user.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.cbnu.tux.domain.user.controller.docs.AuthControllerDocs;
import kr.ac.cbnu.tux.domain.user.dto.request.LoginRequest;
import kr.ac.cbnu.tux.domain.user.dto.response.UserResponse;
import kr.ac.cbnu.tux.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static kr.ac.cbnu.tux.global.utility.CookieUtils.TOKEN_AGE;
import static kr.ac.cbnu.tux.global.utility.CookieUtils.createTokenCookie;

@RequiredArgsConstructor
@Controller
public class AuthController implements AuthControllerDocs {

    private final UserService userService;

    @PostMapping("/api/auth")
    @ResponseStatus(code = HttpStatus.OK)
    @ResponseBody
    public UserResponse login(HttpServletResponse response, @RequestBody LoginRequest loginRequest) {
        try {
            UserResponse userAndToken = userService.tryLogin(loginRequest);
            Cookie tokenCookie = createTokenCookie(userAndToken.getToken().getToken(), TOKEN_AGE);
            response.addCookie(tokenCookie);
            return userAndToken;

        } catch (Exception e) {
            Cookie emptyCookie = createTokenCookie(null, 0);
            response.addCookie(emptyCookie);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/api/auth")
    @ResponseStatus(code = HttpStatus.OK)
    public void logout(HttpServletResponse response) {
        Cookie tokenCookie = createTokenCookie(null, 0);
        response.addCookie(tokenCookie);
    }

}
