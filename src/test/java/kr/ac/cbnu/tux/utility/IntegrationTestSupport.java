package kr.ac.cbnu.tux.utility;

import kr.ac.cbnu.tux.domain.common.service.ViewCountService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTestSupport {

    @MockitoBean
    protected ViewCountService viewCountService;
}