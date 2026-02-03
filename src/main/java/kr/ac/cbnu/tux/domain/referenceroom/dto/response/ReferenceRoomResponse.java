package kr.ac.cbnu.tux.domain.referenceroom.dto.response;

import kr.ac.cbnu.tux.domain.common.dto.AttachmentResponse;
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
public class ReferenceRoomResponse {

    private Long id;
    private String category;
    private String title;
    private String body;
    private OffsetDateTime createdDate;
    private OffsetDateTime editedDate;
    private short editorVersion;
    private Long view;
    private Long authorId;
    private String author;
    List<AttachmentResponse> files;
    List<RfCommentResponse> comments;
    private Long likes;
    List<String> likedPeople;
    private Long dislikes;

    private Boolean isAnonymized;
    private String lecture;
    private String semester;
    private String professor;

    public static ReferenceRoomResponse build(ReferenceRoom data) {
        List<AttachmentResponse> files = data.getAttachments().stream()
                .sorted((c1, c2) -> c1.getOrder().compareTo(c2.getOrder()))
                .map(c -> AttachmentResponse.build(c))
                .toList();

        List<RfCommentResponse> comments = data.getComments().stream()
                .filter(c -> !c.getIsDeleted())
                .sorted((c1, c2) -> c1.getCreatedDate().compareTo(c2.getCreatedDate()))
                .map(c -> RfCommentResponse.build(c))
                .toList();

        Long likes = data.getLikes().stream()
                .filter(l -> !l.getDislike())
                .count();

        List<String> likedPeople = data.getLikes().stream()
                .filter(l -> !l.getDislike() && !l.getUser().isDeleted())
                .map(l -> l.getUser().getNickname())
                .toList();

        Long dislikes = data.getLikes().stream()
                .filter(l -> l.getDislike())
                .count();

        return ReferenceRoomResponse.builder()
                .id(data.getId())
                .category(data.getCategory().name())
                .title(data.getTitle())
                .body(data.getBody())
                .createdDate(data.getCreatedDate())
                .editedDate(data.getEditedDate())
                .editorVersion(data.getEditorVersion() == null ? 1 : data.getEditorVersion())
                .view(data.getView())
                .authorId(data.getUser().getId())
                .author((data.getIsAnonymized()) ? "익명" : data.getUser().getNickname())
                .isAnonymized(data.getIsAnonymized())
                .likes(likes)
                .likedPeople(likedPeople)
                .dislikes(dislikes)
                .lecture(data.getLecture())
                .semester(data.getSemester())
                .professor(data.getProfessor())
                .files(files)
                .comments(comments)
                .build();
    }

}
