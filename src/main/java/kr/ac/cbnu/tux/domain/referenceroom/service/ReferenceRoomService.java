package kr.ac.cbnu.tux.domain.referenceroom.service;


import jakarta.transaction.Transactional;
import kr.ac.cbnu.tux.domain.common.entity.Attachment;
import kr.ac.cbnu.tux.domain.referenceroom.dto.request.ReferenceRoomRequest;
import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import kr.ac.cbnu.tux.domain.referenceroom.entity.RfComment;
import kr.ac.cbnu.tux.domain.referenceroom.enums.ReferenceRoomPostType;
import kr.ac.cbnu.tux.domain.referenceroom.repository.ReferenceRoomRepository;
import kr.ac.cbnu.tux.domain.referenceroom.repository.RfCommentRepository;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.domain.user.enums.UserRole;
import kr.ac.cbnu.tux.global.utility.Sanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ReferenceRoomService {

    private final ReferenceRoomRepository referenceRoomRepository;
    private final RfCommentRepository rfCommentRepository;
    private final Sanitizer sanitizer;


    /* 파일 업로드 및 글쓰기 */

    @Transactional
    public ReferenceRoom createData(ReferenceRoomPostType type, ReferenceRoomRequest request, User user, OffsetDateTime now) {
        ReferenceRoom data = request.toEntity(type);

        if (isSanitizationRequired(data))
            data.setBody(sanitizer.sanitize(data.getBody()));

        data.initializeData(user, now);
        return referenceRoomRepository.save(data);
    }

    @Transactional
    public ReferenceRoom createTemporalDataForFile(ReferenceRoomPostType type, User user, OffsetDateTime now) {
        // 첨부파일 사전 업로드를 위한 임시 게시물 생성
        ReferenceRoom data = ReferenceRoom.builder()
                .category(type)
                .title("Auto creation by uploading file")
                .body(" ")
                .isDeleted(true)
                .createdDate(now)
                .view(0L)
                .isAnonymized(true)
                .user(user)
                .build();

        return referenceRoomRepository.save(data);
    }

    @Transactional
    public void addAttachment(Attachment file, ReferenceRoom data) {
        data.addAttachment(file);
        referenceRoomRepository.save(data);
    }

    @Transactional
    public void updateAfterTemporalCreate(Long id, ReferenceRoom updated, User user) throws Exception {
        ReferenceRoom data = referenceRoomRepository.findById(id).orElseThrow();
        if (user.getId().equals(data.getUser().getId())) {
            if (isSanitizationRequired(updated))
                data.setBody(sanitizer.sanitize(updated.getBody()));
            else
                data.setBody(updated.getBody());

            data.setTitle(updated.getTitle());
            data.setEditorVersion(updated.getEditorVersion());
            data.setIsDeleted(false);
            data.setIsAnonymized(updated.getIsAnonymized());
            data.setLecture(updated.getLecture());
            data.setSemester(updated.getSemester());
            data.setProfessor(updated.getProfessor());
        }
        else {
            throw new Exception("user not matched");
        }
    }

    public ReferenceRoom getData(Long id) {
        return referenceRoomRepository.findById(id).orElseThrow();
    }

    /* 글 수정 */
    @Transactional
    public void update(Long id, ReferenceRoomPostType updatedCategory, ReferenceRoom updated, User user) throws Exception {
        ReferenceRoom data = referenceRoomRepository.findById(id).orElseThrow();

        if (data.getUser().getId().equals(user.getId()) || user.getRole() == UserRole.ADMIN) {
            if (isSanitizationRequired(updated))
                data.setBody(sanitizer.sanitize(updated.getBody()));
            else
                data.setBody(updated.getBody());

            data.setCategory(updatedCategory);
            data.setTitle(updated.getTitle());
            data.setEditorVersion(updated.getEditorVersion());
            data.setIsAnonymized(updated.getIsAnonymized());
            data.setLecture(updated.getLecture());
            data.setSemester(updated.getSemester());
            data.setProfessor(updated.getProfessor());
            data.setEditedDate(OffsetDateTime.now());
        } else {
            throw new Exception("user not matched");
        }
    }

    /* 글 삭제 */
    @Transactional
    public void delete(Long id, User user) throws Exception {
        ReferenceRoom data = referenceRoomRepository.findById(id).orElseThrow();
        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.MANAGER ||
                user.getId().equals(data.getUser().getId())) {
            data.setIsDeleted(true);
            data.setDeletedDate(OffsetDateTime.now());
        } else {
            throw new Exception("user not matched");
        }
    }

    /* 글 조회 */
    public ReferenceRoom read(Long id, User user) {
        ReferenceRoom data = referenceRoomRepository.findById(id).orElseThrow();

        if (user == null || user != data.getUser())
            referenceRoomRepository.updateViewById(id);

        return data;
    }


    /* 자료실 리스트 조회 */
    public Page<ReferenceRoom> list(Pageable pageable) {
        return referenceRoomRepository.findByIsDeletedFalseOrderByCreatedDateDesc(pageable);
    }

    public Page<ReferenceRoom> searchList(String query, Pageable pageable) {
        return referenceRoomRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndLectureContainingIgnoreCaseOrIsDeletedFalseAndProfessorContainingIgnoreCaseOrderByCreatedDateDesc(
                query, query, query, pageable
        );
    }

    public Page<ReferenceRoom> listByCategory(Pageable pageable, ReferenceRoomPostType type) {
        return referenceRoomRepository.findByIsDeletedFalseAndCategoryOrderByCreatedDateDesc(type, pageable);
    }

    public Page<ReferenceRoom> listByCategories(Pageable pageable, List<ReferenceRoomPostType> types) {
        return referenceRoomRepository.findByIsDeletedFalseAndCategoryInOrderByCreatedDateDesc(types, pageable);
    }

    public Page<ReferenceRoom> searchListByCategory(String query, Pageable pageable, ReferenceRoomPostType type) {
        return referenceRoomRepository.findByIsDeletedFalseAndCategoryAndTitleContainingIgnoreCaseOrIsDeletedFalseAndCategoryAndLectureContainingIgnoreCaseOrIsDeletedFalseAndCategoryAndProfessorContainingIgnoreCaseOrderByCreatedDateDesc(
                type, query, type, query, type, query, pageable
        );
    }

    public Page<ReferenceRoom> searchListByCategories(String query, Pageable pageable, List<ReferenceRoomPostType> types) {
        return referenceRoomRepository.findByIsDeletedFalseAndCategoryInAndTitleContainingIgnoreCaseOrIsDeletedFalseAndCategoryInAndLectureContainingIgnoreCaseOrIsDeletedFalseAndCategoryInAndProfessorContainingIgnoreCaseOrderByCreatedDateDesc(
                types, query, types, query, types, query, pageable
        );
    }

    public Long count() {
        return referenceRoomRepository.countByIsDeletedFalse();
    }

    /* 댓글 관련 코드 */

    @Transactional
    public RfComment addComment(Long id, RfComment comment, User user) {
        ReferenceRoom data = referenceRoomRepository.findById(id).orElseThrow();
        comment.setCreatedDate(OffsetDateTime.now());
        comment.setIsDeleted(false);
        comment.setData(data);
        comment.setUser(user);
        return rfCommentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, User user)  throws Exception {
        RfComment comment = rfCommentRepository.findById(commentId).orElseThrow();
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new Exception("user not matched");
        }
        comment.setIsDeleted(true);
        comment.setDeletedDate(OffsetDateTime.now());
    }

    private boolean isSanitizationRequired(ReferenceRoom data) {
        return data.getEditorVersion() == null || data.getEditorVersion() != 2;
    }
}
