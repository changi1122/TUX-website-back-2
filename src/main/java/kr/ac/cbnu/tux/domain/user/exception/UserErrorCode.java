package kr.ac.cbnu.tux.domain.user.exception;

import kr.ac.cbnu.tux.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserErrorCode implements ErrorCode {

    USERNAME_NOT_UNIQUE(HttpStatus.BAD_REQUEST, "username is not unique"),
    PASSWORD_RULE_NOT_MATCHED(HttpStatus.BAD_REQUEST, "password rule not matched"),
    USER_NOT_PRESENT(HttpStatus.BAD_REQUEST, "user not present"),
    USER_NOT_MATCHED(HttpStatus.FORBIDDEN, "user not matched"),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "permission denied");

    private final HttpStatus status;
    private final String message;

    UserErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}