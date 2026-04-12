package com.togezzer.restapi.exception;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class MessageNotOwnedByUserException extends RuntimeException {
    public MessageNotOwnedByUserException(UUID userUuid, @NotBlank String messageUuid) {
        super("User with ID " + userUuid + " is not the author of the message with ID " + messageUuid);
    }
}
