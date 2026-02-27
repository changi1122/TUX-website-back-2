package kr.ac.cbnu.tux.domain.community.exception;

import org.springframework.http.HttpStatus;

public class CommunityException extends RuntimeException {

    private final CommunityErrorCode errorCode;

    public CommunityException(CommunityErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CommunityErrorCode getErrorCode() { return errorCode; }
    public HttpStatus getStatus() { return errorCode.getStatus(); }

}