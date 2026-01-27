package kr.ac.cbnu.tux.domain.referenceroom.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.cbnu.tux.domain.referenceroom.dto.request.ReferenceRoomRequest;
import kr.ac.cbnu.tux.domain.referenceroom.enums.ReferenceRoomPostType;
import kr.ac.cbnu.tux.domain.user.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "자료실(referenceroom)", description = "자료실 API")
public interface ReferenceRoomControllerDocs {

    @Operation(method = "POST", summary = "글 작성", description = "파일 첨부 없이 새로운 글을 작성한다.")
    void createData(@RequestParam ReferenceRoomPostType type, @Validated @RequestBody ReferenceRoomRequest request,
                    @AuthenticationPrincipal User user);

    @Operation(method = "POST", summary = "최초 파일 업로드", description = "글쓰기 도중 파일 업로드시 임시로 글 생성 후 파일을 저장한다.")
    Long uploadFileBeforeCreateData(@RequestParam ReferenceRoomPostType type,
                                    @RequestParam("file") MultipartFile file, @AuthenticationPrincipal User user);

    @Operation(method = "POST", summary = "(글 생성 이후) 파일 업로드", description = "글쓰기 이후 파일 업로드시 파일을 저장한다.")
    void uploadFileAfterCreateData(@PathVariable Long id, @RequestParam("file") MultipartFile file,
                                   @AuthenticationPrincipal User user);

    @Operation(method = "POST", summary = "(글 생성 이후) 글 작성", description = "임시로 생성된 글의 내용을 작성한다.")
    void updateTemporalData(@PathVariable Long id, ReferenceRoomPostType type,
                            @Validated @RequestBody ReferenceRoomRequest request, @AuthenticationPrincipal User user);
}
