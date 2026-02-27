package kr.ac.cbnu.tux.domain.community.exception;

import kr.ac.cbnu.tux.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommunityErrorCode implements ErrorCode {

    USER_NOT_MATCHED(HttpStatus.FORBIDDEN, "글을 수정할 권한이 없습니다."),
    USER_NOT_LOGGED_IN(HttpStatus.UNAUTHORIZED, "추천/비추천을 하기 위해 로그인하세요."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    CommunityErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}