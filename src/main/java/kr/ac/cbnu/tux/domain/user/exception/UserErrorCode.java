package kr.ac.cbnu.tux.domain.user.exception;

import kr.ac.cbnu.tux.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserErrorCode implements ErrorCode {

    USERNAME_NOT_UNIQUE(HttpStatus.BAD_REQUEST, "이미 사용 중인 아이디입니다."),
    PASSWORD_RULE_NOT_MATCHED(HttpStatus.BAD_REQUEST, "비밀번호 영문자와 숫자를 포함하여 최소 8자 이상이어야 합니다."),
    USER_NOT_PRESENT(HttpStatus.BAD_REQUEST, "회원이 존재하지 않습니다."),
    USER_NOT_MATCHED(HttpStatus.FORBIDDEN, "로그인 정보와 회원이 일치하지 않습니다."),
    LOGIN_FAILED(HttpStatus.BAD_REQUEST, "아이디 또는 비밀번호가 잘못되었습니다."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "회원 정보를 조회할 권한이 없습니다.");

    private final HttpStatus status;
    private final String message;

    UserErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}