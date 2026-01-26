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

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import static kr.ac.cbnu.tux.domain.common.enums.AttachmentType.COMMUNITY;
import static kr.ac.cbnu.tux.domain.common.enums.AttachmentType.REFERENCEROOM;

@RequiredArgsConstructor
@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final FileStore fileStore;

    @Transactional
    public Attachment createAttachment(MultipartFile file, Community post) {
        Attachment attachment = Attachment.builder()
                .filename(file.getOriginalFilename())
                .path("/api/community/" + post.getId() + "/file/" +
                        Objects.requireNonNull(file.getOriginalFilename()).replaceAll("[\\\\/:*?\"<>| ]", "_"))
                .isImage(isImageFile(file))
                .order(post.getAttachments().size() + 1)
                .post(post)
                .downloadCount(0L)
                .build();

        return attachmentRepository.save(attachment);
    }

    @Transactional
    public Attachment createAttachment(MultipartFile file, ReferenceRoom data) {
        Attachment attachment = Attachment.builder()
                .filename(file.getOriginalFilename())
                .path("/api/referenceroom/" + data.getId() + "/file/" +
                        Objects.requireNonNull(file.getOriginalFilename()).replaceAll("[\\\\/:*?\"<>| ]", "_"))
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
    public void deleteAttachment(Attachment file, Community post) throws IOException {
        fileStore.deleteAttachment(COMMUNITY, post.getId().toString(), file);
        post.removeAttachment(file);
        attachmentRepository.delete(file);
    }

    @Transactional
    public void deleteAttachment(Attachment file, ReferenceRoom data) throws IOException {
        fileStore.deleteAttachment(REFERENCEROOM, data.getId().toString(), file);
        data.removeAttachment(file);
        attachmentRepository.delete(file);
    }

    private boolean isImageFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.getContentType() == null)
            return false;
        return multipartFile.getContentType().startsWith("image");
    }
}
