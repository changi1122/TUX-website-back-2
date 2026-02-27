package kr.ac.cbnu.tux.global.controller;

import kr.ac.cbnu.tux.domain.common.exception.CommonException;
import kr.ac.cbnu.tux.domain.community.exception.CommunityException;
import kr.ac.cbnu.tux.domain.referenceroom.exception.ReferenceRoomException;
import kr.ac.cbnu.tux.domain.user.exception.UserException;
import kr.ac.cbnu.tux.global.exception.FileException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.NoSuchElementException;

@ControllerAdvice(basePackages = "kr.ac.cbnu.tux")
public class GlobalExceptionController {

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleUserException(UserException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ErrorResponse.of(ex.getErrorCode()));
    }

    @ExceptionHandler(CommunityException.class)
    public ResponseEntity<ErrorResponse> handleCommunityException(CommunityException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ErrorResponse.of(ex.getErrorCode()));
    }

    @ExceptionHandler(ReferenceRoomException.class)
    public ResponseEntity<ErrorResponse> handleReferenceRoomException(ReferenceRoomException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ErrorResponse.of(ex.getErrorCode()));
    }

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<ErrorResponse> handleCommonException(CommonException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ErrorResponse.of(ex.getErrorCode()));
    }

    @ExceptionHandler(FileException.class)
    public ResponseEntity<ErrorResponse> handleFileException(FileException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ErrorResponse.of(ex.getErrorCode()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleException(NoSuchElementException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<String> handleException(FileNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleException(IOException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }
}
