package kr.ac.cbnu.tux.domain.community.dto.response;

import kr.ac.cbnu.tux.domain.community.entity.CmComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

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

    public static CmCommentResponse of(CmComment comment) {

        return CmCommentResponse.builder()
                .id(comment.getId())
                .body(comment.getBody())
                .createdDate(comment.getCreatedDate())
                .authorId(comment.getUser().getId())
                .author(comment.getUser().getNickname())
                .build();
    }

}
