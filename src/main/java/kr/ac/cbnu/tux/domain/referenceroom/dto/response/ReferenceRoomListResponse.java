package kr.ac.cbnu.tux.domain.referenceroom.dto.response;

import kr.ac.cbnu.tux.domain.common.dto.PageInfo;
import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
@Builder
public class ReferenceRoomListResponse {
    private List<ReferenceRoomSimpleResponse> content;
    private PageInfo pageable;
    private long totalElements;

    public static ReferenceRoomListResponse of(Page<ReferenceRoom> page, Pageable pageable) {
        return ReferenceRoomListResponse.builder()
                .content(page.getContent().stream().map(ReferenceRoomSimpleResponse::of).toList())
                .pageable(new PageInfo(pageable))
                .totalElements(page.getTotalElements())
                .build();
    }
}
