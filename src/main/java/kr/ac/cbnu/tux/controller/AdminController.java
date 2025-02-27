package kr.ac.cbnu.tux.controller;

import kr.ac.cbnu.tux.domain.User;
import kr.ac.cbnu.tux.dto.UserDTO;
import kr.ac.cbnu.tux.enums.UserRole;
import kr.ac.cbnu.tux.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }


    /* 최고관리자 전용 */
    @GetMapping("/api/admin/user/waiting")
    @ResponseBody
    public List<UserDTO> listAllGuest() {
        List<User> found = userService.listAllWaitingUser();
        return found.stream().map(user -> UserDTO.build(user)).toList();
    }

    @GetMapping("/api/admin/user/member")
    @ResponseBody
    public List<UserDTO> listAllMemberNotGuest() {
        List<User> found = userService.listAllUserNotGuest();
        return found.stream().map(user -> UserDTO.build(user)).toList();
    }

    @PostMapping("/api/admin/user/{id}/role/{role}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void changeUserRole(@PathVariable("id") Long id, @PathVariable("role") String role) {
        userService.changeUserRole(id, UserRole.fromString(role));
    }

    @PutMapping("/api/admin/user/{id}/password/{password}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void setTemporalPassword(@PathVariable("id") Long id, @PathVariable("password") String password) {
        userService.setTemporalPassword(id, password);
    }

    @DeleteMapping("/api/admin/user/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void hardDelete(@PathVariable("id") Long id) {
        userService.hardDelete(id);
    }

    @DeleteMapping("/api/admin/user/{id}/ban")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void ban(@PathVariable("id") Long id) {
        userService.ban(id);
    }
}
