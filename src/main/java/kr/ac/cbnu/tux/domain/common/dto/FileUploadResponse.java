package kr.ac.cbnu.tux.domain.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileUploadResponse {

    private Long id;
    private String filename;

    public static FileUploadResponse of(Long id, String filename) {
        return FileUploadResponse.builder().id(id).filename(filename).build();
    }
}
