package kr.ac.cbnu.tux.domain.community.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.cbnu.tux.domain.community.dto.request.CommunityRequest;
import kr.ac.cbnu.tux.domain.community.enums.CommunityPostType;
import kr.ac.cbnu.tux.domain.user.dto.request.SignupRequest;
import kr.ac.cbnu.tux.domain.user.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "커뮤니티(community)", description = "커뮤니티 API")
public interface CommunityControllerDocs {

    @Operation(method = "POST", summary = "글 작성", description = "파일 첨부 없이 새로운 글을 작성한다.")
    void createPost(@RequestParam CommunityPostType type, @Validated @RequestBody CommunityRequest request,
                    @AuthenticationPrincipal User user);
}
