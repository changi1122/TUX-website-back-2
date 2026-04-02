package kr.ac.cbnu.tux.domain.community.dto.request;

import jakarta.validation.constraints.NotEmpty;
import kr.ac.cbnu.tux.domain.community.entity.CmComment;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class CmCommentRequest {

    @NotEmpty
    private String body;

    private Long parentId;

    public CmComment toEntity() {
        return CmComment.builder()
                .body(body)
                .build();
    }
}
