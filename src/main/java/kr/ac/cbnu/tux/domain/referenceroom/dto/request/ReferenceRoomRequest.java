package kr.ac.cbnu.tux.domain.referenceroom.dto.request;

import jakarta.validation.constraints.NotEmpty;
import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import kr.ac.cbnu.tux.domain.referenceroom.enums.ReferenceRoomPostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceRoomRequest {

    @NotEmpty
    private String title;

    @NotEmpty
    private String body;

    private Short editorVersion;
    private Boolean isAnonymized;
    private String lecture;
    private String semester;
    private String professor;

    public ReferenceRoom toEntity(ReferenceRoomPostType type) {
        return ReferenceRoom.builder()
                .category(type)
                .title(title)
                .body(body)
                .editorVersion(editorVersion)
                .isAnonymized(isAnonymized)
                .lecture(lecture)
                .semester(semester)
                .professor(professor)
                .build();
    }
}
