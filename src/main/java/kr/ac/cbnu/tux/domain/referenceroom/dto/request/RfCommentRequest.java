package kr.ac.cbnu.tux.domain.referenceroom.dto.request;

import jakarta.validation.constraints.NotEmpty;
import kr.ac.cbnu.tux.domain.referenceroom.entity.RfComment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RfCommentRequest {

    @NotEmpty
    private String body;

    private Long parentId;

    public RfComment toEntity() {
        return RfComment.builder()
                .body(body)
                .build();
    }
}
