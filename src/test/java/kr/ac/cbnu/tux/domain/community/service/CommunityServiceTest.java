package kr.ac.cbnu.tux.domain.community.service;

import jakarta.persistence.EntityManager;
import kr.ac.cbnu.tux.domain.common.entity.Attachment;
import kr.ac.cbnu.tux.domain.common.service.AttachmentService;
import kr.ac.cbnu.tux.domain.community.dto.request.CmCommentRequest;
import kr.ac.cbnu.tux.domain.community.dto.request.CommunityRequest;
import kr.ac.cbnu.tux.domain.community.entity.CmComment;
import kr.ac.cbnu.tux.domain.community.entity.Community;
import kr.ac.cbnu.tux.domain.community.enums.CommunityPostType;
import kr.ac.cbnu.tux.domain.community.exception.CommunityException;
import kr.ac.cbnu.tux.domain.community.repository.CmCommentRepository;
import kr.ac.cbnu.tux.domain.community.repository.CommunityRepository;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.domain.user.enums.UserRole;
import kr.ac.cbnu.tux.domain.user.repository.UserRepository;
import kr.ac.cbnu.tux.global.utility.FileStore;
import kr.ac.cbnu.tux.utility.FileUtils;
import kr.ac.cbnu.tux.utility.IntegrationTestSupport;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static kr.ac.cbnu.tux.domain.common.enums.AttachmentType.COMMUNITY;
import static kr.ac.cbnu.tux.domain.community.factory.CommunityFactory.createRequest;
import static kr.ac.cbnu.tux.domain.user.factory.UserFactory.createTestUser;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommunityServiceTest extends IntegrationTestSupport {

    @Autowired
    CommunityService communityService;
    @Autowired
    CommunityRepository communityRepository;
    @Autowired
    CmCommentRepository cmCommentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AttachmentService attachmentService;
    @Autowired
    FileStore fileStore;
    @Autowired
    EntityManager entityManager;

    @Value("${file.dir}")
    private String uploadDir;

    @BeforeEach
    void setUp() {
        FileUtils.createFolderIfNotExists(new File(uploadDir));
    }

    @AfterEach
    void tearDown() {
        FileUtils.deleteFolderContents(new File(uploadDir));
    }

    @Test
    @DisplayName("파일 첨부하지 않고 새로운 글을 작성한다")
    void createPost() {
        // given
        User user = userRepository.save(createTestUser("author", UserRole.USER));
        CommunityRequest request = createRequest("제목", "<p>본문</p>", (short) 1);
        OffsetDateTime now = OffsetDateTime.now();

        // when
        Community post = communityService.createPost(CommunityPostType.FREE, request, user, now);

        // then
        assertThat(post).extracting("title", "body", "editorVersion", "createdDate", "view", "user")
                .contains(request.getTitle(), request.getBody(), request.getEditorVersion(), 0L, user);
    }

    @Test
    @DisplayName("새로운 글을 작성시 XSS 공격 우려 태그를 제거한다")
    void createPost_XSS_sanitize() {
        // given
        User user = userRepository.save(createTestUser("author", UserRole.USER));
        CommunityRequest request = createRequest("제목", "<script>alert(1)</script>", (short) 1);
        OffsetDateTime now = OffsetDateTime.now();

        // when
        Community post = communityService.createPost(CommunityPostType.JOB, request, user, now);

        // then
        assertThat(post).extracting("title", "body", "editorVersion", "createdDate", "view", "user")
                .contains(request.getTitle(), "", request.getEditorVersion(), 0L, user);
    }

    @Test
    @DisplayName("파일 업로드를 위해 임시 생성된 글을 작성 완료한다")
    void updateTemporalPost() throws IOException {
        // 글을 임시 생성하고 파일을 업로드한다

        // given
        User user = userRepository.save(createTestUser("author", UserRole.USER));
        MockMultipartFile file = FileUtils.getUploadFile();
        OffsetDateTime now = OffsetDateTime.now();

        // when
        Community post = communityService.createTemporalPostForFile(CommunityPostType.FREE, user, now);
        Attachment attachment = attachmentService.createAttachment(file, post);
        communityService.addAttachment(attachment, post);
        fileStore.saveAttachment(COMMUNITY, post.getId().toString(), file);

        // then
        Community foundPost = communityRepository.findById(post.getId()).orElseThrow();
        assertThat(foundPost).extracting("category", "user", "createdDate", "isDeleted")
                .contains(CommunityPostType.FREE, user, now, true);
        assertThat(foundPost.getAttachments()).hasSize(1)
                .extracting("filename", "isImage")
                .contains(tuple("sky.jpg", true));

        // 임시 생성된 글의 내용을 작성한다

        // given
        CommunityRequest request = createRequest("제목", "<p>본문</p>", (short) 1);
        OffsetDateTime newCreatedDate = OffsetDateTime.now();

        // when
        communityService.updateTemporalPost(post.getId(), CommunityPostType.NOTICE, request, user, newCreatedDate);

        // then
        foundPost = communityRepository.findById(post.getId()).orElseThrow();
        assertThat(foundPost).extracting("category", "title", "body", "createdDate", "isDeleted")
                .contains(CommunityPostType.NOTICE, request.getTitle(), request.getBody(), newCreatedDate, false);
    }

    @Test
    @DisplayName("글을 수정한다")
    void updatePost() {
        // given
        User user = userRepository.save(createTestUser("author", UserRole.USER));
        CommunityRequest request = createRequest("제목", "<p>본문</p>", (short) 1);
        Community post = communityService.createPost(CommunityPostType.FREE, request, user, OffsetDateTime.now());

        CommunityRequest updateRequest = createRequest("수정", "<p>수정</p>", (short) 2);
        OffsetDateTime now = OffsetDateTime.now();
        User admin = userRepository.save(createTestUser("admin", UserRole.ADMIN));

        // when
        communityService.updatePost(post.getId(), CommunityPostType.JOB, updateRequest, admin, now);
        communityService.updatePost(post.getId(), CommunityPostType.NOTICE, updateRequest, user, now);

        // then
        Community foundPost = communityRepository.findById(post.getId()).orElseThrow();
        assertThat(foundPost).extracting("category", "title", "body", "editedDate", "editorVersion")
                .contains(CommunityPostType.NOTICE, updateRequest.getTitle(), updateRequest.getBody(), now, (short) 2);
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"USER", "MANAGER", "GUEST"})
    @DisplayName("글을 수정할 권한이 없다면 예외를 던진다")
    void updatePost_no_permission(UserRole role) {
        // given
        User user = userRepository.save(createTestUser("author", UserRole.USER));
        CommunityRequest request = createRequest("제목", "<p>본문</p>", (short) 1);
        Community post = communityService.createPost(CommunityPostType.FREE, request, user, OffsetDateTime.now());

        User userWithoutPermission = userRepository.save(createTestUser("userWithoutPermission", role));
        CommunityRequest updateRequest = createRequest("수정", "<p>수정</p>", (short) 2);

        // when then
        assertThatThrownBy(() -> communityService.updatePost(post.getId(), CommunityPostType.NOTICE, updateRequest, userWithoutPermission, OffsetDateTime.now()))
                .isInstanceOf(CommunityException.class)
                .hasMessage("글을 수정할 권한이 없습니다.");
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"USER", "MANAGER", "ADMIN"})
    @DisplayName("글을 삭제한다")
    void deletePost(UserRole role) {
        // given
        User user = userRepository.save(createTestUser("author", UserRole.USER));
        User otherUser = userRepository.save(createTestUser("otherUser", UserRole.USER));
        CommunityRequest request = createRequest("제목", "<p>본문</p>", (short) 1);
        Community post = communityService.createPost(CommunityPostType.FREE, request, user, OffsetDateTime.now());

        OffsetDateTime now = OffsetDateTime.now();
        User actor = (role == UserRole.USER) ? user : userRepository.save(createTestUser(role.toString().toLowerCase(), role));

        // when
        communityService.deletePost(post.getId(), actor, now);

        // then
        Community foundPost = communityRepository.findById(post.getId()).orElseThrow();
        assertThat(foundPost).extracting("isDeleted", "deletedDate")
                .contains(true, now);

        assertThatThrownBy(() -> communityService.readPost(post.getId(), otherUser, "testId"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"USER", "GUEST"})
    @DisplayName("글을 삭제할 권한이 없다면 예외를 던진다")
    void deletePost_no_permission(UserRole role) {
        // given
        User user = userRepository.save(createTestUser("author", UserRole.USER));
        CommunityRequest request = createRequest("제목", "<p>본문</p>", (short) 1);
        Community post = communityService.createPost(CommunityPostType.FREE, request, user, OffsetDateTime.now());

        User userWithoutPermission = userRepository.save(createTestUser("userWithoutPermission", role));

        // when then
        assertThatThrownBy(() -> communityService.deletePost(post.getId(), userWithoutPermission, OffsetDateTime.now()))
                .isInstanceOf(CommunityException.class)
                .hasMessage("글을 수정할 권한이 없습니다.");
    }

    @Test
    @DisplayName("글을 조회하면 조회수가 증가한다")
    void readPost() {
        // given
        User author = userRepository.save(createTestUser("author", UserRole.USER));
        CommunityRequest request = createRequest("제목", "<p>본문</p>", (short) 1);
        Community post = communityService.createPost(CommunityPostType.FREE, request, author, OffsetDateTime.now());

        User reader = userRepository.save(createTestUser("reader", UserRole.USER));

        // when
        communityService.readPost(post.getId(), reader, "testId");

        // then
        entityManager.clear();  // 1차 캐시 비우기
        Community foundPost = communityRepository.findById(post.getId()).orElseThrow();
        assertThat(foundPost).extracting("title", "body", "editorVersion", "user")
                .contains(request.getTitle(), request.getBody(), request.getEditorVersion(), author);
        verify(viewCountService).addView("community", post.getId(), "testId");
    }

    @Test
    @DisplayName("작성자 본인이 글을 조회하면 조회수가 증가하지 않는다")
    void readPost_self_view() {
        // given
        User author = userRepository.save(createTestUser("author", UserRole.USER));
        CommunityRequest request = createRequest("제목", "<p>본문</p>", (short) 1);
        Community post = communityService.createPost(CommunityPostType.FREE, request, author, OffsetDateTime.now());

        // when
        communityService.readPost(post.getId(), author, "testId");

        // then
        entityManager.clear();  // 1차 캐시 비우기
        Community foundPost = communityRepository.findById(post.getId()).orElseThrow();
        assertThat(foundPost.getView()).isEqualTo(0L);
        verify(viewCountService, never()).addView(any(), any(), any());
    }

    @Test
    @DisplayName("댓글을 작성한다")
    void addComment() {
        // given
        User author = userRepository.save(createTestUser("author", UserRole.USER));
        CommunityRequest request = createRequest("제목", "<p>본문</p>", (short) 1);
        Community post = communityService.createPost(CommunityPostType.FREE, request, author, OffsetDateTime.now());

        User commenter = userRepository.save(createTestUser("commenter", UserRole.USER));
        CmCommentRequest commentRequest = CmCommentRequest.builder().body("댓글 내용").build();
        OffsetDateTime now = OffsetDateTime.now();

        // when
        CmComment comment = communityService.addComment(post.getId(), commentRequest, commenter, now);

        // then
        CmComment foundComment = cmCommentRepository.findById(comment.getId()).orElseThrow();
        assertThat(foundComment).extracting("body", "isDeleted", "createdDate", "user")
                .contains("댓글 내용", false, now, commenter);
        assertThat(foundComment.getPost().getId()).isEqualTo(post.getId());
    }

    @Test
    @DisplayName("댓글을 삭제한다")
    void deleteComment() {
        // given
        User author = userRepository.save(createTestUser("author", UserRole.USER));
        CommunityRequest request = createRequest("제목", "<p>본문</p>", (short) 1);
        Community post = communityService.createPost(CommunityPostType.FREE, request, author, OffsetDateTime.now());

        User commenter = userRepository.save(createTestUser("commenter", UserRole.USER));
        CmCommentRequest commentRequest = CmCommentRequest.builder().body("댓글 내용").build();
        CmComment comment = communityService.addComment(post.getId(), commentRequest, commenter, OffsetDateTime.now());

        OffsetDateTime now = OffsetDateTime.now();

        // when
        communityService.deleteComment(comment.getId(), commenter, now);

        // then
        CmComment foundComment = cmCommentRepository.findById(comment.getId()).orElseThrow();
        assertThat(foundComment).extracting("isDeleted", "deletedDate")
                .contains(true, now);
    }

    @Test
    @DisplayName("댓글 작성자가 아니면 삭제할 수 없다")
    void deleteComment_no_permission() {
        // given
        User author = userRepository.save(createTestUser("author", UserRole.USER));
        CommunityRequest request = createRequest("제목", "<p>본문</p>", (short) 1);
        Community post = communityService.createPost(CommunityPostType.FREE, request, author, OffsetDateTime.now());

        User commenter = userRepository.save(createTestUser("commenter", UserRole.USER));
        CmCommentRequest commentRequest = CmCommentRequest.builder().body("댓글 내용").build();
        CmComment comment = communityService.addComment(post.getId(), commentRequest, commenter, OffsetDateTime.now());

        User otherUser = userRepository.save(createTestUser("other", UserRole.USER));

        // when then
        assertThatThrownBy(() -> communityService.deleteComment(comment.getId(), otherUser, OffsetDateTime.now()))
                .isInstanceOf(CommunityException.class)
                .hasMessage("글을 수정할 권한이 없습니다.");
    }
}