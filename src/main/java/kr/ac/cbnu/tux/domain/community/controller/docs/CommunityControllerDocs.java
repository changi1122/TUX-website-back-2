package kr.ac.cbnu.tux.domain.community.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.cbnu.tux.domain.common.dto.FileUploadResponse;
import kr.ac.cbnu.tux.domain.common.enums.SortType;
import kr.ac.cbnu.tux.domain.community.dto.request.CmCommentRequest;
import kr.ac.cbnu.tux.domain.community.dto.request.CommunityRequest;
import kr.ac.cbnu.tux.domain.community.dto.response.CmCommentResponse;
import kr.ac.cbnu.tux.domain.community.dto.response.CommunityListResponse;
import kr.ac.cbnu.tux.domain.community.dto.response.CommunityResponse;
import kr.ac.cbnu.tux.domain.common.enums.SearchType;
import kr.ac.cbnu.tux.domain.community.enums.CommunityPostType;
import kr.ac.cbnu.tux.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Tag(name = "커뮤니티(community)", description = "커뮤니티 API")
public interface CommunityControllerDocs {

    @Operation(method = "POST", summary = "글 작성", description = "파일 첨부 없이 새로운 글을 작성한다.")
    void createPost(@RequestParam CommunityPostType type, @Validated @RequestBody CommunityRequest request,
                    @AuthenticationPrincipal User user);

    @Operation(method = "POST", summary = "최초 파일 업로드", description = "글쓰기 도중 파일 업로드시 임시로 글 생성 후 파일을 저장한다.")
    FileUploadResponse uploadFileBeforeCreatePost(@RequestParam CommunityPostType type,
                                                  @RequestParam("file") MultipartFile file, @AuthenticationPrincipal User user);

    @Operation(method = "POST", summary = "(글 생성 이후) 파일 업로드", description = "글쓰기 이후 파일 업로드시 파일을 저장한다.")
    FileUploadResponse uploadFileAfterCreatePost(@PathVariable Long id, @RequestParam("file") MultipartFile file,
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
    CommunityResponse readPost(@PathVariable Long id, @AuthenticationPrincipal User user,
                               HttpServletRequest request);

    @Operation(method = "GET", summary = "글 목록 조회", description = "전체 글 목록을 조회한다.")
    CommunityListResponse listPosts(@RequestParam(name = "query", defaultValue = "") String query,
                                    @RequestParam(name = "searchType", defaultValue = "TITLE") SearchType searchType,
                                    @RequestParam(name = "sortType", defaultValue = "CREATED_DATE") SortType sortType,
                                    Pageable pageable);

    @Operation(method = "GET", summary = "카테고리별 글 목록 조회", description = "카테고리별로 글 목록을 조회한다.")
    CommunityListResponse listPostsByCategory(
            @RequestParam(name = "query", defaultValue = "") String query,
            @RequestParam(name = "searchType", defaultValue = "TITLE") SearchType searchType,
            @RequestParam(name = "sortType", defaultValue = "CREATED_DATE") SortType sortType,
            @RequestParam("type") List<CommunityPostType> categories, Pageable pageable);

    @Operation(method = "POST", summary = "댓글 작성", description = "글에 댓글을 작성한다.")
    CmCommentResponse addComment(@PathVariable Long id, @Validated @RequestBody CmCommentRequest request,
                                 @AuthenticationPrincipal User user);

    @Operation(method = "DELETE", summary = "댓글 삭제", description = "댓글을 삭제한다.")
    void deleteComment(@PathVariable Long id, @PathVariable Long commentId, @AuthenticationPrincipal User user);

    @Operation(method = "GET", summary = "첨부파일 다운로드", description = "첨부파일을 다운로드한다.")
    ResponseEntity<FileSystemResource> getFile(@PathVariable String id, @PathVariable String filename,
                                               @RequestParam(name = "aid", defaultValue = "-1") Long aid,
                                               HttpServletRequest request);

    @Operation(method = "DELETE", summary = "첨부파일 삭제", description = "첨부파일을 삭제한다.")
    void deleteFile(@PathVariable Long id, @PathVariable String filename,
                    @AuthenticationPrincipal User user) throws IOException;

    @Operation(method = "POST", summary = "추천/비추천", description = "글에 추천 또는 비추천을 추가한다.")
    void addLike(@PathVariable Long id, @RequestParam Boolean dislike,
                 @AuthenticationPrincipal User user);
}
