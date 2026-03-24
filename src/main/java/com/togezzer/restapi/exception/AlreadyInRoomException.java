package com.togezzer.restapi.exception;

public class AlreadyInRoomException extends RuntimeException {
    public AlreadyInRoomException(String message) {
        super(message);
    }
}
