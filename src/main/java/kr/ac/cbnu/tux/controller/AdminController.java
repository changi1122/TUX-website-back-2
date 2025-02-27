package kr.ac.cbnu.tux.controller;

import jakarta.annotation.PostConstruct;
import kr.ac.cbnu.tux.domain.User;
import kr.ac.cbnu.tux.dto.UserDTO;
import kr.ac.cbnu.tux.enums.UserRole;
import kr.ac.cbnu.tux.service.UserService;
import kr.ac.cbnu.tux.utility.FileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class AdminController {

    private final UserService userService;
    private final FileStore fileStore;

    private static volatile List<String> bannerFileList; // 매번 파일시스템 조회를 막기 위해 배너 파일 목록을 유지

    @PostConstruct
    public void loadBannerFiles() throws IOException {
        updateBannerFileList();
    }

    /* 회원 관리 : 최고관리자 전용 */
    
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


    /* 배너 관리 */
    
    // 배너 파일 추가
    @PostMapping("/api/admin/banner")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void uploadBanner(MultipartFile file) throws IOException {
        if (file != null)
            fileStore.saveBannerImage(file);
        updateBannerFileList();
    }

    // 배너 파일 삭제
    @DeleteMapping("/api/admin/banner/{filename}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void removeBanner(@PathVariable String filename) throws IOException {
        fileStore.deleteBannerImage(filename);
        updateBannerFileList();
    }
    
    // 배너 목록 조회
    @GetMapping("/api/banner")
    @ResponseBody
    public List<String> listAllBanner() throws IOException {
        return bannerFileList;
    }

    // 배너 파일 다운로드
    @GetMapping("/api/banner/{filename}")
    public ResponseEntity<Resource> downloadBanner(@PathVariable String filename) throws IOException {
        UrlResource resource = new UrlResource("file:" + fileStore.getBannerPath(filename));
        if (!resource.exists() || !resource.isReadable()) {
            throw new FileNotFoundException(filename);
        }

        MediaType contentType = MediaType.parseMediaType(Files.probeContentType(Path.of(filename)));

        return ResponseEntity.ok()
                .contentType(contentType)
                .body(resource);
    }

    // 파일시스템에서 배너 파일 목록을 조회하고 bannerFileList 업데이트
    private synchronized void updateBannerFileList() throws IOException {
        bannerFileList = fileStore.listBannerImages();
    }
}
