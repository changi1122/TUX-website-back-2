package kr.ac.cbnu.tux.domain.admin.controller;

import kr.ac.cbnu.tux.domain.admin.controller.docs.AdminControllerDocs;
import kr.ac.cbnu.tux.domain.user.dto.response.UserResponse;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.domain.user.enums.UserRole;
import kr.ac.cbnu.tux.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class AdminController implements AdminControllerDocs {

    private final UserService userService;

    @GetMapping("/api/admin/user/waiting")
    @ResponseBody
    public List<UserResponse> listAllGuest() {
        List<User> found = userService.listAllWaitingUser();
        return found.stream().map(UserResponse::build).toList();
    }

    @GetMapping("/api/admin/user/member")
    @ResponseBody
    public List<UserResponse> listAllMemberNotGuest() {
        List<User> found = userService.listAllUserNotGuest();
        return found.stream().map(UserResponse::build).toList();
    }

    @PostMapping("/api/admin/user/{id}/role/{role}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void changeUserRole(@PathVariable Long id, @PathVariable UserRole role) {
        userService.changeUserRole(id, role);
    }

    @PutMapping("/api/admin/user/{id}/password")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void setTemporalPassword(@PathVariable Long id, @RequestBody String password) {
        userService.setTemporalPassword(id, password);
    }

    @DeleteMapping("/api/admin/user/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void deleteUserHardly(@PathVariable Long id) {
        userService.deleteUserHardly(id);
    }

    @DeleteMapping("/api/admin/user/{id}/ban")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void ban(@PathVariable Long id) {
        userService.ban(id, OffsetDateTime.now());
    }
}
