package com.togezzer.restapi.exception;

public class AlreadyInServerException extends RuntimeException {
    public AlreadyInServerException(String message) {
        super(message);
    }
}
