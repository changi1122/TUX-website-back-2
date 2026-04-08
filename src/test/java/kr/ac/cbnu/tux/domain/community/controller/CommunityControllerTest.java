package kr.ac.cbnu.tux.domain.community.controller;

import kr.ac.cbnu.tux.domain.common.entity.Attachment;
import kr.ac.cbnu.tux.domain.community.dto.request.CmCommentRequest;
import kr.ac.cbnu.tux.domain.community.dto.request.CommunityRequest;
import kr.ac.cbnu.tux.domain.community.entity.Community;
import kr.ac.cbnu.tux.domain.community.enums.CommunityPostType;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.domain.user.enums.UserRole;
import kr.ac.cbnu.tux.domain.user.factory.UserFactory;
import kr.ac.cbnu.tux.utility.ControllerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommunityController.class)
class CommunityControllerTest extends ControllerTestSupport {

    @Test
    @WithMockUser
    @DisplayName("게시글 작성 시 type 파라미터는 필수이다")
    void createPost_withoutType_returnsBadRequest() throws Exception {
        // given
        CommunityRequest request = CommunityRequest.builder()
                .title("제목")
                .body("내용")
                .build();

        // when & then
        mockMvc.perform(post("/api/community")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("게시글 작성 시 title과 body는 필수이다")
    void createPost_withoutTitleAndBody_returnsBadRequest() throws Exception {
        // given
        CommunityRequest request = CommunityRequest.builder().build();

        // when & then
        mockMvc.perform(post("/api/community")
                        .with(csrf())
                        .param("type", CommunityPostType.FREE.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("파일 업로드 시 type과 file은 필수이다")
    void uploadFileBeforeCreatePost_withoutTypeOrFile_returnsBadRequest() throws Exception {
        mockMvc.perform(multipart("/api/community/file")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("임시 글 업데이트 시 title과 body는 필수이다")
    void updateTemporalPost_withoutTitleAndBody_returnsBadRequest() throws Exception {
        // given
        CommunityRequest request = CommunityRequest.builder().build();

        // when & then
        mockMvc.perform(post("/api/community/1")
                        .with(csrf())
                        .param("type", CommunityPostType.FREE.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("글 수정 시 title과 body는 필수이다")
    void updatePost_withoutTitleAndBody_returnsBadRequest() throws Exception {
        // given
        CommunityRequest request = CommunityRequest.builder().build();

        // when & then
        mockMvc.perform(put("/api/community/1")
                        .with(csrf())
                        .param("type", CommunityPostType.FREE.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("댓글 작성 시 body는 필수이다")
    void addComment_withoutBody_returnsBadRequest() throws Exception {
        // given
        CmCommentRequest request = CmCommentRequest.builder().build();

        // when & then
        mockMvc.perform(post("/api/community/1/comment")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("다른 사용자의 글에 일반 USER가 파일 업로드 시 실패한다")
    void uploadFileAfterCreatePost_byNonOwnerUser_throwsException() throws Exception {
        // given
        User postOwner = UserFactory.createTestUser("owner", UserRole.USER);
        ReflectionTestUtils.setField(postOwner, "id", 1L);

        User currentUser = UserFactory.createTestUser("other", UserRole.USER);
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        Community post = Community.builder()
                .id(1L)
                .category(CommunityPostType.FREE)
                .title("제목")
                .body("내용")
                .user(postOwner)
                .build();

        given(communityService.getPost(1L)).willReturn(post);

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        // when & then
        mockMvc.perform(multipart("/api/community/1/file")
                        .file(file)
                        .with(csrf())
                        .with(user(currentUser)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN은 다른 사용자의 글에 파일 업로드 가능하다")
    void uploadFileAfterCreatePost_byAdmin_succeeds() throws Exception {
        // given
        User postOwner = UserFactory.createTestUser("owner", UserRole.USER);
        ReflectionTestUtils.setField(postOwner, "id", 1L);

        User admin = UserFactory.createTestUser("admin", UserRole.ADMIN);
        ReflectionTestUtils.setField(admin, "id", 2L);

        Community post = Community.builder()
                .id(1L)
                .category(CommunityPostType.FREE)
                .title("제목")
                .body("내용")
                .user(postOwner)
                .build();

        given(communityService.getPost(1L)).willReturn(post);

        Attachment attachment = Attachment.builder()
                .filename("test.txt")
                .path("/api/community/1/file/test.txt")
                .isImage(false)
                .downloadCount(0L)
                .build();
        given(attachmentService.createAttachment(any(MultipartFile.class), any(Community.class), any(User.class))).willReturn(attachment);

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        // when & then
        mockMvc.perform(multipart("/api/community/1/file")
                        .file(file)
                        .with(csrf())
                        .with(user(admin)))
                .andDo(print())
                .andExpect(status().isAccepted());
    }

    @Test
    @DisplayName("다른 사용자의 첨부파일을 일반 USER가 삭제 시 실패한다")
    void deleteFile_byNonOwnerUser_throwsException() throws Exception {
        // given
        User postOwner = UserFactory.createTestUser("owner", UserRole.USER);
        ReflectionTestUtils.setField(postOwner, "id", 1L);

        User currentUser = UserFactory.createTestUser("other", UserRole.USER);
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        Community post = Community.builder()
                .id(1L)
                .category(CommunityPostType.FREE)
                .title("제목")
                .body("내용")
                .user(postOwner)
                .build();

        given(communityService.getPost(1L)).willReturn(post);

        // when & then
        mockMvc.perform(delete("/api/community/1/file/test.txt")
                        .with(csrf())
                        .with(user(currentUser)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN은 다른 사용자의 첨부파일 삭제 가능하다")
    void deleteFile_byAdmin_succeeds() throws Exception {
        // given
        User postOwner = UserFactory.createTestUser("owner", UserRole.USER);
        ReflectionTestUtils.setField(postOwner, "id", 1L);

        User admin = UserFactory.createTestUser("admin", UserRole.ADMIN);
        ReflectionTestUtils.setField(admin, "id", 2L);

        Community post = Community.builder()
                .id(1L)
                .category(CommunityPostType.FREE)
                .title("제목")
                .body("내용")
                .user(postOwner)
                .build();

        given(communityService.getPost(1L)).willReturn(post);

        // when & then
        mockMvc.perform(delete("/api/community/1/file/test.txt")
                        .with(csrf())
                        .with(user(admin)))
                .andDo(print())
                .andExpect(status().isAccepted());
    }

}
