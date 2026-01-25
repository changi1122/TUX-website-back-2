package kr.ac.cbnu.tux.domain.referenceroom.service;

import kr.ac.cbnu.tux.domain.referenceroom.dto.request.ReferenceRoomRequest;
import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import kr.ac.cbnu.tux.domain.referenceroom.enums.ReferenceRoomPostType;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.domain.user.enums.UserRole;
import kr.ac.cbnu.tux.domain.user.repository.UserRepository;
import kr.ac.cbnu.tux.utility.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;

import static kr.ac.cbnu.tux.domain.referenceroom.factory.ReferenceRoomFactory.createRequest;
import static kr.ac.cbnu.tux.domain.user.factory.UserFactory.createTestUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ReferenceRoomServiceTest extends IntegrationTestSupport {

    @Autowired
    private ReferenceRoomService referenceRoomService;
    @Autowired
    private UserRepository userRepository;

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
}