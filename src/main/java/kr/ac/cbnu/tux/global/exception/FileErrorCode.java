package kr.ac.cbnu.tux.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum FileErrorCode implements ErrorCode {

    SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "io exception while saveAttachment"),
    DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "io exception while deleteAttachment"),
    COPY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "io exception while copyBanner");

    private final HttpStatus status;
    private final String message;

    FileErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}