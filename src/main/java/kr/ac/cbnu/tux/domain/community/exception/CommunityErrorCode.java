package kr.ac.cbnu.tux.domain.community.exception;

import kr.ac.cbnu.tux.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommunityErrorCode implements ErrorCode {

    USER_NOT_MATCHED(HttpStatus.FORBIDDEN, "user not matched"),
    USER_NOT_LOGGED_IN(HttpStatus.UNAUTHORIZED, "user not logged in"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Not found");

    private final HttpStatus status;
    private final String message;

    CommunityErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}