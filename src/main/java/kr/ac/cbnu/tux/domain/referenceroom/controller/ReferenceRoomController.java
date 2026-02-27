package kr.ac.cbnu.tux.domain.referenceroom.controller;

import kr.ac.cbnu.tux.domain.common.entity.Attachment;
import kr.ac.cbnu.tux.domain.common.service.AttachmentService;
import kr.ac.cbnu.tux.domain.common.service.LikeService;
import kr.ac.cbnu.tux.domain.referenceroom.controller.docs.ReferenceRoomControllerDocs;
import kr.ac.cbnu.tux.domain.referenceroom.dto.request.ReferenceRoomRequest;
import kr.ac.cbnu.tux.domain.referenceroom.dto.request.RfCommentRequest;
import kr.ac.cbnu.tux.domain.referenceroom.dto.response.ReferenceRoomListResponse;
import kr.ac.cbnu.tux.domain.referenceroom.dto.response.ReferenceRoomResponse;
import kr.ac.cbnu.tux.domain.referenceroom.dto.response.RfCommentResponse;
import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import kr.ac.cbnu.tux.domain.referenceroom.entity.RfComment;
import kr.ac.cbnu.tux.domain.common.enums.SearchType;
import kr.ac.cbnu.tux.domain.referenceroom.enums.ReferenceRoomPostType;
import kr.ac.cbnu.tux.domain.referenceroom.service.ReferenceRoomService;
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
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import static kr.ac.cbnu.tux.domain.common.enums.AttachmentType.REFERENCEROOM;
import static kr.ac.cbnu.tux.domain.referenceroom.service.ReferenceRoomService.CAN_EDIT_ROLES;

@RequiredArgsConstructor
@Controller
public class ReferenceRoomController implements ReferenceRoomControllerDocs {

    private final ReferenceRoomService referenceRoomService;
    private final AttachmentService attachmentService;
    private final LikeService likeService;
    private final FileStore fileStore;

    /* 파일 업로드 없이 글쓰기 */
    @PostMapping("/api/referenceroom")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void createData(@RequestParam ReferenceRoomPostType type, @Validated @RequestBody ReferenceRoomRequest request,
                           @AuthenticationPrincipal User user) {
        referenceRoomService.createData(type, request, user, OffsetDateTime.now());
    }

    /* 글쓰기 도중 파일 업로드시 임시로 글 생성 후 파일 업로드 */
    @PostMapping(path = "/api/referenceroom/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public Long uploadFileBeforeCreateData(@RequestParam ReferenceRoomPostType type,
                                           @RequestParam("file") MultipartFile file, @AuthenticationPrincipal User user) {

        ReferenceRoom data = referenceRoomService.createTemporalDataForFile(type, user, OffsetDateTime.now());
        Attachment attachment = attachmentService.createAttachment(file, data);
        referenceRoomService.addAttachment(attachment, data);
        fileStore.saveAttachment(REFERENCEROOM, data.getId().toString(), file);
        return data.getId();
    }

    /* 글이 생성된 이후 파일 업로드 */
    @PostMapping(path = "/api/referenceroom/{id}/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void uploadFileAfterCreateData(@PathVariable Long id, @RequestParam("file") MultipartFile file,
                                          @AuthenticationPrincipal User user) {
        ReferenceRoom data = referenceRoomService.getData(id);

        if (!user.equals(data.getUser()) && !CAN_EDIT_ROLES.contains(user.getRole())) {
            throw new RuntimeException("user not matched");
        }

        Attachment attachment = attachmentService.createAttachment(file, data);
        referenceRoomService.addAttachment(attachment, data);
        fileStore.saveAttachment(REFERENCEROOM, data.getId().toString(), file);
    }

    /* 임시로 생성된 글 내용 업데이트 */
    @PostMapping("/api/referenceroom/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void updateTemporalData(@PathVariable Long id, ReferenceRoomPostType type,
                                   @Validated @RequestBody ReferenceRoomRequest request, @AuthenticationPrincipal User user) {
        referenceRoomService.updateTemporalData(id, type, request, user, OffsetDateTime.now());
    }


    /* 글 수정 */
    @PutMapping("/api/referenceroom/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void updateData(@PathVariable Long id, ReferenceRoomPostType type,
                           @Validated @RequestBody ReferenceRoomRequest request, @AuthenticationPrincipal User user) {
        referenceRoomService.updateData(id, type, request, user, OffsetDateTime.now());
    }
    
    /* 글 삭제 */
    @DeleteMapping("/api/referenceroom/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void deleteData(@PathVariable Long id, @AuthenticationPrincipal User user) {
        referenceRoomService.deleteData(id, user, OffsetDateTime.now());
    }

    /* 글 읽기 */
    @GetMapping("/api/referenceroom/{id}")
    @ResponseBody
    public ReferenceRoomResponse readData(@PathVariable Long id, @AuthenticationPrincipal User user,
                                          HttpServletRequest request) {
        String identifier = (user != null) ? user.getId().toString() : request.getRemoteAddr();
        ReferenceRoom data = referenceRoomService.readData(id, user, identifier);
        return ReferenceRoomResponse.of(data);
    }

    /* 자료실 리스트 조회 */
    @GetMapping("/api/referenceroom/list")
    @ResponseBody
    public ReferenceRoomListResponse listData(@RequestParam(name = "query", defaultValue = "") String query,
                                              @RequestParam(name = "searchType", defaultValue = "TITLE") SearchType searchType,
                                              Pageable pageable, @AuthenticationPrincipal User user) {
        if (ReferenceRoomPostType.cannotListBy(user)) {
            throw new RuntimeException("permission denied");
        }

        Page<ReferenceRoom> page;
        if (StringUtils.hasText(query)) {
            page = referenceRoomService.searchList(query, searchType, pageable);
        } else {
            page = referenceRoomService.list(pageable);
        }

        return ReferenceRoomListResponse.of(page, pageable);
    }

    @GetMapping("/api/referenceroom/list/category")
    @ResponseBody
    public ReferenceRoomListResponse listDataByCategory(
            @RequestParam(name = "query", defaultValue = "") String query,
            @RequestParam(name = "searchType", defaultValue = "TITLE") SearchType searchType,
            @RequestParam("type") List<ReferenceRoomPostType> types, Pageable pageable,
            @AuthenticationPrincipal User user) {

        if (types.stream().anyMatch(type -> type.cannotReadBy(user))) {
            throw new RuntimeException("permission denied");
        }

        Page<ReferenceRoom> page;
        if (StringUtils.hasText(query)) {
            page = referenceRoomService.searchListByCategories(query, searchType, pageable, types);
        } else {
            page = referenceRoomService.listByCategories(pageable, types);
        }

        return ReferenceRoomListResponse.of(page, pageable);
    }

    /* 댓글 */
    @PostMapping("/api/referenceroom/{id}/comment")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    @ResponseBody
    public RfCommentResponse addComment(@PathVariable Long id, @Validated @RequestBody RfCommentRequest request,
                                        @AuthenticationPrincipal User user) {
        RfComment savedComment = referenceRoomService.addComment(id, request, user, OffsetDateTime.now());
        return RfCommentResponse.of(savedComment);
    }

    @DeleteMapping("/api/referenceroom/{id}/comment/{commentId}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void deleteComment(@PathVariable Long id, @PathVariable Long commentId,
                              @AuthenticationPrincipal User user) {
        referenceRoomService.deleteComment(commentId, user, OffsetDateTime.now());
    }


    /* 첨부파일 다운로드 */
    @GetMapping(value = "/api/referenceroom/{id}/file/{filename}")
    public ResponseEntity<FileSystemResource> getFile(
            @PathVariable Long id, @PathVariable String filename,
            @RequestParam(name = "aid", defaultValue = "-1") Long aid,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {

        ReferenceRoom data = referenceRoomService.getData(id);
        if (data.getCategory().cannotReadBy(user)) {
            throw new RuntimeException("permission denied");
        }

        String path = fileStore.getReferenceRoomAttachmentFilePath(Long.toString(id), filename);
        File file = new File(path);
        if (!file.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }

        Attachment attachment = attachmentService.getFile(
                URLDecoder.decode(filename, StandardCharsets.UTF_8), data);
        boolean isImage = Boolean.TRUE.equals(attachment.getIsImage());

        long lastModifiedMillis = file.lastModified();
        String etag = "\"" + file.length() + "-" + lastModifiedMillis + "\"";

        String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        // 다운로드 수 늘리기 (캐시 히트 시 제외)
        if (aid != -1)
            attachmentService.increaseDownloadCountById(aid);

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(Files.probeContentType(Path.of(path)));
        } catch (Exception e) {
            mediaType = MediaType.parseMediaType("application/octet-stream");
        }

        String disposition = isImage ? "inline"
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
    @DeleteMapping(value = "/api/referenceroom/{id}/file/{filename}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void deleteFile(@PathVariable Long id, @PathVariable String filename,
                           @AuthenticationPrincipal User user) throws IOException {
        ReferenceRoom data = referenceRoomService.getData(id);

        if (user.equals(data.getUser()) || !CAN_EDIT_ROLES.contains(user.getRole())) {
            Attachment file = attachmentService.getFile(URLDecoder.decode(filename, StandardCharsets.UTF_8), data);
            attachmentService.deleteAttachment(file, data);
        } else {
            throw new RuntimeException("user not matched");
        }
    }

    /* 추천 비추천 추가 */
    @PostMapping("/api/referenceroom/{id}/likes")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void addLike(@PathVariable Long id, @RequestParam Boolean dislike,
                        @AuthenticationPrincipal User user) {
        if (user == null)
            throw new RuntimeException("user not logged in");

        ReferenceRoom data = referenceRoomService.getData(id);
        likeService.createLike(data, user, dislike);
    }
}
