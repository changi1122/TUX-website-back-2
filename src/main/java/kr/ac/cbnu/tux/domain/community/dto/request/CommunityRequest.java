package kr.ac.cbnu.tux.domain.community.dto.request;

import jakarta.validation.constraints.NotEmpty;
import kr.ac.cbnu.tux.domain.community.entity.Community;
import kr.ac.cbnu.tux.domain.community.enums.CommunityPostType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommunityRequest {

    @NotEmpty
    private String title;

    @NotEmpty
    private String body;

    private Short editorVersion;

    public Community toEntity(CommunityPostType type) {
        return Community.builder()
                .category(type)
                .title(title)
                .body(body)
                .editorVersion(editorVersion)
                .build();
    }
}
