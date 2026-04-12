package com.togezzer.restapi.exception;

public class UserNotInRoomException extends RuntimeException {
    public UserNotInRoomException(String message) { super(message);}
}
