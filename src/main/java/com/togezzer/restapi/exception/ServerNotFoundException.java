package com.togezzer.restapi.exception;

public class ServerNotFoundException extends NotFoundException {
    public ServerNotFoundException(String message) {
        super(message);
    }
}
