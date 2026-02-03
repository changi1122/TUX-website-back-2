package kr.ac.cbnu.tux.domain.common.dto;

import kr.ac.cbnu.tux.domain.common.entity.Attachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttachmentResponse {

    private Long id;
    private String filename;
    private String path;
    private Boolean isImage;
    private Integer order;
    private Long downloadCount;

    public static AttachmentResponse build(Attachment file) {
        return AttachmentResponse.builder()
                .id(file.getId())
                .filename(file.getFilename())
                .path(file.getPath())
                .isImage(file.getIsImage())
                .order(file.getOrder())
                .downloadCount(file.getDownloadCount())
                .build();
    }

}
