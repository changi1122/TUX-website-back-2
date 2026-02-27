package kr.ac.cbnu.tux.domain.admin.controller;

import kr.ac.cbnu.tux.domain.admin.controller.docs.BannerControllerDocs;
import kr.ac.cbnu.tux.global.utility.BannerListStore;
import kr.ac.cbnu.tux.global.utility.FileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
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
public class BannerController implements BannerControllerDocs {

    private final FileStore fileStore;
    private final BannerListStore bannerStore; // 매번 파일시스템 조회를 막기 위해 배너 파일 목록을 유지

    @PostMapping("/api/admin/banner")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void uploadBanner(MultipartFile file) throws IOException {
        if (file != null)
            fileStore.saveBannerImage(file);
        bannerStore.updateList();
    }

    @DeleteMapping("/api/admin/banner/{filename}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void removeBanner(@PathVariable String filename) throws IOException {
        fileStore.deleteBannerImage(filename);
        bannerStore.updateList();
    }

    @GetMapping("/api/banner")
    @ResponseBody
    public List<String> listAllBanners() throws IOException {
        return bannerStore.getList();
    }

    @GetMapping("/api/banner/{filename}")
    public ResponseEntity<Resource> downloadBanner(@PathVariable String filename) throws IOException {
        UrlResource resource = new UrlResource("file:" + fileStore.getBannerPath(filename));
        if (!resource.exists() || !resource.isReadable()) {
            throw new FileNotFoundException(filename);
        }

        MediaType contentType = MediaType.parseMediaType(Files.probeContentType(Path.of(filename)));

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=604800")
                .body(resource);
    }
}
