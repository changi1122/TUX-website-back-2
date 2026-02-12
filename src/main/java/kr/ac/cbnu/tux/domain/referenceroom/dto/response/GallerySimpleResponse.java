package kr.ac.cbnu.tux.domain.referenceroom.dto.response;

import kr.ac.cbnu.tux.domain.common.dto.AttachmentResponse;
import kr.ac.cbnu.tux.domain.common.entity.Attachment;
import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GallerySimpleResponse {

    private Long id;
    private String category;
    private AttachmentResponse mainImage;
    private String title;
    private OffsetDateTime createdDate;
    private OffsetDateTime editedDate;
    private Long view;
    private Integer comment;
    private String author;
    private Long likes;

    private String lecture;
    private String semester;
    private String professor;

    public static GallerySimpleResponse of(ReferenceRoom data) {
        List<Attachment> imageFiles = data.getAttachments().stream()
                .filter(c -> c.getIsImage())
                .sorted((c1, c2) -> c1.getOrder().compareTo(c2.getOrder()))
                .toList();

        return GallerySimpleResponse.builder()
                .id(data.getId())
                .category(data.getCategory().name())
                .mainImage((imageFiles.size() > 0) ? AttachmentResponse.of(imageFiles.get(0)) : null)
                .title(data.getTitle())
                .createdDate(data.getCreatedDate())
                .editedDate(data.getEditedDate())
                .view(data.getView())
                .comment(data.getComments().stream().filter(c -> !c.getIsDeleted()).toList().size())
                .author((data.getIsAnonymized()) ? "익명" : data.getUser().getNickname())
                .likes(data.getLikes().stream().filter(l -> !l.getDislike()).count())
                .lecture(data.getLecture())
                .semester(data.getSemester())
                .professor(data.getProfessor())
                .build();
    }
}
