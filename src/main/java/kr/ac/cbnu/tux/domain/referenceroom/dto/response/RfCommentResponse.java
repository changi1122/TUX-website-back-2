package kr.ac.cbnu.tux.domain.referenceroom.dto.response;

import kr.ac.cbnu.tux.domain.referenceroom.entity.RfComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

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

    public static RfCommentResponse of(RfComment comment) {

        return RfCommentResponse.builder()
                .id(comment.getId())
                .body(comment.getBody())
                .createdDate(comment.getCreatedDate())
                .authorId(comment.getUser().getId())
                .author(comment.getUser().getNickname())
                .build();
    }

}
