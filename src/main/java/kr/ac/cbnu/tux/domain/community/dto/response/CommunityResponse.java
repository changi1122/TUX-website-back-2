package kr.ac.cbnu.tux.domain.community.dto.response;

import kr.ac.cbnu.tux.domain.common.dto.AttachmentResponse;
import kr.ac.cbnu.tux.domain.community.entity.Community;
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
public class CommunityResponse {

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
    private List<CmCommentResponse> comments;
    private Long likes;
    List<String> likedPeople;
    private Long dislikes;

    public static CommunityResponse of(Community post) {
        List<AttachmentResponse> files = post.getAttachments().stream()
                .sorted((c1, c2) -> c1.getOrder().compareTo(c2.getOrder()))
                .map(c -> AttachmentResponse.of(c))
                .toList();

        List<CmCommentResponse> comments = post.getComments().stream()
                .filter(c -> !c.getIsDeleted())
                .sorted((c1, c2) -> c1.getCreatedDate().compareTo(c2.getCreatedDate()))
                .map(c -> CmCommentResponse.of(c))
                .toList();

        List<String> likedPeople = post.getLikes().stream()
                .filter(l -> !l.getDislike() && !l.getUser().isDeleted())
                .map(l -> l.getUser().getNickname())
                .toList();

        return CommunityResponse.builder()
                .id(post.getId())
                .category(post.getCategory().name())
                .title(post.getTitle())
                .body(post.getBody())
                .createdDate(post.getCreatedDate())
                .editedDate(post.getEditedDate())
                .editorVersion(post.getEditorVersion() == null ? 1 : post.getEditorVersion())
                .view(post.getView())
                .authorId(post.getUser().getId())
                .author(post.getUser().getNickname())
                .files(files)
                .comments(comments)
                .likes(post.getTotalLikes())
                .likedPeople(likedPeople)
                .dislikes(post.getTotalDislikes())
                .build();
    }


}
