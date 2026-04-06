package com.togezzer.restapi.exception;

public class RemoteApiClientException extends RuntimeException {

    public RemoteApiClientException(int status, String uri) {
        super(String.format("Client error (%d) while calling %s", status, uri));
    }
}
