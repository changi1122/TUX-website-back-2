package kr.ac.cbnu.tux.domain.referenceroom.exception;

import kr.ac.cbnu.tux.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ReferenceRoomErrorCode implements ErrorCode {

    USER_NOT_MATCHED(HttpStatus.FORBIDDEN, "user not matched"),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "permission denied"),
    USER_NOT_LOGGED_IN(HttpStatus.UNAUTHORIZED, "user not logged in"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Not found");

    private final HttpStatus status;
    private final String message;

    ReferenceRoomErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}