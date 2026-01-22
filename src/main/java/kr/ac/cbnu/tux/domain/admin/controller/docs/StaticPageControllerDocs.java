package kr.ac.cbnu.tux.domain.admin.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.cbnu.tux.domain.admin.entity.StaticPage;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "정적 페이지(staticpage)", description = "정적 페이지 API")
public interface StaticPageControllerDocs {

    @Operation(method = "POST", summary = "페이지 추가/수정", description = "연혁, 연락처 등 정적인 페이지를 저장합니다.")
    void createAndUpdate(@PathVariable String name, @Validated @RequestBody StaticPage updated);

    @Operation(method = "GET", summary = "페이지 조회", description = "연혁, 연락처 등 정적인 페이지를 조회합니다.")
    StaticPage read(@PathVariable String name);
}
