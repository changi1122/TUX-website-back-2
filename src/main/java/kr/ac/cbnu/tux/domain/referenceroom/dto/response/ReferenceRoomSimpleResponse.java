package kr.ac.cbnu.tux.domain.referenceroom.dto.response;

import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReferenceRoomSimpleResponse {

    private Long id;
    private String category;
    private String title;
    private OffsetDateTime createdDate;
    private OffsetDateTime editedDate;
    private Long view;
    private Long comment;
    private String author;
    private Long likes;

    private String lecture;
    private String semester;
    private String professor;

    public static ReferenceRoomSimpleResponse of(ReferenceRoom data) {
        return ReferenceRoomSimpleResponse.builder()
                .id(data.getId())
                .category(data.getCategory().name())
                .title(data.getTitle())
                .createdDate(data.getCreatedDate())
                .editedDate(data.getEditedDate())
                .view(data.getView())
                .comment(data.getTotalComments())
                .author((data.getIsAnonymized()) ? "익명" : data.getUser().getNickname())
                .likes(data.getTotalLikes())
                .lecture(data.getLecture())
                .semester(data.getSemester())
                .professor(data.getProfessor())
                .build();
    }
}
