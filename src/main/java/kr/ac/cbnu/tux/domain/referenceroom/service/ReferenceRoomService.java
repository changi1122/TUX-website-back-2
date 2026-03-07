package kr.ac.cbnu.tux.domain.referenceroom.service;


import jakarta.transaction.Transactional;
import kr.ac.cbnu.tux.domain.common.entity.Attachment;
import kr.ac.cbnu.tux.domain.common.enums.SortType;
import kr.ac.cbnu.tux.domain.referenceroom.dto.request.ReferenceRoomRequest;
import kr.ac.cbnu.tux.domain.referenceroom.dto.request.RfCommentRequest;
import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import kr.ac.cbnu.tux.domain.referenceroom.entity.RfComment;
import kr.ac.cbnu.tux.domain.common.enums.SearchType;
import kr.ac.cbnu.tux.domain.referenceroom.enums.ReferenceRoomPostType;
import kr.ac.cbnu.tux.domain.common.service.ViewCountService;
import kr.ac.cbnu.tux.domain.referenceroom.repository.ReferenceRoomRepository;
import kr.ac.cbnu.tux.domain.referenceroom.repository.RfCommentRepository;
import kr.ac.cbnu.tux.domain.referenceroom.exception.ReferenceRoomErrorCode;
import kr.ac.cbnu.tux.domain.referenceroom.exception.ReferenceRoomException;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.domain.user.enums.UserRole;
import kr.ac.cbnu.tux.global.utility.Sanitizer;
import kr.ac.cbnu.tux.global.utility.ScoreUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class ReferenceRoomService {

    private final ReferenceRoomRepository referenceRoomRepository;
    private final RfCommentRepository rfCommentRepository;
    private final Sanitizer sanitizer;
    private final ViewCountService viewCountService;

    public final static List<UserRole> CAN_EDIT_ROLES = List.of(UserRole.ADMIN);
    private final static List<UserRole> CAN_DELETE_ROLES = List.of(UserRole.ADMIN, UserRole.MANAGER);

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
                .totalLikes(0L)
                .totalDislikes(0L)
                .totalComments(0L)
                .score(ScoreUtils.calculateInitialScore(now))
                .isAnonymized(true)
                .user(user)
                .build();

        return referenceRoomRepository.save(data);
    }

    @Transactional
    public void addAttachment(Attachment file, ReferenceRoom data) {
        data.addAttachment(file);
    }

    @Transactional
    public void updateTemporalData(Long id, ReferenceRoomPostType type, ReferenceRoomRequest request, User user, OffsetDateTime now) {
        ReferenceRoom data = referenceRoomRepository.findById(id).orElseThrow();

        if (!user.equals(data.getUser())) {
            throw new ReferenceRoomException(ReferenceRoomErrorCode.USER_NOT_MATCHED);
        }

        if (isSanitizationRequired(request))
            data.setBody(sanitizer.sanitize(request.getBody()));
        else
            data.setBody(request.getBody());

        data.updateTemporalData(
                type,
                request.getTitle(),
                request.getEditorVersion(),
                request.getIsAnonymized(),
                request.getLecture(),
                request.getSemester(),
                request.getProfessor(),
                now
        );
    }

    public ReferenceRoom getData(Long id) {
        return referenceRoomRepository.findById(id).orElseThrow();
    }

    /* 글 수정 */
    @Transactional
    public void updateData(Long id, ReferenceRoomPostType type, ReferenceRoomRequest request, User user, OffsetDateTime now) {
        ReferenceRoom data = referenceRoomRepository.findByIdAndIsDeletedFalse(id).orElseThrow();

        if (!user.equals(data.getUser()) && !CAN_EDIT_ROLES.contains(user.getRole())) {
            throw new ReferenceRoomException(ReferenceRoomErrorCode.USER_NOT_MATCHED);
        }

        if (isSanitizationRequired(request))
            data.setBody(sanitizer.sanitize(request.getBody()));
        else
            data.setBody(request.getBody());

        data.updateData(
                type,
                request.getTitle(),
                request.getEditorVersion(),
                request.getIsAnonymized(),
                request.getLecture(),
                request.getSemester(),
                request.getProfessor(),
                now
        );
    }

    /* 글 삭제 */
    @Transactional
    public void deleteData(Long id, User user, OffsetDateTime now) {
        ReferenceRoom data = referenceRoomRepository.findByIdAndIsDeletedFalse(id).orElseThrow();

        if (!user.equals(data.getUser()) && !CAN_DELETE_ROLES.contains(user.getRole())) {
            throw new ReferenceRoomException(ReferenceRoomErrorCode.USER_NOT_MATCHED);
        }

        data.deleteData(now);
    }

    /* 글 조회 */
    public ReferenceRoom readData(Long id, User user, String viewerIdentifier) {
        ReferenceRoom data = referenceRoomRepository.findById(id).orElseThrow();
        if (data.getIsDeleted() && !user.equals(data.getUser())) // 임시 생성된 글 조회를 위해 본인은 조회 허용
            throw new NoSuchElementException();

        if (data.getCategory().cannotReadBy(user))
            throw new ReferenceRoomException(ReferenceRoomErrorCode.PERMISSION_DENIED);

        if (!data.getUser().equals(user))
            viewCountService.addView("referenceroom", data.getId(), viewerIdentifier);

        return data;
    }


    /* 자료실 리스트 조회 */
    public Page<ReferenceRoom> list(String query, SearchType searchType, SortType sortType, Pageable pageable) {
        return referenceRoomRepository.findAllDsl(null, query, searchType, sortType, pageable);
    }

    public Page<ReferenceRoom> listByCategories(List<ReferenceRoomPostType> categories, String query,
                                                SearchType searchType, SortType sortType, Pageable pageable) {
        return referenceRoomRepository.findAllDsl(categories, query, searchType, sortType, pageable);
    }

    /* 댓글 관련 코드 */

    @Transactional
    public RfComment addComment(Long dataId, RfCommentRequest request, User user, OffsetDateTime now) {
        ReferenceRoom data = referenceRoomRepository.findByIdAndIsDeletedFalseWithLock(dataId)
                .orElseThrow(() -> new ReferenceRoomException(ReferenceRoomErrorCode.NOT_FOUND));
        data.createComment();
        referenceRoomRepository.save(data);

        RfComment comment = request.toEntity();
        comment.initializeComment(data, user, now);
        return rfCommentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long dataId, Long commentId, User user, OffsetDateTime now) {
        ReferenceRoom data = referenceRoomRepository.findByIdAndIsDeletedFalseWithLock(dataId)
                .orElseThrow(() -> new ReferenceRoomException(ReferenceRoomErrorCode.NOT_FOUND));

        RfComment comment = rfCommentRepository.findById(commentId).orElseThrow();
        if (!comment.getUser().equals(user)) {
            throw new ReferenceRoomException(ReferenceRoomErrorCode.USER_NOT_MATCHED);
        }
        comment.deleteComment(now);

        data.deleteComment();
        referenceRoomRepository.save(data);
    }

    private boolean isSanitizationRequired(ReferenceRoom data) {
        return data.getEditorVersion() == null || data.getEditorVersion() != 2;
    }

    private boolean isSanitizationRequired(ReferenceRoomRequest request) {
        return request.getEditorVersion() == null || request.getEditorVersion() != 2;
    }
}
