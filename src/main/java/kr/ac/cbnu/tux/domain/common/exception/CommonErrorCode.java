package kr.ac.cbnu.tux.domain.common.exception;

import kr.ac.cbnu.tux.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommonErrorCode implements ErrorCode {

    DUPLICATE_LIKE(HttpStatus.BAD_REQUEST, "already like/dislike given");

    private final HttpStatus status;
    private final String message;

    CommonErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}