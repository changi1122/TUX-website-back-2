package kr.ac.cbnu.tux.domain.community.service;

import kr.ac.cbnu.tux.domain.community.dto.request.CommunityRequest;
import kr.ac.cbnu.tux.domain.community.entity.Community;
import kr.ac.cbnu.tux.domain.community.enums.CommunityPostType;
import kr.ac.cbnu.tux.domain.community.factory.CommunityFactory;
import kr.ac.cbnu.tux.domain.community.repository.CommunityRepository;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.domain.user.enums.UserRole;
import kr.ac.cbnu.tux.domain.user.repository.UserRepository;
import kr.ac.cbnu.tux.utility.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;

import static kr.ac.cbnu.tux.domain.community.factory.CommunityFactory.createRequest;
import static kr.ac.cbnu.tux.domain.user.factory.UserFactory.createTestUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CommunityServiceTest extends IntegrationTestSupport {

    @Autowired
    CommunityService communityService;
    @Autowired
    CommunityRepository communityRepository;
    @Autowired
    private UserRepository userRepository;

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


}