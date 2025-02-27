package kr.ac.cbnu.tux.controller;

import kr.ac.cbnu.tux.domain.Attachment;
import kr.ac.cbnu.tux.domain.ReferenceRoom;
import kr.ac.cbnu.tux.domain.RfComment;
import kr.ac.cbnu.tux.domain.User;
import kr.ac.cbnu.tux.dto.ReferenceRoomDTO;
import kr.ac.cbnu.tux.dto.ReferenceRoomListDTO;
import kr.ac.cbnu.tux.enums.ReferenceRoomPostType;
import kr.ac.cbnu.tux.enums.UserRole;
import kr.ac.cbnu.tux.service.AttachmentService;
import kr.ac.cbnu.tux.service.LikeService;
import kr.ac.cbnu.tux.service.ReferenceRoomService;
import kr.ac.cbnu.tux.utility.FileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class ReferenceRoomController {

    private final ReferenceRoomService referenceRoomService;
    private final AttachmentService attachmentService;
    private final LikeService likeService;
    private final FileStore fileStore;

    @Value("${file.dir}")
    private String fileDir;

    /* 파일 업로드 없이 글쓰기 */
    @PostMapping("/api/referenceroom")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void createWithoutFileUpload(ReferenceRoomPostType type, @RequestBody ReferenceRoom data,
                                        @AuthenticationPrincipal User user) {
        referenceRoomService.createWithoutFileUpload(type, data, user);
    }

    /* 글 생성 이전에 파일 업로드시 임시로 글 생성 후 파일 업로드 */
    @PostMapping(path = "/api/referenceroom/file")
    @ResponseBody
    public Long fileUploadBeforeCreation(
            ReferenceRoomPostType type, @RequestParam("file") MultipartFile multipartFile,
            @AuthenticationPrincipal User user) throws IOException {

        ReferenceRoom data = referenceRoomService.temporalCreate(type, user);
        Attachment file = attachmentService.create(multipartFile, data);
        referenceRoomService.addAttachment(file, data);
        fileStore.saveAttactment("referenceroom", data.getId().toString(), multipartFile);
        return data.getId();
    }

    /* 글이 생성된 이후 파일 업로드 */
    @PostMapping(path = "/api/referenceroom/{id}/file")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void addFile(@PathVariable("id") Long id, @RequestParam("file") MultipartFile multipartFile,
                        @AuthenticationPrincipal User user) throws Exception {
        ReferenceRoom data = referenceRoomService.getData(id).orElseThrow();

        if (user.getId().equals(data.getUser().getId()) || user.getRole() == UserRole.ADMIN) {
            Attachment file = attachmentService.create(multipartFile, data);
            referenceRoomService.addAttachment(file, data);
            fileStore.saveAttactment("referenceroom", data.getId().toString(), multipartFile);
        } else {
            throw new Exception("user not matched");
        }
    }

    /* 임시로 생성된 글 내용 업데이트 */
    @PostMapping("/api/referenceroom/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void updateAfterTemporalCreate(@PathVariable("id") Long id, @RequestBody ReferenceRoom data,
                                          @AuthenticationPrincipal User user) throws Exception {
        referenceRoomService.updateAfterTemporalCreate(id, data, user);
    }


    /* 글 수정 */
    @PutMapping("/api/referenceroom/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void update(@PathVariable("id") Long id, ReferenceRoomPostType type, @RequestBody ReferenceRoom updated,
                       @AuthenticationPrincipal User user) throws Exception {
        referenceRoomService.update(id, type, updated, user);
    }
    
    /* 글 삭제 */
    @DeleteMapping("/api/referenceroom/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void delete(@PathVariable("id") Long id, @AuthenticationPrincipal User user) throws Exception {
        referenceRoomService.delete(id, user);
    }

    /* 글 읽기 */
    @GetMapping("/api/referenceroom/{id}")
    @ResponseBody
    public ReferenceRoomDTO read(@PathVariable("id") Long id, @AuthenticationPrincipal User user) throws AccessDeniedException {
        ReferenceRoom data = referenceRoomService.read(id, user);

        if (data.getCategory().cannotReadBy(user)) {
            throw new AccessDeniedException("permission denied");
        }

        return ReferenceRoomDTO.build(data);
    }

    /* 자료실 리스트 조회 */

    @GetMapping("/api/referenceroom/list")
    @ResponseBody
    public Page<ReferenceRoomListDTO> list(@RequestParam(name = "query", defaultValue = "") String query,
                                           Pageable pageable, @AuthenticationPrincipal User user) throws AccessDeniedException {
        if (ReferenceRoomPostType.cannotListBy(user)) {
            throw new AccessDeniedException("permission denied");
        }

        Page<ReferenceRoom> found;
        if (StringUtils.hasText(query)) {
            found = referenceRoomService.searchList(query, pageable);
        } else {
            found = referenceRoomService.list(pageable);
        }

        return new PageImpl<>(
                found.getContent().stream().map(data -> ReferenceRoomListDTO.build(data)).toList(),
                pageable,
                found.getTotalElements()
        );
    }

    @GetMapping("/api/referenceroom/list/category")
    @ResponseBody
    public Page<ReferenceRoomListDTO> listByCategory(
            @RequestParam(name = "query", defaultValue = "") String query,
            @RequestParam("type") List<ReferenceRoomPostType> types, Pageable pageable,
            @AuthenticationPrincipal User user) throws AccessDeniedException {

        if (types.stream().anyMatch(type -> type.cannotReadBy(user))) {
            throw new AccessDeniedException("permission denied");
        }

        Page<ReferenceRoom> found;
        if (StringUtils.hasText(query)) {
            found = referenceRoomService.searchListByCategories(query, pageable, types);
        } else {
            found = referenceRoomService.listByCategories(pageable, types);
        }
        return new PageImpl<>(
                found.getContent().stream().map(data -> ReferenceRoomListDTO.build(data)).toList(),
                pageable,
                found.getTotalElements()
        );
    }
    
    
    /* 댓글 */
    @PostMapping("/api/referenceroom/{id}/comment")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void addComment(@PathVariable("id") Long id, @RequestBody RfComment comment,
                           @AuthenticationPrincipal User user) {
        referenceRoomService.addComment(id, comment, user);
    }

    @DeleteMapping("/api/referenceroom/{id}/comment/{commentId}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void deleteComment(@PathVariable("id") Long id, @PathVariable("commentId") Long commentId,
                              @AuthenticationPrincipal User user) throws Exception {
        referenceRoomService.deleteComment(commentId, user);
    }


    /* 첨부파일 다운로드 */
    @GetMapping(value = "/api/referenceroom/{id}/file/{filename}")
    public ResponseEntity<FileSystemResource> getFile(
            @PathVariable("id") Long id, @PathVariable("filename") String filename,
            @RequestParam(name = "aid", defaultValue = "-1") Long aid, @AuthenticationPrincipal User user) throws Exception {

        ReferenceRoom data = referenceRoomService.getData(id).orElseThrow();
        if (data.getCategory().cannotReadBy(user)) {
            throw new AccessDeniedException("permission denied");
        }

        // 다운로드 수 늘리기
        if (aid != -1)
            attachmentService.increaseDownloadCountById(aid);

        String path = fileDir +
                String.format("file/referenceroom/%s/%s", id, URLDecoder.decode(filename, StandardCharsets.UTF_8));

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
    @DeleteMapping(value = "/api/referenceroom/{id}/file/{filename}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void deleteFile(@PathVariable("id") Long id, @PathVariable("filename") String filename,
                           @AuthenticationPrincipal User user) throws Exception {
        ReferenceRoom data = referenceRoomService.getData(id).orElseThrow();

        if (user.getId().equals(data.getUser().getId()) || user.getRole() == UserRole.ADMIN) {
            Attachment file = attachmentService.getFile(URLDecoder.decode(filename, StandardCharsets.UTF_8), data).orElseThrow();
            attachmentService.delete(file, data);
        } else {
            throw new Exception("user not matched");
        }
    }

    /* 추천 비추천 추가 */
    @PostMapping("/api/referenceroom/{id}/likes")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void addLike(@PathVariable("id") Long id, @RequestParam Boolean dislike,
                        @AuthenticationPrincipal User user) throws Exception {
        if (user == null)
            throw new Exception("user not logged in");

        ReferenceRoom data = referenceRoomService.getData(id).orElseThrow();
        likeService.create(data, user, dislike);
    }
}
