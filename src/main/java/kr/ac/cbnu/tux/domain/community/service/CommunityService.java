package kr.ac.cbnu.tux.domain.community.service;

import jakarta.transaction.Transactional;
import kr.ac.cbnu.tux.domain.common.entity.Attachment;
import kr.ac.cbnu.tux.domain.community.dto.request.CommunityRequest;
import kr.ac.cbnu.tux.domain.community.entity.CmComment;
import kr.ac.cbnu.tux.domain.community.entity.Community;
import kr.ac.cbnu.tux.domain.community.enums.CommunityPostType;
import kr.ac.cbnu.tux.domain.community.repository.CmCommentRepository;
import kr.ac.cbnu.tux.domain.community.repository.CommunityRepository;
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
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CmCommentRepository cmCommentRepository;
    private final Sanitizer sanitizer;

    /* 파일 업로드 및 글쓰기 */

    @Transactional
    public Community createPost(CommunityPostType type, CommunityRequest request, User user, OffsetDateTime now) {
        Community post = request.toEntity(type);

        if (isSanitizationRequired(post))
            post.setBody(sanitizer.sanitize(post.getBody()));

        post.initializePost(now, user);
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
        communityRepository.save(post);
    }

    @Transactional
    public void updateAfterTemporalCreate(Long id, Community updated, User user) throws Exception {
        Community post = communityRepository.findById(id).orElseThrow();
        if (user.getId().equals(post.getUser().getId())) {
            if (isSanitizationRequired(updated))
                post.setBody(sanitizer.sanitize(updated.getBody()));
            else
                post.setBody(updated.getBody());

            post.setTitle(updated.getTitle());
            post.setIsDeleted(false);
            post.setEditorVersion(updated.getEditorVersion());
        }
        else {
            throw new Exception("user not matched");
        }
    }

    public Community getPost(Long id) {
        return communityRepository.findById(id).orElseThrow();
    }


    @Transactional
    public void update(Long id, CommunityPostType updatedCategory, Community updated, User user) throws Exception {
        Community post = communityRepository.findById(id).orElseThrow();

        if (post.getUser().getId().equals(user.getId()) || user.getRole() == UserRole.ADMIN) {
            if (isSanitizationRequired(updated))
                post.setBody(sanitizer.sanitize(updated.getBody()));
            else
                post.setBody(updated.getBody());

            post.setCategory(updatedCategory);
            post.setTitle(updated.getTitle());
            post.setEditedDate(OffsetDateTime.now());
            post.setEditorVersion(updated.getEditorVersion());
        } else {
            throw new Exception("user not matched");
        }
    }

    @Transactional
    public void delete(Long id, User user) throws Exception {
        Community post = communityRepository.findById(id).orElseThrow();
        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.MANAGER ||
                user.getId().equals(post.getUser().getId())) {
            post.setIsDeleted(true);
            post.setDeletedDate(OffsetDateTime.now());
        } else {
            throw new Exception("user not matched");
        }
    }

    public Community read(Long id, User user) {
        Community post = communityRepository.findById(id).orElseThrow();

        if (user == null || user != post.getUser())
            communityRepository.updateViewById(id);

        return post;
    }


    /* 게시판 리스트 조회 */

    public Page<Community> list(Pageable pageable) {
        return communityRepository.findByIsDeletedFalseOrderByCreatedDateDesc(pageable);
    }

    public Page<Community> searchList(String query, Pageable pageable) {
        return communityRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseOrderByCreatedDateDesc(query, pageable);
    }

    public Page<Community> listByCategory(Pageable pageable, CommunityPostType type) {
        return communityRepository.findByIsDeletedFalseAndCategoryOrderByCreatedDateDesc(type, pageable);
    }

    public Page<Community> listByCategories(Pageable pageable, List<CommunityPostType> types) {
        return communityRepository.findByIsDeletedFalseAndCategoryInOrderByCreatedDateDesc(types, pageable);
    }

    public Page<Community> searchListByCategory(String query, Pageable pageable, CommunityPostType type) {
        return communityRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseAndCategoryOrderByCreatedDateDesc(query, type, pageable);
    }

    public Page<Community> searchListByCategories(String query, Pageable pageable, List<CommunityPostType> types) {
        return communityRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseAndCategoryInOrderByCreatedDateDesc(query, types, pageable);
    }

    public List<Community> listAll() {
        return communityRepository.findAllByIsDeletedFalse();
    }

    public Long count() {
        return communityRepository.countByIsDeletedFalse();
    }


    /* 댓글 관련 코드 */

    @Transactional
    public CmComment addComment(Long id, CmComment comment, User user) {
        Community post = communityRepository.findById(id).orElseThrow();

        comment.setCreatedDate(OffsetDateTime.now());
        comment.setIsDeleted(false);
        comment.setPost(post);
        comment.setUser(user);
        return cmCommentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, User user) throws Exception {
        CmComment comment = cmCommentRepository.findById(commentId).orElseThrow();
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new Exception("user not matched");
        }
        comment.setIsDeleted(true);
        comment.setDeletedDate(OffsetDateTime.now());
    }

    private boolean isSanitizationRequired(Community post) {
        return post.getEditorVersion() == null || post.getEditorVersion() != 2;
    }
}
