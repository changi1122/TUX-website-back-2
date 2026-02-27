package kr.ac.cbnu.tux.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    HttpStatus getStatus();
    String name();
    String getMessage();
}