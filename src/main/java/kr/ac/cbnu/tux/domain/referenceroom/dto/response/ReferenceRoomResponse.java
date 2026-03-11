package kr.ac.cbnu.tux.domain.referenceroom.dto.response;

import kr.ac.cbnu.tux.domain.common.dto.AttachmentResponse;
import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import kr.ac.cbnu.tux.domain.referenceroom.entity.RfComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public static ReferenceRoomResponse of(ReferenceRoom data) {
        List<AttachmentResponse> files = data.getAttachments().stream()
                .sorted((c1, c2) -> c1.getOrder().compareTo(c2.getOrder()))
                .map(c -> AttachmentResponse.of(c))
                .toList();

        List<RfComment> allComments = data.getComments().stream()
                .filter(c -> !c.getIsDeleted())
                .sorted(Comparator.comparing(c -> c.getCreatedDate()))
                .toList();

        Map<Long, List<RfCommentResponse>> repliesMap = allComments.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getParent().getId(),
                        Collectors.mapping(RfCommentResponse::of, Collectors.toList())
                ));

        List<RfCommentResponse> comments = allComments.stream()
                .filter(c -> c.getParent() == null)
                .map(c -> RfCommentResponse.of(c, repliesMap.getOrDefault(c.getId(), List.of())))
                .toList();

        List<String> likedPeople = data.getLikes().stream()
                .filter(l -> !l.getDislike() && !l.getUser().isDeleted())
                .map(l -> l.getUser().getNickname())
                .toList();

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
                .likes(data.getTotalLikes())
                .likedPeople(likedPeople)
                .dislikes(data.getTotalDislikes())
                .lecture(data.getLecture())
                .semester(data.getSemester())
                .professor(data.getProfessor())
                .files(files)
                .comments(comments)
                .build();
    }

}
