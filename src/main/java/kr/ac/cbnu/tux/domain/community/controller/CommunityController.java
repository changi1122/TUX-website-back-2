package kr.ac.cbnu.tux.domain.community.controller;

import kr.ac.cbnu.tux.domain.common.entity.Attachment;
import kr.ac.cbnu.tux.domain.common.service.AttachmentService;
import kr.ac.cbnu.tux.domain.common.service.LikeService;
import kr.ac.cbnu.tux.domain.community.controller.docs.CommunityControllerDocs;
import kr.ac.cbnu.tux.domain.community.dto.request.CommunityRequest;
import kr.ac.cbnu.tux.domain.community.dto.response.CmCommentDTO;
import kr.ac.cbnu.tux.domain.community.dto.response.CommunityDTO;
import kr.ac.cbnu.tux.domain.community.dto.response.CommunityListDTO;
import kr.ac.cbnu.tux.domain.community.entity.CmComment;
import kr.ac.cbnu.tux.domain.community.entity.Community;
import kr.ac.cbnu.tux.domain.community.enums.CommunityPostType;
import kr.ac.cbnu.tux.domain.community.service.CommunityService;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.domain.user.enums.UserRole;
import kr.ac.cbnu.tux.global.utility.FileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

import static kr.ac.cbnu.tux.domain.common.enums.AttachmentType.COMMUNITY;

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
    public Long uploadFileBeforeCreatePost(@RequestParam CommunityPostType type,
                                           @RequestParam("file") MultipartFile file,
                                           @AuthenticationPrincipal User user) {

        Community post = communityService.createTemporalPostForFile(type, user, OffsetDateTime.now());
        Attachment attachment = attachmentService.createAttachment(file, post);
        communityService.addAttachment(attachment, post);
        fileStore.saveAttachment(COMMUNITY, post.getId().toString(), file);
        return post.getId();
    }

    /* 글이 생성된 이후 파일 업로드 */
    @PostMapping(path = "/api/community/{id}/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void uploadFileAfterCreatePost(@PathVariable Long id, @RequestParam("file") MultipartFile file,
                                          @AuthenticationPrincipal User user) {
        Community post = communityService.getPost(id);

        if (!user.getId().equals(post.getUser().getId()) &&
                !List.of(UserRole.ADMIN, UserRole.MANAGER).contains(user.getRole())) {
            throw new RuntimeException("user not matched");
        }

        Attachment attachment = attachmentService.createAttachment(file, post);
        communityService.addAttachment(attachment, post);
        fileStore.saveAttachment(COMMUNITY, post.getId().toString(), file);
    }

    /* 임시로 생성된 글 내용 업데이트 */
    @PostMapping("/api/community/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void updateTemporalPost(@PathVariable Long id, @RequestBody Community post,
                                   @AuthenticationPrincipal User user) throws Exception {
        communityService.updateAfterTemporalCreate(id, post, user);
    }

    /* 글 수정 */
    @PutMapping("/api/community/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void updatePost(@PathVariable Long id, CommunityPostType type, @RequestBody Community updated,
                           @AuthenticationPrincipal User user) throws Exception {
        communityService.update(id, type, updated, user);
    }

    /* 글 삭제 */
    @DeleteMapping("/api/community/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void deletePost(@PathVariable Long id, @AuthenticationPrincipal User user) throws Exception {
        communityService.delete(id, user);
    }

    /* 글 읽기 */
    @GetMapping("/api/community/{id}")
    @ResponseBody
    public CommunityDTO readPost(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Community post = communityService.read(id, user);
        return CommunityDTO.build(post);
    }


    /* 게시판 리스트 조회 */

    @GetMapping("/api/community/list")
    @ResponseBody
    public Page<CommunityListDTO> listPosts(@RequestParam(name = "query", defaultValue = "") String query,
                                            Pageable pageable) {
        Page<Community> found;
            if (StringUtils.hasText(query)) {
                found = communityService.searchList(query, pageable);
            } else {
                found = communityService.list(pageable);
        }

        return new PageImpl<>(
            found.getContent().stream().map(CommunityListDTO::build).toList(),
            pageable,
            found.getTotalElements()
        );
    }

    @GetMapping("/api/community/list/category")
    @ResponseBody
    public Page<CommunityListDTO> listPostsByCategory(
            @RequestParam(name = "query", defaultValue = "") String query,
            @RequestParam("type") List<CommunityPostType> types, Pageable pageable) {

        Page<Community> found;
        if (StringUtils.hasText(query)) {
            found = communityService.searchListByCategories(query, pageable, types);
        } else {
            found = communityService.listByCategories(pageable, types);
        }
        return new PageImpl<>(
                found.getContent().stream().map(CommunityListDTO::build).toList(),
                pageable,
                found.getTotalElements()
        );
    }


    /* 댓글 */
    @PostMapping("/api/community/{id}/comment")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    @ResponseBody
    public CmCommentDTO addComment(@PathVariable Long id, @RequestBody CmComment comment,
                                   @AuthenticationPrincipal User user) {

        CmComment savedComment = communityService.addComment(id, comment, user);
        return CmCommentDTO.build(savedComment);
    }

    @DeleteMapping("/api/community/{id}/comment/{commentId}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void deleteComment(@PathVariable Long id, @PathVariable Long commentId,
                              @AuthenticationPrincipal User user) throws Exception {

        communityService.deleteComment(commentId, user);
    }


    /* 첨부파일 다운로드 */
    @GetMapping(value = "/api/community/{id}/file/{filename}")
    public ResponseEntity<FileSystemResource> getFile(
            @PathVariable String id, @PathVariable String filename,
            @RequestParam(name = "aid", defaultValue = "-1") Long aid) throws Exception {

        // 다운로드 수 늘리기
        if (aid != -1)
            attachmentService.increaseDownloadCountById(aid);

        String path = fileStore.getCommunityAttachmentFilePath(id, filename);

        if (new File(path).exists()) {
            FileSystemResource resource = new FileSystemResource(path);

            MediaType mediaType;
            try {
                mediaType = MediaType.parseMediaType(Files.probeContentType(Path.of(path)));
            } catch (Exception e){
                mediaType = MediaType.parseMediaType("application/octet-stream");
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                            new String(filename.getBytes("UTF-8"), "ISO-8859-1") + "\"")
                    .body(resource);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }
    }

    /* 첨부파일 삭제 */
    @DeleteMapping(value = "/api/community/{id}/file/{filename}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void deleteFile(@PathVariable Long id, @PathVariable String filename,
                           @AuthenticationPrincipal User user) throws Exception {
        Community post = communityService.getPost(id);

        if (user.getId().equals(post.getUser().getId()) || user.getRole() == UserRole.ADMIN) {
            Attachment file = attachmentService.getFile(URLDecoder.decode(filename, StandardCharsets.UTF_8), post);
            attachmentService.deleteAttachment(file, post);
        } else {
            throw new Exception("user not matched");
        }
    }

    /* 추천 비추천 추가 */
    @PostMapping("/api/community/{id}/likes")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void addLike(@PathVariable Long id, @RequestParam Boolean dislike,
                        @AuthenticationPrincipal User user) throws Exception {
        if (user == null)
            throw new Exception("user not logged in");

        Community post = communityService.getPost(id);
        likeService.create(post, user, dislike);
    }
}
