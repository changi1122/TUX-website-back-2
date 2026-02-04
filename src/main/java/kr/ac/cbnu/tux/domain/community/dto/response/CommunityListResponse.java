package kr.ac.cbnu.tux.domain.community.dto.response;

import kr.ac.cbnu.tux.domain.common.dto.PageInfo;
import kr.ac.cbnu.tux.domain.community.entity.Community;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
@Builder
public class CommunityListResponse {
    private List<CommunitySimpleResponse> content;
    private PageInfo pageable;
    private long totalElements;

    public static CommunityListResponse of(Page<Community> page, Pageable pageable) {
        return CommunityListResponse.builder()
                .content(page.getContent().stream().map(CommunitySimpleResponse::of).toList())
                .pageable(new PageInfo(pageable))
                .totalElements(page.getTotalElements())
                .build();
    }
}
