package kr.ac.cbnu.tux.domain.community.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.cbnu.tux.domain.community.dto.request.CommunityRequest;
import kr.ac.cbnu.tux.domain.community.dto.response.CommunityResponse;
import kr.ac.cbnu.tux.domain.community.enums.CommunityPostType;
import kr.ac.cbnu.tux.domain.user.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "커뮤니티(community)", description = "커뮤니티 API")
public interface CommunityControllerDocs {

    @Operation(method = "POST", summary = "글 작성", description = "파일 첨부 없이 새로운 글을 작성한다.")
    void createPost(@RequestParam CommunityPostType type, @Validated @RequestBody CommunityRequest request,
                    @AuthenticationPrincipal User user);

    @Operation(method = "POST", summary = "최초 파일 업로드", description = "글쓰기 도중 파일 업로드시 임시로 글 생성 후 파일을 저장한다.")
    Long uploadFileBeforeCreatePost(@RequestParam CommunityPostType type,
                                    @RequestParam("file") MultipartFile file, @AuthenticationPrincipal User user);

    @Operation(method = "POST", summary = "(글 생성 이후) 파일 업로드", description = "글쓰기 이후 파일 업로드시 파일을 저장한다.")
    void uploadFileAfterCreatePost(@PathVariable Long id, @RequestParam("file") MultipartFile file,
                                   @AuthenticationPrincipal User user);

    @Operation(method = "POST", summary = "(글 생성 이후) 글 작성", description = "임시로 생성된 글의 내용을 작성한다.")
    void updateTemporalPost(@PathVariable Long id, CommunityPostType type,
                            @Validated @RequestBody CommunityRequest request, @AuthenticationPrincipal User user);

    @Operation(method = "PUT", summary = "글 수정", description = "글의 내용을 수정한다.")
    void updatePost(@PathVariable Long id, CommunityPostType type, @Validated @RequestBody CommunityRequest request,
                    @AuthenticationPrincipal User user);

    @Operation(method = "DELETE", summary = "글 삭제", description = "글을 삭제한다.")
    void deletePost(@PathVariable Long id, @AuthenticationPrincipal User user);

    @Operation(method = "GET", summary = "글 조회", description = "글을 조회한다.")
    CommunityResponse readPost(@PathVariable Long id, @AuthenticationPrincipal User user);
}
