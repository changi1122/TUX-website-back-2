package kr.ac.cbnu.tux.domain.common.service;

import jakarta.transaction.Transactional;
import kr.ac.cbnu.tux.domain.common.entity.Attachment;
import kr.ac.cbnu.tux.domain.common.exception.CommonErrorCode;
import kr.ac.cbnu.tux.domain.common.exception.CommonException;
import kr.ac.cbnu.tux.domain.common.repository.AttachmentRepository;
import kr.ac.cbnu.tux.domain.community.entity.Community;
import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.domain.user.enums.UserRole;
import kr.ac.cbnu.tux.global.utility.FileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.web.util.UriUtils;

import static kr.ac.cbnu.tux.domain.common.enums.AttachmentType.COMMUNITY;
import static kr.ac.cbnu.tux.domain.common.enums.AttachmentType.REFERENCEROOM;

@RequiredArgsConstructor
@Service
public class AttachmentService {

    private static final Map<UserRole, Long> MAX_FILE_SIZE = new EnumMap<>(Map.of(
            UserRole.GUEST,   50L  * 1024 * 1024,   //  50 MB
            UserRole.USER,    200L * 1024 * 1024,   // 200 MB
            UserRole.MANAGER, 3000L * 1024 * 1024,  // 3 GB
            UserRole.ADMIN,   3000L * 1024 * 1024   // 3 GB
    ));

    private final AttachmentRepository attachmentRepository;
    private final FileStore fileStore;

    @Transactional
    public Attachment createAttachment(MultipartFile file, Community post, User user) {
        validateFileSize(file, user);
        String uniqueFilename = resolveUniqueFilename(
                Objects.requireNonNull(file.getOriginalFilename()), post.getAttachments());
        
        String sanitizedFilename = uniqueFilename.replaceAll("[\\\\/:*?\"<>|% ]", "_");
        Attachment attachment = Attachment.builder()
                .filename(uniqueFilename)
                .path("/api/community/" + post.getId() + "/file/" +
                        UriUtils.encodePathSegment(sanitizedFilename, StandardCharsets.UTF_8))
                .isImage(isImageFile(file))
                .order(post.getAttachments().size() + 1)
                .post(post)
                .downloadCount(0L)
                .build();

        return attachmentRepository.save(attachment);
    }

    @Transactional
    public Attachment createAttachment(MultipartFile file, ReferenceRoom data, User user) {
        validateFileSize(file, user);
        String uniqueFilename = resolveUniqueFilename(
                Objects.requireNonNull(file.getOriginalFilename()), data.getAttachments());
        
        String sanitizedFilename = uniqueFilename.replaceAll("[\\\\/:*?\"<>|% ]", "_");
        Attachment attachment = Attachment.builder()
                .filename(uniqueFilename)
                .path("/api/referenceroom/" + data.getId() + "/file/" +
                        UriUtils.encodePathSegment(sanitizedFilename, StandardCharsets.UTF_8))
                .isImage(isImageFile(file))
                .order(data.getAttachments().size() + 1)
                .data(data)
                .downloadCount(0L)
                .build();

        return attachmentRepository.save(attachment);
    }

    public void increaseDownloadCountById(Long attachmentId) {
        attachmentRepository.increaseDownloadCountById(attachmentId);
    }

    public Attachment getFile(String filename, Community post) {
        return attachmentRepository.findByFilenameAndPost(filename, post).orElseThrow();
    }

    public Attachment getFile(String filename, ReferenceRoom data) {
        return attachmentRepository.findByFilenameAndData(filename, data).orElseThrow();
    }

    @Transactional
    public void deleteAttachment(Attachment file, Community post) {
        fileStore.deleteAttachment(COMMUNITY, post.getId().toString(), file);
        post.removeAttachment(file);
        attachmentRepository.delete(file);
    }

    @Transactional
    public void deleteAttachment(Attachment file, ReferenceRoom data) {
        fileStore.deleteAttachment(REFERENCEROOM, data.getId().toString(), file);
        data.removeAttachment(file);
        attachmentRepository.delete(file);
    }

    private void validateFileSize(MultipartFile file, User user) {
        long limit = MAX_FILE_SIZE.getOrDefault(user.getRole(), 0L);
        if (file.getSize() > limit) {
            throw new CommonException(CommonErrorCode.FILE_SIZE_LIMIT_EXCEEDED);
        }
    }

    private String resolveUniqueFilename(String originalFilename, List<Attachment> existingAttachments) {
        Set<String> existingNames = existingAttachments.stream()
                .map(Attachment::getFilename)
                .collect(Collectors.toSet());

        if (!existingNames.contains(originalFilename)) {
            return originalFilename;
        }

        int dotIndex = originalFilename.lastIndexOf('.');
        String baseName = dotIndex >= 0 ? originalFilename.substring(0, dotIndex) : originalFilename;
        String extension = dotIndex >= 0 ? originalFilename.substring(dotIndex) : "";

        int counter = 1;
        String candidate;
        do {
            candidate = baseName + " (" + counter++ + ")" + extension;
        } while (existingNames.contains(candidate));

        return candidate;
    }

    private boolean isImageFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.getContentType() == null)
            return false;
        return multipartFile.getContentType().startsWith("image");
    }
}
