package kr.ac.cbnu.tux.domain.user.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.cbnu.tux.domain.user.dto.request.SignupRequest;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.domain.user.dto.request.LoginRequest;
import kr.ac.cbnu.tux.domain.user.dto.response.UserResponse;
import kr.ac.cbnu.tux.domain.user.enums.UserRole;
import kr.ac.cbnu.tux.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.time.OffsetDateTime;
import java.util.Objects;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;


    @PostMapping("/api/auth")
    @ResponseStatus(code = HttpStatus.OK)
    @ResponseBody
    public UserResponse login(final HttpServletRequest request, final HttpServletResponse response,
                              @RequestBody LoginRequest loginRequest) {
        try {
            UserResponse userAndToken = userService.tryLogin(loginRequest);
            Cookie tokenCookie = createTokenCookie(userAndToken.getToken().getToken(), 168 * 60 * 60);
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
    public void logout(final HttpServletRequest request, final HttpServletResponse response) {
        Cookie tokenCookie = createTokenCookie(null, 0);
        response.addCookie(tokenCookie);
    }

    @GetMapping("/api/auth")
    @ResponseBody
    public UserResponse getCurrentUser(@AuthenticationPrincipal User user) {
        return UserResponse.build(user);
    }

    @PostMapping("/api/user")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void createUser(@Validated @RequestBody SignupRequest signupRequest) {
        userService.createUser(signupRequest, OffsetDateTime.now());
    }

    @PutMapping("/api/user/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void update(@PathVariable("id") Long id, @RequestBody User user,
                       @AuthenticationPrincipal User currentUser) throws Exception {

        if (!Objects.deepEquals(currentUser.getId(), id)) {
            throw new Exception("user not matched");
        }

        userService.update(id, user);
    }

    @DeleteMapping("/api/user/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void delete(@PathVariable("id") Long id,
                       @AuthenticationPrincipal User currentUser, final HttpServletResponse response) throws Exception {
        if (!Objects.deepEquals(currentUser.getId(), id)) {
            throw new Exception("user not matched");
        }

        userService.userDelete(id);

        Cookie tokenCookie = createTokenCookie(null, 0);
        response.addCookie(tokenCookie);
    }

    @GetMapping("/api/user/{id}")
    @ResponseBody
    public UserResponse read(@PathVariable("id") Long id, @AuthenticationPrincipal User currentUser) throws AccessDeniedException {
        String currentUsername = currentUser.getUsername();
        String currentRole = currentUser.getRole().name();

        if (currentUsername.equals("anonymousUser") ||
                !currentUsername.equals(userService.read(id).orElseThrow().getUsername()) ||
                !currentRole.equals(UserRole.MANAGER.name()) && !currentRole.equals(UserRole.ADMIN.name())) {
            throw new AccessDeniedException("permission denied");
        }

        return UserResponse.build(userService.read(id).orElseThrow());
    }

    @GetMapping("/api/user/check/username")
    @ResponseBody
    public Boolean canUseAsUsername(@RequestParam("username") String username) {
        return userService.canUseAsUsername(username);
    }

    @GetMapping("/api/user/check/nickname")
    @ResponseBody
    public Boolean canUseAsNickname(@RequestParam("nickname") String nickname) {
        return userService.canUseAsNickname(nickname);
    }


    private Cookie createTokenCookie(String token, int age) {
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(age);
        cookie.setPath("/");
        return cookie;
    }
}
