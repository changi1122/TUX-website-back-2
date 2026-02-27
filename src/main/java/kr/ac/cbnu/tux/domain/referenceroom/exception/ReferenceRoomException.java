package kr.ac.cbnu.tux.domain.referenceroom.exception;

import org.springframework.http.HttpStatus;

public class ReferenceRoomException extends RuntimeException {

    private final ReferenceRoomErrorCode errorCode;

    public ReferenceRoomException(ReferenceRoomErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ReferenceRoomErrorCode getErrorCode() { return errorCode; }
    public HttpStatus getStatus() { return errorCode.getStatus(); }

}