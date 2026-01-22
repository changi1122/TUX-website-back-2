package kr.ac.cbnu.tux.domain.admin.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "배너(banner)", description = "배너 관리 API")
public interface BannerControllerDocs {

    @Operation(method = "POST", summary = "배너 추가", description = "새로운 배너를 추가합니다.")
    void uploadBanner(MultipartFile file) throws IOException;

    @Operation(method = "DELETE", summary = "배너 삭제", description = "배너를 삭제합니다.")
    void removeBanner(@PathVariable String filename) throws IOException;

    @Operation(method = "GET", summary = "배너 목록 조회", description = "배너 목록을 조회합니다. (파일 이름 순)")
    List<String> listAllBanners() throws IOException;

    @Operation(method = "GET", summary = "배너 이미지 다운로드", description = "배너 이미지 파일을 다운로드합니다.")
    ResponseEntity<Resource> downloadBanner(@PathVariable String filename) throws IOException;
}
