package com.togezzer.restapi.exception;

public class MinioException extends RuntimeException{
    public MinioException(String message) {
        super(message);
    }
}
