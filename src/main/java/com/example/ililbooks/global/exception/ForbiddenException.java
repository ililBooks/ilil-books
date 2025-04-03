package com.example.ililbooks.global.exception;

public class ForbiddenException extends HandledException {

    public ForbiddenException() {
        super(ErrorCode.FORBIDDEN);
    }

    public ForbiddenException(String message) {
        super(ErrorCode.FORBIDDEN,message);
    }
}
