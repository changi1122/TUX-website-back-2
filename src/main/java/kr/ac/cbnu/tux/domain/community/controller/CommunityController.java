package kr.ac.cbnu.tux.domain.community.controller;

import kr.ac.cbnu.tux.domain.common.dto.FileUploadResponse;
import kr.ac.cbnu.tux.domain.common.entity.Attachment;
import kr.ac.cbnu.tux.domain.common.enums.SortType;
import kr.ac.cbnu.tux.domain.common.service.AttachmentService;
import kr.ac.cbnu.tux.domain.common.service.LikeService;
import kr.ac.cbnu.tux.domain.community.controller.docs.CommunityControllerDocs;
import kr.ac.cbnu.tux.domain.community.dto.request.CmCommentRequest;
import kr.ac.cbnu.tux.domain.community.dto.request.CommunityRequest;
import kr.ac.cbnu.tux.domain.community.dto.response.CmCommentResponse;
import kr.ac.cbnu.tux.domain.community.dto.response.CommunityListResponse;
import kr.ac.cbnu.tux.domain.community.dto.response.CommunityResponse;
import kr.ac.cbnu.tux.domain.community.entity.CmComment;
import kr.ac.cbnu.tux.domain.community.entity.Community;
import kr.ac.cbnu.tux.domain.common.enums.SearchType;
import kr.ac.cbnu.tux.domain.community.enums.CommunityPostType;
import kr.ac.cbnu.tux.domain.community.exception.CommunityErrorCode;
import kr.ac.cbnu.tux.domain.community.exception.CommunityException;
import kr.ac.cbnu.tux.domain.community.service.CommunityService;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.global.utility.FileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import static kr.ac.cbnu.tux.domain.common.enums.AttachmentType.COMMUNITY;
import static kr.ac.cbnu.tux.domain.community.service.CommunityService.CAN_EDIT_ROLES;

@RequiredArgsConstructor
@Controller
public class CommunityController implements CommunityControllerDocs {

    private final CommunityService communityService;
    private final AttachmentService attachmentService;
    private final LikeService likeService;
    private final FileStore fileStore;

    /* 파일 업로드 없이 글쓰기 */
    @PostMapping("/api/community")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void createPost(@RequestParam CommunityPostType type, @Validated @RequestBody CommunityRequest request,
                           @AuthenticationPrincipal User user) {
        communityService.createPost(type, request, user, OffsetDateTime.now());
    }

    /* 글쓰기 도중 파일 업로드시 임시로 글 생성 후 파일 업로드 */
    @PostMapping(path = "/api/community/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public FileUploadResponse uploadFileBeforeCreatePost(@RequestParam CommunityPostType type,
                                           @RequestParam("file") MultipartFile file,
                                           @AuthenticationPrincipal User user) {

        Community post = communityService.createTemporalPostForFile(type, user, OffsetDateTime.now());
        Attachment attachment = attachmentService.createAttachment(file, post, user);
        communityService.addAttachment(attachment, post);
        fileStore.saveAttachment(COMMUNITY, post.getId().toString(), file, attachment.getFilename());
        return FileUploadResponse.of(post.getId(), attachment.getFilename());
    }

    /* 글이 생성된 이후 파일 업로드 */
    @PostMapping(path = "/api/community/{id}/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    @ResponseBody
    public FileUploadResponse uploadFileAfterCreatePost(@PathVariable Long id, @RequestParam("file") MultipartFile file,
                                          @AuthenticationPrincipal User user) {
        Community post = communityService.getPost(id);

        if (!user.equals(post.getUser()) && !CAN_EDIT_ROLES.contains(user.getRole())) {
            throw new CommunityException(CommunityErrorCode.USER_NOT_MATCHED);
        }

        Attachment attachment = attachmentService.createAttachment(file, post, user);
        communityService.addAttachment(attachment, post);
        fileStore.saveAttachment(COMMUNITY, post.getId().toString(), file, attachment.getFilename());
        return FileUploadResponse.of(post.getId(), attachment.getFilename());
    }

    /* 임시로 생성된 글 내용 업데이트 */
    @PostMapping("/api/community/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void updateTemporalPost(@PathVariable Long id, CommunityPostType type,
                                   @Validated @RequestBody CommunityRequest request, @AuthenticationPrincipal User user) {
        communityService.updateTemporalPost(id, type, request, user, OffsetDateTime.now());
    }

    /* 글 수정 */
    @PutMapping("/api/community/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void updatePost(@PathVariable Long id, CommunityPostType type, @Validated @RequestBody CommunityRequest request,
                           @AuthenticationPrincipal User user) {
        communityService.updatePost(id, type, request, user, OffsetDateTime.now());
    }

    /* 글 삭제 */
    @DeleteMapping("/api/community/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void deletePost(@PathVariable Long id, @AuthenticationPrincipal User user) {
        communityService.deletePost(id, user, OffsetDateTime.now());
    }

    /* 글 읽기 */
    @GetMapping("/api/community/{id}")
    @ResponseBody
    public CommunityResponse readPost(@PathVariable Long id, @AuthenticationPrincipal User user,
                                      HttpServletRequest request) {
        String identifier = (user != null) ? user.getId().toString() : request.getRemoteAddr();
        Community post = communityService.readPost(id, user, identifier);
        return CommunityResponse.of(post);
    }

    /* 게시판 리스트 조회 */
    @GetMapping("/api/community/list")
    @ResponseBody
    public CommunityListResponse listPosts(
            @RequestParam(name = "query", defaultValue = "") String query,
            @RequestParam(name = "searchType", defaultValue = "TITLE") SearchType searchType,
            @RequestParam(name = "sortType", defaultValue = "CREATED_DATE") SortType sortType,
            Pageable pageable) {

        Page<Community> page = communityService.list(query, searchType, sortType, pageable);
        return CommunityListResponse.of(page, pageable);
    }

    @GetMapping("/api/community/list/category")
    @ResponseBody
    public CommunityListResponse listPostsByCategory(
            @RequestParam(name = "query", defaultValue = "") String query,
            @RequestParam(name = "searchType", defaultValue = "TITLE") SearchType searchType,
            @RequestParam(name = "sortType", defaultValue = "CREATED_DATE") SortType sortType,
            @RequestParam("type") List<CommunityPostType> categories, Pageable pageable) {

        Page<Community> page = communityService.listByCategories(categories, query, searchType, sortType, pageable);
        return CommunityListResponse.of(page, pageable);
    }

    /* 댓글 */
    @PostMapping("/api/community/{id}/comment")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    @ResponseBody
    public CmCommentResponse addComment(@PathVariable Long id, @Validated @RequestBody CmCommentRequest request,
                                        @AuthenticationPrincipal User user) {

        CmComment savedComment = communityService.addComment(id, request, user, OffsetDateTime.now());
        return CmCommentResponse.of(savedComment);
    }

    @DeleteMapping("/api/community/{id}/comment/{commentId}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void deleteComment(@PathVariable Long id, @PathVariable Long commentId,
                              @AuthenticationPrincipal User user) {

        communityService.deleteComment(id, commentId, user, OffsetDateTime.now());
    }


    /* 첨부파일 다운로드 */
    @GetMapping(value = "/api/community/{id}/file/{filename}")
    public ResponseEntity<FileSystemResource> getFile(
            @PathVariable String id, @PathVariable String filename,
            @RequestParam(name = "aid", defaultValue = "-1") Long aid,
            HttpServletRequest request) {

        String path = fileStore.getCommunityAttachmentFilePath(id, filename);
        File file = new File(path);
        if (!file.exists()) {
            throw new CommunityException(CommunityErrorCode.NOT_FOUND);
        }

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(Files.probeContentType(Path.of(path)));
        } catch (Exception e) {
            mediaType = MediaType.parseMediaType("application/octet-stream");
        }

        boolean isImage = mediaType.getType().equals("image");

        long lastModifiedMillis = file.lastModified();
        String etag = "\"" + file.length() + "-" + lastModifiedMillis + "\"";

        String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        // 다운로드 수 늘리기 (캐시 히트 시 제외)
        if (aid != -1)
            attachmentService.increaseDownloadCountById(aid);

        // aid가 있으면 명시적 다운로드 요청 → attachment, aid가 없는 이미지는 inline (페이지 내 임베드)
        String disposition = (aid == -1 && isImage) ? "inline"
                : "attachment; filename=\"" + new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1) + "\"";
        String cacheControl = isImage
                ? "public, max-age=86400, stale-while-revalidate=3600"
                : "private, no-cache";

        return ResponseEntity.ok()
                .contentType(mediaType)
                .eTag(etag)
                .lastModified(Instant.ofEpochMilli(lastModifiedMillis))
                .header(HttpHeaders.CACHE_CONTROL, cacheControl)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(new FileSystemResource(file));
    }

    /* 첨부파일 삭제 */
    @DeleteMapping(value = "/api/community/{id}/file/{filename}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void deleteFile(@PathVariable Long id, @PathVariable String filename,
                           @AuthenticationPrincipal User user) {
        Community post = communityService.getPost(id);

        if (!post.getUser().equals(user) && !CAN_EDIT_ROLES.contains(user.getRole())) {
            throw new CommunityException(CommunityErrorCode.USER_NOT_MATCHED);
        }

        Attachment file = attachmentService.getFile(URLDecoder.decode(filename, StandardCharsets.UTF_8), post);
        attachmentService.deleteAttachment(file, post);
    }

    /* 추천 비추천 추가 */
    @PostMapping("/api/community/{id}/likes")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void addLike(@PathVariable Long id, @RequestParam Boolean dislike,
                        @AuthenticationPrincipal User user) {
        if (user == null)
            throw new CommunityException(CommunityErrorCode.USER_NOT_LOGGED_IN);

        likeService.createLikeOnCommunity(id, user, dislike, OffsetDateTime.now());
    }
}
