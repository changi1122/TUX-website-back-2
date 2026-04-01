package kr.ac.cbnu.tux.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.ac.cbnu.tux.domain.admin.service.StaticPageService;
import kr.ac.cbnu.tux.domain.common.service.AttachmentService;
import kr.ac.cbnu.tux.domain.common.service.LikeService;
import kr.ac.cbnu.tux.domain.community.service.CommunityService;
import kr.ac.cbnu.tux.domain.referenceroom.service.ReferenceRoomService;
import kr.ac.cbnu.tux.domain.user.repository.RefreshTokenRepository;
import kr.ac.cbnu.tux.domain.user.repository.UserRepository;
import kr.ac.cbnu.tux.domain.user.service.UserService;
import kr.ac.cbnu.tux.global.controller.GlobalExceptionController;
import kr.ac.cbnu.tux.global.security.JwtTokenProvider;
import kr.ac.cbnu.tux.global.utility.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 컨트롤러 단위 테스트를 위한 추상 클래스
 * 사용법:
 * @WebMvcTest(YourController.class)
 * class YourControllerTest extends ControllerTestSupport {
 *     // 테스트 코드
 * }
 */
@Import({GlobalExceptionController.class, ControllerTestSupport.TestJacksonConfig.class})
@ActiveProfiles("test")
public abstract class ControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // Security 관련 - JwtAuthenticationFilter에서 사용
    @MockitoBean
    protected JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    protected UserRepository userRepository;

    @MockitoBean
    protected RefreshTokenRepository refreshTokenRepository;

    // Domain Services
    @MockitoBean
    protected UserService userService;

    @MockitoBean
    protected CommunityService communityService;

    @MockitoBean
    protected ReferenceRoomService referenceRoomService;

    @MockitoBean
    protected AttachmentService attachmentService;

    @MockitoBean
    protected LikeService likeService;

    @MockitoBean
    protected StaticPageService staticPageService;

    @MockitoBean
    protected FileStore fileStore;

    @TestConfiguration
    static class TestJacksonConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
    }

}
