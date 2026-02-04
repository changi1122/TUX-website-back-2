package kr.ac.cbnu.tux.domain.community.dto.response;

import kr.ac.cbnu.tux.domain.community.entity.Community;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommunitySimpleResponse {

    private Long id;
    private String category;
    private String title;
    private OffsetDateTime createdDate;
    private OffsetDateTime editedDate;
    private Long view;
    private Integer comment;
    private String author;
    private Long likes;

    public static CommunitySimpleResponse of(Community post) {
        return CommunitySimpleResponse.builder()
                .id(post.getId())
                .category(post.getCategory().name())
                .title(post.getTitle())
                .createdDate(post.getCreatedDate())
                .editedDate(post.getEditedDate())
                .view(post.getView())
                .comment(post.getComments().stream().filter(p -> !p.getIsDeleted()).toList().size())
                .author(post.getUser().getNickname())
                .likes(post.getLikes().stream().filter(l -> !l.getDislike()).count())
                .build();
    }
}
