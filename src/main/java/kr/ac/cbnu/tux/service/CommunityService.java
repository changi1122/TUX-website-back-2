package kr.ac.cbnu.tux.service;

import jakarta.transaction.Transactional;
import kr.ac.cbnu.tux.entity.*;
import kr.ac.cbnu.tux.enums.CommunityPostType;
import kr.ac.cbnu.tux.enums.UserRole;
import kr.ac.cbnu.tux.repository.CmCommentRepository;
import kr.ac.cbnu.tux.repository.CommunityRepository;
import kr.ac.cbnu.tux.utility.Sanitizer;
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
    public void createWithoutFileUpload(CommunityPostType type, Community post, User user) {
        if (isSanitizationRequired(post))
            post.setBody(sanitizer.sanitize(post.getBody()));

        post.setCategory(type);
        post.setCreatedDate(OffsetDateTime.now());
        post.setIsDeleted(false);
        post.setView(0L);
        post.setUser(user);
        communityRepository.save(post);
    }

    @Transactional
    public Community temporalCreate(CommunityPostType type, User user) {
        // 첨부파일 사전 업로드
        Community post = new Community();
        post.setCategory(type);
        post.setTitle("Auto creation by uploading file");
        post.setBody(" ");
        post.setIsDeleted(true);
        post.setCreatedDate(OffsetDateTime.now());
        post.setView(0L);
        post.setUser(user);

        return communityRepository.save(post);
    }

    @Transactional
    public void addAttachment(Attachment file, Community post) {
        post.addAttachment(file);
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

    public Optional<Community> getData(Long id) {
        return communityRepository.findById(id);
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
