package kr.ac.cbnu.tux.domain.community.dto.response;

import kr.ac.cbnu.tux.domain.community.entity.CmComment;
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
public class CmCommentResponse {


    private Long id;
    private String body;
    private OffsetDateTime createdDate;
    private Long authorId;
    private String author;
    private List<CmCommentResponse> replies;

    public static CmCommentResponse of(CmComment comment) {

        return CmCommentResponse.builder()
                .id(comment.getId())
                .body(comment.getBody())
                .createdDate(comment.getCreatedDate())
                .authorId(comment.getUser().getId())
                .author(comment.getUser().getNickname())
                .replies(List.of())
                .build();
    }

    public static CmCommentResponse of(CmComment comment, List<CmCommentResponse> replies) {

        return CmCommentResponse.builder()
                .id(comment.getId())
                .body(comment.getBody())
                .createdDate(comment.getCreatedDate())
                .authorId(comment.getUser().getId())
                .author(comment.getUser().getNickname())
                .replies(replies)
                .build();
    }

}
