package kr.ac.cbnu.tux.domain.common.service;

import kr.ac.cbnu.tux.domain.common.entity.Like;
import kr.ac.cbnu.tux.domain.common.repository.LikeRepository;
import kr.ac.cbnu.tux.domain.community.dto.request.CommunityRequest;
import kr.ac.cbnu.tux.domain.community.entity.Community;
import kr.ac.cbnu.tux.domain.community.enums.CommunityPostType;
import kr.ac.cbnu.tux.domain.community.service.CommunityService;
import kr.ac.cbnu.tux.domain.referenceroom.dto.request.ReferenceRoomRequest;
import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import kr.ac.cbnu.tux.domain.referenceroom.enums.ReferenceRoomPostType;
import kr.ac.cbnu.tux.domain.common.exception.CommonException;
import kr.ac.cbnu.tux.domain.referenceroom.service.ReferenceRoomService;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LikeServiceTest extends IntegrationTestSupport {

    @Autowired
    LikeService likeService;
    @Autowired
    LikeRepository likeRepository;
    @Autowired
    CommunityService communityService;
    @Autowired
    ReferenceRoomService referenceRoomService;
    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("커뮤니티 글에 추천을 추가한다")
    void createLike_community() {
        // given
        User author = userRepository.save(createTestUser("author", UserRole.USER));
        CommunityRequest request = createRequest("제목", "<p>본문</p>", (short) 1);
        Community post = communityService.createPost(CommunityPostType.FREE, request, author, OffsetDateTime.now());

        User liker = userRepository.save(createTestUser("liker", UserRole.USER));

        // when
        likeService.createLikeOnCommunity(post.getId(), liker, false, OffsetDateTime.now());

        // then
        Like like = likeRepository.findAll().get(0);
        assertThat(like).extracting("post", "user", "dislike")
                .contains(post, liker, false);
    }

    @Test
    @DisplayName("커뮤니티 글에 비추천을 추가한다")
    void createDislike_community() {
        // given
        User author = userRepository.save(createTestUser("author", UserRole.USER));
        CommunityRequest request = createRequest("제목", "<p>본문</p>", (short) 1);
        Community post = communityService.createPost(CommunityPostType.FREE, request, author, OffsetDateTime.now());

        User liker = userRepository.save(createTestUser("liker", UserRole.USER));

        // when
        likeService.createLikeOnCommunity(post.getId(), liker, true,  OffsetDateTime.now());

        // then
        Like like = likeRepository.findAll().get(0);
        assertThat(like).extracting("post", "user", "dislike")
                .contains(post, liker, true);
    }

    @Test
    @DisplayName("커뮤니티 글에 이미 추천을 누른 경우 예외를 던진다")
    void createLike_community_already_liked() {
        // given
        User author = userRepository.save(createTestUser("author", UserRole.USER));
        CommunityRequest request = createRequest("제목", "<p>본문</p>", (short) 1);
        Community post = communityService.createPost(CommunityPostType.FREE, request, author, OffsetDateTime.now());

        User liker = userRepository.save(createTestUser("liker", UserRole.USER));
        likeService.createLikeOnCommunity(post.getId(), liker, false, OffsetDateTime.now());

        // when then
        assertThatThrownBy(() -> likeService.createLikeOnCommunity(post.getId(), liker, false, OffsetDateTime.now()))
                .isInstanceOf(CommonException.class)
                .hasMessage("이미 추천/비추천하였습니다.");
    }

    @Test
    @DisplayName("커뮤니티 글에 이미 비추천을 누른 경우 예외를 던진다")
    void createDislike_community_already_disliked() {
        // given
        User author = userRepository.save(createTestUser("author", UserRole.USER));
        CommunityRequest request = createRequest("제목", "<p>본문</p>", (short) 1);
        Community post = communityService.createPost(CommunityPostType.FREE, request, author, OffsetDateTime.now());

        User liker = userRepository.save(createTestUser("liker", UserRole.USER));
        likeService.createLikeOnCommunity(post.getId(), liker, true, OffsetDateTime.now());

        // when then
        assertThatThrownBy(() -> likeService.createLikeOnCommunity(post.getId(), liker, true, OffsetDateTime.now()))
                .isInstanceOf(CommonException.class)
                .hasMessage("이미 추천/비추천하였습니다.");
    }

    @Test
    @DisplayName("자료실 글에 추천을 추가한다")
    void createLike_referenceRoom() {
        // given
        User author = userRepository.save(createTestUser("author", UserRole.USER));
        ReferenceRoomRequest request = kr.ac.cbnu.tux.domain.referenceroom.factory.ReferenceRoomFactory
                .createRequest("제목", "<p>본문</p>", (short) 1);
        ReferenceRoom data = referenceRoomService.createData(ReferenceRoomPostType.STUDY, request, author, OffsetDateTime.now());

        User liker = userRepository.save(createTestUser("liker", UserRole.USER));

        // when
        likeService.createLikeOnReferenceRoom(data.getId(), liker, false, OffsetDateTime.now());

        // then
        Like like = likeRepository.findAll().get(0);
        assertThat(like).extracting("data", "user", "dislike")
                .contains(data, liker, false);
    }

    @Test
    @DisplayName("자료실 글에 비추천을 추가한다")
    void createDislike_referenceRoom() {
        // given
        User author = userRepository.save(createTestUser("author", UserRole.USER));
        ReferenceRoomRequest request = kr.ac.cbnu.tux.domain.referenceroom.factory.ReferenceRoomFactory
                .createRequest("제목", "<p>본문</p>", (short) 1);
        ReferenceRoom data = referenceRoomService.createData(ReferenceRoomPostType.STUDY, request, author, OffsetDateTime.now());

        User liker = userRepository.save(createTestUser("liker", UserRole.USER));

        // when
        likeService.createLikeOnReferenceRoom(data.getId(), liker, true, OffsetDateTime.now());

        // then
        Like like = likeRepository.findAll().get(0);
        assertThat(like).extracting("data", "user", "dislike")
                .contains(data, liker, true);
    }

    @Test
    @DisplayName("자료실 글에 이미 추천을 누른 경우 예외를 던진다")
    void createLike_referenceRoom_already_liked() {
        // given
        User author = userRepository.save(createTestUser("author", UserRole.USER));
        ReferenceRoomRequest request = kr.ac.cbnu.tux.domain.referenceroom.factory.ReferenceRoomFactory
                .createRequest("제목", "<p>본문</p>", (short) 1);
        ReferenceRoom data = referenceRoomService.createData(ReferenceRoomPostType.STUDY, request, author, OffsetDateTime.now());

        User liker = userRepository.save(createTestUser("liker", UserRole.USER));
        likeService.createLikeOnReferenceRoom(data.getId(), liker, false, OffsetDateTime.now());

        // when then
        assertThatThrownBy(() -> likeService.createLikeOnReferenceRoom(data.getId(), liker, false, OffsetDateTime.now()))
                .isInstanceOf(CommonException.class)
                .hasMessage("이미 추천/비추천하였습니다.");
    }

    @Test
    @DisplayName("자료실 글에 이미 비추천을 누른 경우 예외를 던진다")
    void createDislike_referenceRoom_already_disliked() {
        // given
        User author = userRepository.save(createTestUser("author", UserRole.USER));
        ReferenceRoomRequest request = kr.ac.cbnu.tux.domain.referenceroom.factory.ReferenceRoomFactory
                .createRequest("제목", "<p>본문</p>", (short) 1);
        ReferenceRoom data = referenceRoomService.createData(ReferenceRoomPostType.STUDY, request, author, OffsetDateTime.now());

        User liker = userRepository.save(createTestUser("liker", UserRole.USER));
        likeService.createLikeOnReferenceRoom(data.getId(), liker, true, OffsetDateTime.now());

        // when then
        assertThatThrownBy(() -> likeService.createLikeOnReferenceRoom(data.getId(), liker, true, OffsetDateTime.now()))
                .isInstanceOf(CommonException.class)
                .hasMessage("이미 추천/비추천하였습니다.");
    }
}
