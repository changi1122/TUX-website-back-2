package kr.ac.cbnu.tux.domain.community.service;

import jakarta.transaction.Transactional;
import kr.ac.cbnu.tux.domain.common.entity.Attachment;
import kr.ac.cbnu.tux.domain.community.dto.request.CmCommentRequest;
import kr.ac.cbnu.tux.domain.community.dto.request.CommunityRequest;
import kr.ac.cbnu.tux.domain.community.entity.CmComment;
import kr.ac.cbnu.tux.domain.community.entity.Community;
import kr.ac.cbnu.tux.domain.common.enums.SearchType;
import kr.ac.cbnu.tux.domain.community.enums.CommunityPostType;
import kr.ac.cbnu.tux.domain.community.repository.CmCommentRepository;
import kr.ac.cbnu.tux.domain.common.service.ViewCountService;
import kr.ac.cbnu.tux.domain.community.repository.CommunityRepository;
import kr.ac.cbnu.tux.domain.community.exception.CommunityErrorCode;
import kr.ac.cbnu.tux.domain.community.exception.CommunityException;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.domain.user.enums.UserRole;
import kr.ac.cbnu.tux.global.utility.Sanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CmCommentRepository cmCommentRepository;
    private final Sanitizer sanitizer;
    private final ViewCountService viewCountService;

    public final static List<UserRole> CAN_EDIT_ROLES = List.of(UserRole.ADMIN);
    private final static List<UserRole> CAN_DELETE_ROLES = List.of(UserRole.ADMIN, UserRole.MANAGER);

    /* 파일 업로드 및 글쓰기 */

    @Transactional
    public Community createPost(CommunityPostType type, CommunityRequest request, User user, OffsetDateTime now) {
        Community post = request.toEntity(type);

        if (isSanitizationRequired(post))
            post.setBody(sanitizer.sanitize(post.getBody()));

        post.initializePost(user, now);
        return communityRepository.save(post);
    }

    @Transactional
    public Community createTemporalPostForFile(CommunityPostType type, User user, OffsetDateTime now) {
        // 첨부파일 사전 업로드를 위한 임시 게시물 생성
        Community post = Community.builder()
                .category(type)
                .title("Auto creation by uploading file")
                .body(" ")
                .isDeleted(true)
                .createdDate(now)
                .view(0L)
                .user(user)
                .build();

        return communityRepository.save(post);
    }

    @Transactional
    public void addAttachment(Attachment file, Community post) {
        post.addAttachment(file);
    }

    @Transactional
    public void updateTemporalPost(Long id, CommunityPostType type, CommunityRequest request, User user, OffsetDateTime now) {
        Community post = communityRepository.findById(id).orElseThrow();

        if (!user.equals(post.getUser())) {
            throw new CommunityException(CommunityErrorCode.USER_NOT_MATCHED);
        }

        if (isSanitizationRequired(request))
            post.setBody(sanitizer.sanitize(request.getBody()));
        else
            post.setBody(request.getBody());

        post.updateTemporalPost(type, request.getTitle(), request.getEditorVersion(), now);
    }

    public Community getPost(Long id) {
        return communityRepository.findById(id).orElseThrow();
    }

    @Transactional
    public void updatePost(Long id, CommunityPostType type, CommunityRequest request, User user, OffsetDateTime now) {
        Community post = communityRepository.findByIdAndIsDeletedFalse(id).orElseThrow();

        if (!user.equals(post.getUser()) && !CAN_EDIT_ROLES.contains(user.getRole())) {
            throw new CommunityException(CommunityErrorCode.USER_NOT_MATCHED);
        }

        if (isSanitizationRequired(request))
            post.setBody(sanitizer.sanitize(request.getBody()));
        else
            post.setBody(request.getBody());

        post.updatePost(type, request.getTitle(), request.getEditorVersion(), now);
    }

    @Transactional
    public void deletePost(Long id, User user, OffsetDateTime now) {
        Community post = communityRepository.findByIdAndIsDeletedFalse(id).orElseThrow();

        if (!user.equals(post.getUser()) && !CAN_DELETE_ROLES.contains(user.getRole())) {
            throw new CommunityException(CommunityErrorCode.USER_NOT_MATCHED);
        }

        post.deletePost(now);
    }

    public Community readPost(Long id, User user, String viewerIdentifier) {
        Community post = communityRepository.findById(id).orElseThrow();
        if (post.getIsDeleted() && !user.equals(post.getUser())) // 임시 생성된 글 조회를 위해 본인은 조회 허용
            throw new NoSuchElementException();

        if (!post.getUser().equals(user))
            viewCountService.addView("community", post.getId(), viewerIdentifier);

        return post;
    }


    /* 게시판 리스트 조회 */

    public Page<Community> list(Pageable pageable) {
        return communityRepository.findByIsDeletedFalseOrderByCreatedDateDesc(pageable);
    }

    public Page<Community> searchList(String query, SearchType searchType, Pageable pageable) {
        return communityRepository.searchDsl(query, searchType, null, pageable);
    }

    public Page<Community> listByCategories(Pageable pageable, List<CommunityPostType> types) {
        return communityRepository.findByIsDeletedFalseAndCategoryInOrderByCreatedDateDesc(types, pageable);
    }

    public Page<Community> searchListByCategories(String query, SearchType searchType, Pageable pageable, List<CommunityPostType> types) {
        return communityRepository.searchDsl(query, searchType, types, pageable);
    }

    public Long count() {
        return communityRepository.countByIsDeletedFalse();
    }


    /* 댓글 관련 코드 */

    @Transactional
    public CmComment addComment(Long id, CmCommentRequest request, User user, OffsetDateTime now) {
        Community post = communityRepository.findByIdAndIsDeletedFalse(id).orElseThrow();
        CmComment comment = request.toEntity();
        comment.initializeComment(post, user, now);
        return cmCommentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, User user, OffsetDateTime now) {
        CmComment comment = cmCommentRepository.findById(commentId).orElseThrow();
        if (!comment.getUser().equals(user)) {
            throw new CommunityException(CommunityErrorCode.USER_NOT_MATCHED);
        }
        comment.deleteComment(now);
    }

    private boolean isSanitizationRequired(Community post) {
        return post.getEditorVersion() == null || post.getEditorVersion() != 2;
    }

    private boolean isSanitizationRequired(CommunityRequest request) {
        return request.getEditorVersion() == null || request.getEditorVersion() != 2;
    }
}
