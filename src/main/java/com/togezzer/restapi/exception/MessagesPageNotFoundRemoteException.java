package com.togezzer.restapi.exception;

import java.util.UUID;

public class MessagesPageNotFoundRemoteException extends NotFoundException {
    public MessagesPageNotFoundRemoteException(UUID roomId) {
        super("Messages not found for roomId %s".formatted(roomId));
    }
}
