package kr.ac.cbnu.tux.global.controller;

import kr.ac.cbnu.tux.domain.user.exception.UserException;

public record ErrorResponse(String code, String message) {

    public static ErrorResponse of(UserException ex) {
        return new ErrorResponse(ex.getErrorCode().name(), ex.getMessage());
    }
}