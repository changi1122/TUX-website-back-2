package kr.ac.cbnu.tux.domain.referenceroom.service;

import kr.ac.cbnu.tux.domain.common.entity.Attachment;
import kr.ac.cbnu.tux.domain.common.service.AttachmentService;
import kr.ac.cbnu.tux.domain.referenceroom.dto.request.ReferenceRoomRequest;
import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import kr.ac.cbnu.tux.domain.referenceroom.enums.ReferenceRoomPostType;
import kr.ac.cbnu.tux.domain.referenceroom.repository.ReferenceRoomRepository;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.domain.user.enums.UserRole;
import kr.ac.cbnu.tux.domain.user.repository.UserRepository;
import kr.ac.cbnu.tux.global.utility.FileStore;
import kr.ac.cbnu.tux.utility.FileUtils;
import kr.ac.cbnu.tux.utility.IntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;

import static kr.ac.cbnu.tux.domain.common.enums.AttachmentType.REFERENCEROOM;
import static kr.ac.cbnu.tux.domain.referenceroom.factory.ReferenceRoomFactory.createRequest;
import static kr.ac.cbnu.tux.domain.referenceroom.factory.ReferenceRoomFactory.createUpdateRequest;
import static kr.ac.cbnu.tux.domain.user.factory.UserFactory.createTestUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ReferenceRoomServiceTest extends IntegrationTestSupport {

    @Autowired
    ReferenceRoomService referenceRoomService;
    @Autowired
    ReferenceRoomRepository referenceRoomRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AttachmentService attachmentService;
    @Autowired
    FileStore fileStore;

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
    void createData() {
        // given
        User user = userRepository.save(createTestUser("author", UserRole.USER));
        ReferenceRoomRequest request = createRequest("제목", "<p>본문</p>", (short) 1);
        OffsetDateTime now = OffsetDateTime.now();

        // when
        ReferenceRoom data = referenceRoomService.createData(ReferenceRoomPostType.STUDY, request, user, now);

        // then
        assertThat(data).extracting("title", "body", "editorVersion", "createdDate", "view", "user")
                .contains(request.getTitle(), request.getBody(), request.getEditorVersion(), 0L, user);
    }

    @Test
    @DisplayName("새로운 글을 작성시 XSS 공격 우려 태그를 제거한다")
    void createData_XSS_sanitize() {
        // given
        User user = userRepository.save(createTestUser("author", UserRole.USER));
        ReferenceRoomRequest request = createRequest("제목", "<script>alert(1)</script>", (short) 1);
        OffsetDateTime now = OffsetDateTime.now();

        // when
        ReferenceRoom data = referenceRoomService.createData(ReferenceRoomPostType.EXAM, request, user, now);

        // then
        assertThat(data).extracting("title", "body", "editorVersion", "createdDate", "view", "user")
                .contains(request.getTitle(), "", request.getEditorVersion(), 0L, user);
    }

    @Test
    @DisplayName("파일 업로드를 위해 임시 생성된 글을 작성 완료한다")
    void updateTemporalData() throws IOException {
        // 글을 임시 생성하고 파일을 업로드한다

        // given
        User user = userRepository.save(createTestUser("author", UserRole.USER));
        MockMultipartFile file = FileUtils.getUploadFile();
        OffsetDateTime now = OffsetDateTime.now();

        // when
        ReferenceRoom data = referenceRoomService.createTemporalDataForFile(ReferenceRoomPostType.STUDY, user, now);
        Attachment attachment = attachmentService.createAttachment(file, data);
        referenceRoomService.addAttachment(attachment, data);
        fileStore.saveAttachment(REFERENCEROOM, data.getId().toString(), file);

        // then
        ReferenceRoom foundData = referenceRoomRepository.findById(data.getId()).orElseThrow();
        assertThat(foundData).extracting("category", "user", "createdDate", "isDeleted")
                .contains(ReferenceRoomPostType.STUDY, user, now, true);

        // 임시 생성된 글의 내용을 작성한다

        // given
        ReferenceRoomRequest request = createRequest("제목", "<p>본문</p>", (short) 1);
        OffsetDateTime newCreatedDate = OffsetDateTime.now();

        // when
        referenceRoomService.updateTemporalData(data.getId(), ReferenceRoomPostType.GALLERY, request, user, newCreatedDate);

        // then
        foundData = referenceRoomRepository.findById(data.getId()).orElseThrow();
        assertThat(foundData).extracting("category", "title", "body", "lecture", "semester", "professor", "createdDate", "isDeleted")
                .contains(ReferenceRoomPostType.GALLERY, request.getTitle(), request.getBody(), request.getLecture(), request.getSemester(), request.getProfessor(), newCreatedDate, false);
    }

    @Test
    @DisplayName("글을 수정한다")
    void updateData() {
        // given
        User user = userRepository.save(createTestUser("author", UserRole.USER));
        ReferenceRoomRequest request = createRequest("제목", "<p>본문</p>", (short) 1);
        ReferenceRoom data = referenceRoomService.createData(ReferenceRoomPostType.STUDY, request, user, OffsetDateTime.now());

        ReferenceRoomRequest updateRequest = createUpdateRequest("수정", "<p>수정</p>", (short) 2);
        OffsetDateTime now = OffsetDateTime.now();
        User admin = userRepository.save(createTestUser("admin", UserRole.ADMIN));

        // when
        referenceRoomService.updateData(data.getId(), ReferenceRoomPostType.GALLERY, updateRequest, admin, now);
        referenceRoomService.updateData(data.getId(), ReferenceRoomPostType.EXAM, updateRequest, user, now);

        // then
        ReferenceRoom foundData = referenceRoomRepository.findById(data.getId()).orElseThrow();
        assertThat(foundData).extracting("category", "title", "body", "isAnonymized", "lecture", "semester", "professor", "editedDate", "editorVersion")
                .contains(ReferenceRoomPostType.EXAM, updateRequest.getTitle(), updateRequest.getBody(), true,
                        updateRequest.getLecture(), updateRequest.getSemester(), updateRequest.getProfessor(), now, (short) 2);
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"USER", "MANAGER", "GUEST"})
    @DisplayName("글을 수정할 권한이 없다면 예외를 던진다")
    void updateData_no_permission(UserRole role) {
        // given
        User user = userRepository.save(createTestUser("author", UserRole.USER));
        ReferenceRoomRequest request = createRequest("제목", "<p>본문</p>", (short) 1);
        ReferenceRoom data = referenceRoomService.createData(ReferenceRoomPostType.STUDY, request, user, OffsetDateTime.now());

        User userWithoutPermission = userRepository.save(createTestUser("userWithoutPermission", role));
        ReferenceRoomRequest updateRequest = createUpdateRequest("수정", "<p>수정</p>", (short) 2);

        // when then
        assertThatThrownBy(() -> referenceRoomService.updateData(data.getId(), ReferenceRoomPostType.GALLERY, updateRequest, userWithoutPermission, OffsetDateTime.now()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("user not matched");
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"USER", "MANAGER", "ADMIN"})
    void deleteData(UserRole role) {
        // given
        User user = userRepository.save(createTestUser("author", UserRole.USER));
        ReferenceRoomRequest request = createRequest("제목", "<p>본문</p>", (short) 1);
        ReferenceRoom data = referenceRoomService.createData(ReferenceRoomPostType.STUDY, request, user, OffsetDateTime.now());

        OffsetDateTime now = OffsetDateTime.now();
        User actor = (role == UserRole.USER) ? user : userRepository.save(createTestUser(role.toString().toLowerCase(), role));

        // when
        referenceRoomService.deleteData(data.getId(), actor, now);

        // then
        ReferenceRoom foundData = referenceRoomRepository.findById(data.getId()).orElseThrow();
        assertThat(foundData).extracting("isDeleted", "deletedDate")
                .contains(true, now);
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"USER", "GUEST"})
    @DisplayName("글을 삭제할 권한이 없다면 예외를 던진다")
    void deleteData_no_permission(UserRole role) {
        // given
        User user = userRepository.save(createTestUser("author", UserRole.USER));
        ReferenceRoomRequest request = createRequest("제목", "<p>본문</p>", (short) 1);
        ReferenceRoom data = referenceRoomService.createData(ReferenceRoomPostType.STUDY, request, user, OffsetDateTime.now());

        User userWithoutPermission = userRepository.save(createTestUser("userWithoutPermission", role));

        // when then
        assertThatThrownBy(() -> referenceRoomService.deleteData(data.getId(), userWithoutPermission, OffsetDateTime.now()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("user not matched");
    }
}