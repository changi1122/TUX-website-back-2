package kr.ac.cbnu.tux.domain.referenceroom.controller;

import kr.ac.cbnu.tux.domain.referenceroom.controller.docs.GalleryControllerDocs;
import kr.ac.cbnu.tux.domain.referenceroom.dto.response.GalleryListResponse;
import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import kr.ac.cbnu.tux.domain.referenceroom.enums.ReferenceRoomPostType;
import kr.ac.cbnu.tux.domain.referenceroom.service.ReferenceRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequiredArgsConstructor
@Controller
public class GalleryController implements GalleryControllerDocs {

    private final ReferenceRoomService referenceRoomService;

    @GetMapping("/api/gallery/list")
    @ResponseBody
    public GalleryListResponse list(@RequestParam(name = "query", defaultValue = "") String query, Pageable pageable) {
        Page<ReferenceRoom> page;
        if (StringUtils.hasText(query)) {
            page = referenceRoomService.searchListByCategory(query, pageable, ReferenceRoomPostType.GALLERY);
        } else {
            page = referenceRoomService.listByCategory(pageable, ReferenceRoomPostType.GALLERY);
        }

        return GalleryListResponse.of(page, pageable);
    }
}
