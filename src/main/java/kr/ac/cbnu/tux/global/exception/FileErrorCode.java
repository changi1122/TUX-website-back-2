package kr.ac.cbnu.tux.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum FileErrorCode implements ErrorCode {

    SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "첨부파일 저장 중 입출력 오류가 발생하였습니다."),
    DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "첨부파일 삭제 중 입출력 오류가 발생하였습니다."),
    COPY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "기본 배너 파일 복구 중 입출력 오류가 발생하였습니다.");

    private final HttpStatus status;
    private final String message;

    FileErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}