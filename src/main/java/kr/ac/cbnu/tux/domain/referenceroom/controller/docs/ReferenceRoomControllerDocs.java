package kr.ac.cbnu.tux.domain.referenceroom.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.cbnu.tux.domain.referenceroom.dto.request.ReferenceRoomRequest;
import kr.ac.cbnu.tux.domain.referenceroom.dto.request.RfCommentRequest;
import kr.ac.cbnu.tux.domain.referenceroom.dto.response.ReferenceRoomListResponse;
import kr.ac.cbnu.tux.domain.referenceroom.dto.response.ReferenceRoomResponse;
import kr.ac.cbnu.tux.domain.referenceroom.dto.response.RfCommentResponse;
import kr.ac.cbnu.tux.domain.referenceroom.enums.ReferenceRoomPostType;
import kr.ac.cbnu.tux.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @Operation(method = "PUT", summary = "글 수정", description = "글의 내용을 수정한다.")
    void updateData(@PathVariable Long id, ReferenceRoomPostType type,
                    @Validated @RequestBody ReferenceRoomRequest request, @AuthenticationPrincipal User user);

    @Operation(method = "DELETE", summary = "글 삭제", description = "글을 삭제한다.")
    void deleteData(@PathVariable Long id, @AuthenticationPrincipal User user);

    @Operation(method = "GET", summary = "글 조회", description = "글을 조회한다.")
    ReferenceRoomResponse readData(@PathVariable Long id, @AuthenticationPrincipal User user);

    @Operation(method = "GET", summary = "글 목록 조회", description = "전체 글 목록을 조회한다.")
    ReferenceRoomListResponse listData(@RequestParam(name = "query", defaultValue = "") String query,
                                       Pageable pageable, @AuthenticationPrincipal User user);

    @Operation(method = "GET", summary = "카테고리별 글 목록 조회", description = "카테고리별로 글 목록을 조회한다.")
    ReferenceRoomListResponse listDataByCategory(
            @RequestParam(name = "query", defaultValue = "") String query,
            @RequestParam("type") List<ReferenceRoomPostType> types, Pageable pageable,
            @AuthenticationPrincipal User user);

    @Operation(method = "POST", summary = "댓글 작성", description = "글에 댓글을 작성한다.")
    RfCommentResponse addComment(@PathVariable Long id, @Validated @RequestBody RfCommentRequest request,
                                 @AuthenticationPrincipal User user);

    @Operation(method = "DELETE", summary = "댓글 삭제", description = "댓글을 삭제한다.")
    void deleteComment(@PathVariable Long id, @PathVariable Long commentId, @AuthenticationPrincipal User user);
}
