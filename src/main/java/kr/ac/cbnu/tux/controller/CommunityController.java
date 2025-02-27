package kr.ac.cbnu.tux.controller;

import kr.ac.cbnu.tux.domain.Attachment;
import kr.ac.cbnu.tux.domain.CmComment;
import kr.ac.cbnu.tux.domain.Community;
import kr.ac.cbnu.tux.domain.User;
import kr.ac.cbnu.tux.dto.CommunityDTO;
import kr.ac.cbnu.tux.dto.CommunityListDTO;
import kr.ac.cbnu.tux.enums.CommunityPostType;
import kr.ac.cbnu.tux.enums.UserRole;
import kr.ac.cbnu.tux.service.AttachmentService;
import kr.ac.cbnu.tux.service.CommunityService;
import kr.ac.cbnu.tux.service.LikeService;
import kr.ac.cbnu.tux.utility.FileHandler;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Controller
public class CommunityController {

    private final CommunityService communityService;
    private final AttachmentService attachmentService;
    private final LikeService likeService;

    @Autowired
    public CommunityController(CommunityService communityService, AttachmentService attachmentService,
                               LikeService likeService) {
        this.communityService = communityService;
        this.attachmentService = attachmentService;
        this.likeService = likeService;
    }


    /* 파일 업로드 없이 글쓰기 */
    @PostMapping("/api/community")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void createWithoutFileUpload(CommunityPostType type, @RequestBody Community post,
                                        @AuthenticationPrincipal User user) {
        communityService.createWithoutFileUpload(type, post, user);
    }

    /* 글 생성 이전에 파일 업로드시 임시로 글 생성 후 파일 업로드 */
    @PostMapping(path = "/api/community/file")
    @ResponseBody
    public Long fileUploadBeforeCreation(
            CommunityPostType type, @RequestParam("file") MultipartFile multipartFile,
            @AuthenticationPrincipal User user) throws IOException {

        Community post = communityService.temporalCreate(type, user);
        Attachment file = attachmentService.create(multipartFile, post);
        communityService.addAttachment(file, post);
        FileHandler.saveAttactment("community", post.getId().toString(), multipartFile);
        return post.getId();
    }

    /* 글이 생성된 이후 파일 업로드 */
    @PostMapping(path = "/api/community/{id}/file")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void addFile(
            @PathVariable("id") Long id, @RequestParam("file") MultipartFile multipartFile,
            @AuthenticationPrincipal User user) throws Exception {
        Community post = communityService.getData(id).orElseThrow();

        if (user.getId().equals(post.getUser().getId()) || user.getRole() == UserRole.ADMIN) {
            Attachment file = attachmentService.create(multipartFile, post);
            communityService.addAttachment(file, post);
            FileHandler.saveAttactment("community", post.getId().toString(), multipartFile);
        } else {
            throw new Exception("user not matched");
        }
    }

    /* 임시로 생성된 글 내용 업데이트 */
    @PostMapping("/api/community/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void updateAfterTemporalCreate(@PathVariable("id") Long id, @RequestBody Community post,
                                          @AuthenticationPrincipal User user) throws Exception {
        communityService.updateAfterTemporalCreate(id, post, user);
    }

    /* 글 수정 */
    @PutMapping("/api/community/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void update(@PathVariable("id") Long id,  CommunityPostType type, @RequestBody Community updated,
                       @AuthenticationPrincipal User user) throws Exception {
        communityService.update(id, type, updated, user);
    }

    /* 글 삭제 */
    @DeleteMapping("/api/community/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void delete(@PathVariable("id") Long id, @AuthenticationPrincipal User user) throws Exception {
            communityService.delete(id, user);
    }

    /* 글 읽기 */
    @GetMapping("/api/community/{id}")
    @ResponseBody
    public CommunityDTO read(@PathVariable("id") Long id, @AuthenticationPrincipal User user) {
        Community post = communityService.read(id, user);
        return CommunityDTO.build(post);
    }


    /* 게시판 리스트 조회 */

    @GetMapping("/api/community/list")
    @ResponseBody
    public Page<CommunityListDTO> list(@RequestParam(name = "query", defaultValue = "") String query,
                                       Pageable pageable) {
        Page<Community> found;
            if (StringUtils.hasText(query)) {
                found = communityService.searchList(query, pageable);
            } else {
                found = communityService.list(pageable);
        }

        return new PageImpl<>(
            found.getContent().stream().map(post -> CommunityListDTO.build(post)).toList(),
            pageable,
            found.getTotalElements()
        );
    }

    @GetMapping("/api/community/list/category")
    @ResponseBody
    public Page<CommunityListDTO> listByCategory(
            @RequestParam(name = "query", defaultValue = "") String query,
            @RequestParam("type") List<CommunityPostType> types, Pageable pageable) {

        Page<Community> found;
        if (StringUtils.hasText(query)) {
            found = communityService.searchListByCategories(query, pageable, types);
        } else {
            found = communityService.listByCategories(pageable, types);
        }
        return new PageImpl<>(
                found.getContent().stream().map(post -> CommunityListDTO.build(post)).toList(),
                pageable,
                found.getTotalElements()
        );
    }


    /* 댓글 */
    @PostMapping("/api/community/{id}/comment")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void addComment(@PathVariable("id") Long id, @RequestBody CmComment comment,
                           @AuthenticationPrincipal User user) {

        communityService.addComment(id, comment, user);
    }

    @DeleteMapping("/api/community/{id}/comment/{commentId}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void deleteComment(@PathVariable("id") Long id, @PathVariable("commentId") Long commentId,
                              @AuthenticationPrincipal User user) throws Exception {

        communityService.deleteComment(commentId, user);
    }


    /* 첨부파일 다운로드 */
    @GetMapping(value = "/api/community/{id}/file/{filename}")
    public ResponseEntity<FileSystemResource> getFile(
            @PathVariable("id") String id, @PathVariable("filename") String filename,
            @RequestParam(name = "aid", defaultValue = "-1") Long aid) throws Exception {

        // 다운로드 수 늘리기
        if (aid != -1)
            attachmentService.increaseDownloadCountById(aid);

        String path = System.getProperty("user.dir") +
                String.format("/upload/file/community/%s/%s", id, URLDecoder.decode(filename, StandardCharsets.UTF_8));

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
    public void deleteFile(@PathVariable("id") Long id, @PathVariable("filename") String filename,
                           @AuthenticationPrincipal User user) throws Exception {
        Community post = communityService.getData(id).orElseThrow();

        if (user.getId().equals(post.getUser().getId()) || user.getRole() == UserRole.ADMIN) {
            Attachment file = attachmentService.getFile(URLDecoder.decode(filename, StandardCharsets.UTF_8), post).orElseThrow();
            attachmentService.delete(file, post);
        } else {
            throw new Exception("user not matched");
        }
    }

    /* 추천 비추천 추가 */
    @PostMapping("/api/community/{id}/likes")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void addLike(@PathVariable("id") Long id, @RequestParam Boolean dislike,
                           @AuthenticationPrincipal User user) throws Exception {
        if (user == null)
            throw new Exception("user not logged in");

        Community post = communityService.getData(id).orElseThrow();
        likeService.create(post, user, dislike);
    }
}
