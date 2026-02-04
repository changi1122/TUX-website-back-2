package kr.ac.cbnu.tux.domain.referenceroom.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.cbnu.tux.domain.referenceroom.dto.response.GalleryListResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "갤러리(gallery)", description = "갤러리 API")
public interface GalleryControllerDocs {

    @Operation(method = "GET", summary = "갤러리 목록 조회", description = "갤러리 목록을 조회한다.")
    GalleryListResponse list(@RequestParam(name = "query", defaultValue = "") String query, Pageable pageable);
}
