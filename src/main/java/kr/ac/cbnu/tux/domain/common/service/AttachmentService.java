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
    public Attachment create(MultipartFile multipartFile, ReferenceRoom data) {
        Attachment file = new Attachment();
        file.setFilename(multipartFile.getOriginalFilename());
        file.setPath("/api/referenceroom/" + data.getId() + "/file/" +
                Objects.requireNonNull(multipartFile.getOriginalFilename()).replaceAll("[\\\\/:*?\"<>| ]", "_"));
        file.setIsImage(multipartFile.getContentType().startsWith("image"));
        file.setOrder(data.getAttachments().size() + 1);
        file.setData(data);
        file.setDownloadCount(0L);
        return attachmentRepository.save(file);
    }

    @Transactional
    public Attachment create(MultipartFile multipartFile, Community post) {
        Attachment file = new Attachment();
        file.setFilename(multipartFile.getOriginalFilename());
        file.setPath("/api/community/" + post.getId() + "/file/" +
                Objects.requireNonNull(multipartFile.getOriginalFilename()).replaceAll("[\\\\/:*?\"<>| ]", "_"));
        file.setIsImage(multipartFile.getContentType().startsWith("image"));
        file.setOrder(post.getAttachments().size() + 1);
        file.setPost(post);
        file.setDownloadCount(0L);
        return attachmentRepository.save(file);
    }

    public void increaseDownloadCountById(Long attachmentId) {
        attachmentRepository.increaseDownloadCountById(attachmentId);
    }

    public Optional<Attachment> getFile(String filename, Community post) {
        return attachmentRepository.findByFilenameAndPost(filename, post);
    }

    public Optional<Attachment> getFile(String filename, ReferenceRoom data) {
        return attachmentRepository.findByFilenameAndData(filename, data);
    }

    @Transactional
    public void delete(Attachment file, Community post) throws IOException {
        fileStore.deleteAttachment(COMMUNITY, post.getId().toString(), file);
        post.removeAttachment(file);
        attachmentRepository.delete(file);
    }

    @Transactional
    public void delete(Attachment file, ReferenceRoom data) throws IOException {
        fileStore.deleteAttachment(REFERENCEROOM, data.getId().toString(), file);
        data.removeAttachment(file);
        attachmentRepository.delete(file);
    }


}
