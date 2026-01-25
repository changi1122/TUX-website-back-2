package kr.ac.cbnu.tux.domain.referenceroom.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.cbnu.tux.domain.community.dto.request.CommunityRequest;
import kr.ac.cbnu.tux.domain.community.enums.CommunityPostType;
import kr.ac.cbnu.tux.domain.referenceroom.dto.request.ReferenceRoomRequest;
import kr.ac.cbnu.tux.domain.referenceroom.enums.ReferenceRoomPostType;
import kr.ac.cbnu.tux.domain.user.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "자료실(referenceroom)", description = "자료실 API")
public interface ReferenceRoomControllerDocs {

    @Operation(method = "POST", summary = "글 작성", description = "파일 첨부 없이 새로운 글을 작성한다.")
    void createData(@RequestParam ReferenceRoomPostType type, @Validated @RequestBody ReferenceRoomRequest request,
                    @AuthenticationPrincipal User user);
}
