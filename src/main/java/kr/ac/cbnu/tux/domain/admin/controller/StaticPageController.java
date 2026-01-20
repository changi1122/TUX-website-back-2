package kr.ac.cbnu.tux.domain.admin.controller;

import kr.ac.cbnu.tux.domain.admin.controller.docs.StaticPageControllerDocs;
import kr.ac.cbnu.tux.domain.admin.entity.StaticPage;
import kr.ac.cbnu.tux.domain.admin.service.StaticPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

// 연혁, 연락처 등 정적인 페이지를 저장하기 위한 컨트롤러

// 프론트엔드 코드 수정 없이 내용 수정이 가능하게 DB에 저장하도록 개선함 - 2024년 1월 11일

@RequiredArgsConstructor
@Controller
public class StaticPageController implements StaticPageControllerDocs {

    private final StaticPageService staticPageService;

    @PostMapping("/api/admin/staticpage/{name}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void createAndUpdate(@PathVariable String name, @Validated @RequestBody StaticPage updated) {
        staticPageService.createAndUpdate(name, updated);
    }

    @GetMapping("/api/staticpage/{name}")
    @ResponseBody
    public StaticPage read(@PathVariable String name) {
        return staticPageService.read(name);
    }
}
