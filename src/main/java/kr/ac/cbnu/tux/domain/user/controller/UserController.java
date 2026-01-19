package kr.ac.cbnu.tux.domain.user.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.cbnu.tux.domain.user.controller.docs.UserControllerDocs;
import kr.ac.cbnu.tux.domain.user.dto.request.SignupRequest;
import kr.ac.cbnu.tux.domain.user.dto.request.UserDataRequest;
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

import static kr.ac.cbnu.tux.utility.CookieUtils.createTokenCookie;

@RequiredArgsConstructor
@Controller
public class UserController implements UserControllerDocs {

    private final UserService userService;

    @GetMapping("/api/auth")
    @ResponseBody
    public UserResponse getCurrentUser(@AuthenticationPrincipal User user) {
        return UserResponse.build(user);
    }

    @PostMapping("/api/user")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void createUser(@Validated @RequestBody SignupRequest request) {
        userService.createUser(request, OffsetDateTime.now());
    }

    @PutMapping("/api/user/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void updateUser(@PathVariable Long id, @Validated @RequestBody UserDataRequest request,
                       @AuthenticationPrincipal User currentUser) {

        if (!Objects.deepEquals(currentUser.getId(), id)) {
            throw new RuntimeException("user not matched");
        }

        userService.updateUser(id, request);
    }

    @DeleteMapping("/api/user/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void deleteUser(@PathVariable Long id,
                       @AuthenticationPrincipal User currentUser, final HttpServletResponse response) {

        if (!Objects.deepEquals(currentUser.getId(), id)) {
            throw new RuntimeException("user not matched");
        }

        userService.deleteUserSoftly(id, OffsetDateTime.now());

        Cookie tokenCookie = createTokenCookie(null, 0);
        response.addCookie(tokenCookie);
    }

    @GetMapping("/api/user/{id}")
    @ResponseBody
    public UserResponse readUser(@PathVariable Long id, @AuthenticationPrincipal User currentUser) throws AccessDeniedException {
        String currentUsername = currentUser.getUsername();
        User foundUser = userService.readUser(id);

        if ("anonymousUser".equals(currentUsername) || !currentUsername.equals(foundUser.getUsername()) ||
                !canReadUserData(currentUser.getRole())) {
            throw new AccessDeniedException("permission denied");
        }

        return UserResponse.build(foundUser);
    }

    @GetMapping("/api/user/check/username")
    @ResponseBody
    public boolean canUseAsUsername(@RequestParam String username) {
        return userService.canUseAsUsername(username);
    }

    @GetMapping("/api/user/check/nickname")
    @ResponseBody
    public boolean canUseAsNickname(@RequestParam String nickname) {
        return userService.canUseAsNickname(nickname);
    }

    private boolean canReadUserData(UserRole role) {
        return (role == UserRole.MANAGER || role == UserRole.ADMIN);
    }

}
