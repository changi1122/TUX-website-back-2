package kr.ac.cbnu.tux.domain.common.exception;

import org.springframework.http.HttpStatus;

public class CommonException extends RuntimeException {

    private final CommonErrorCode errorCode;

    public CommonException(CommonErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CommonErrorCode getErrorCode() { return errorCode; }
    public HttpStatus getStatus() { return errorCode.getStatus(); }

}