package kr.ac.cbnu.tux.domain.common.service;

import jakarta.transaction.Transactional;
import kr.ac.cbnu.tux.domain.common.entity.Attachment;
import kr.ac.cbnu.tux.domain.common.repository.AttachmentRepository;
import kr.ac.cbnu.tux.domain.community.entity.Community;
import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import kr.ac.cbnu.tux.global.utility.FileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static kr.ac.cbnu.tux.domain.common.enums.AttachmentType.COMMUNITY;
import static kr.ac.cbnu.tux.domain.common.enums.AttachmentType.REFERENCEROOM;

@RequiredArgsConstructor
@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final FileStore fileStore;

    @Transactional
    public Attachment createAttachment(MultipartFile file, Community post) {
        String uniqueFilename = resolveUniqueFilename(
                Objects.requireNonNull(file.getOriginalFilename()), post.getAttachments());
        
        Attachment attachment = Attachment.builder()
                .filename(uniqueFilename)
                .path("/api/community/" + post.getId() + "/file/" +
                        uniqueFilename.replaceAll("[\\\\/:*?\"<>| ]", "_"))
                .isImage(isImageFile(file))
                .order(post.getAttachments().size() + 1)
                .post(post)
                .downloadCount(0L)
                .build();

        return attachmentRepository.save(attachment);
    }

    @Transactional
    public Attachment createAttachment(MultipartFile file, ReferenceRoom data) {
        String uniqueFilename = resolveUniqueFilename(
                Objects.requireNonNull(file.getOriginalFilename()), data.getAttachments());
        
        Attachment attachment = Attachment.builder()
                .filename(uniqueFilename)
                .path("/api/referenceroom/" + data.getId() + "/file/" +
                        uniqueFilename.replaceAll("[\\\\/:*?\"<>| ]", "_"))
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
