package kr.ac.cbnu.tux.controller;

import kr.ac.cbnu.tux.domain.ReferenceRoom;
import kr.ac.cbnu.tux.dto.GalleryListDTO;
import kr.ac.cbnu.tux.enums.ReferenceRoomPostType;
import kr.ac.cbnu.tux.service.ReferenceRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GalleryController {

    private final ReferenceRoomService referenceRoomService;

    @Autowired
    public GalleryController(ReferenceRoomService referenceRoomService) {
        this.referenceRoomService = referenceRoomService;
    }


    @GetMapping("/api/gallery/list")
    @ResponseBody
    public Page<GalleryListDTO> list(@RequestParam(name = "query", defaultValue = "") String query, Pageable pageable) {
        Page<ReferenceRoom> found;
        if (StringUtils.hasText(query)) {
            found = referenceRoomService.searchListByCategory(query, pageable, ReferenceRoomPostType.GALLERY);
        } else {
            found = referenceRoomService.listByCategory(pageable, ReferenceRoomPostType.GALLERY);
        }
        return new PageImpl<>(
                found.getContent().stream().map(data -> GalleryListDTO.build(data)).toList(),
                pageable,
                found.getTotalElements()
        );
    }
}
