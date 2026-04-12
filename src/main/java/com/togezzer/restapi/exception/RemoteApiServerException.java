package com.togezzer.restapi.exception;

public class RemoteApiServerException extends RuntimeException {

    public RemoteApiServerException(int status, String uri) {
        super(String.format("Server error (%d) while calling %s", status, uri));
    }
}
