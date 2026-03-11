package kr.ac.cbnu.tux.domain.referenceroom.dto.response;

import kr.ac.cbnu.tux.domain.referenceroom.entity.RfComment;
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
public class RfCommentResponse {

    private Long id;
    private String body;
    private OffsetDateTime createdDate;
    private Long authorId;
    private String author;
    private List<RfCommentResponse> replies;

    public static RfCommentResponse of(RfComment comment) {

        return RfCommentResponse.builder()
                .id(comment.getId())
                .body(comment.getBody())
                .createdDate(comment.getCreatedDate())
                .authorId(comment.getUser().getId())
                .author(comment.getUser().getNickname())
                .replies(List.of())
                .build();
    }

    public static RfCommentResponse of(RfComment comment, List<RfCommentResponse> replies) {

        return RfCommentResponse.builder()
                .id(comment.getId())
                .body(comment.getBody())
                .createdDate(comment.getCreatedDate())
                .authorId(comment.getUser().getId())
                .author(comment.getUser().getNickname())
                .replies(replies)
                .build();
    }

}
