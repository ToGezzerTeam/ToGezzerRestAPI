package com.togezzer.restapi.exception;

import java.util.UUID;

public class MessageNotFoundRemoteException extends NotFoundException {
    public MessageNotFoundRemoteException(UUID messageUuid, UUID roomId) {
        super("Message with uuid %s not found in roomId %s".formatted(messageUuid,roomId));
    }
}
