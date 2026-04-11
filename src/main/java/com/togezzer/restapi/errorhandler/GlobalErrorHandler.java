package com.togezzer.restapi.errorhandler;

import com.togezzer.restapi.exception.AlreadyInRoomException;
import com.togezzer.restapi.exception.MessageNotFoundRemoteException;
import com.togezzer.restapi.exception.MessageNotOwnedByUserException;
import com.togezzer.restapi.exception.RemoteApiClientException;
import com.togezzer.restapi.exception.RemoteApiServerException;
import com.togezzer.restapi.exception.RoomNotFoundException;
import com.togezzer.restapi.exception.UserNotFoundException;
import com.togezzer.restapi.exception.UserNotInRoomException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalErrorHandler {
    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<String> handleRoomNotFoundException(RoomNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(AlreadyInRoomException.class)
    public ResponseEntity<String> handleAlreadyInRoomException(AlreadyInRoomException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(UserNotInRoomException.class)
    public ResponseEntity<String> handleUserNotInRoomException(UserNotInRoomException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(MessageNotFoundRemoteException.class)
    public ResponseEntity<String> handleMessageNotFoundRemoteException(MessageNotFoundRemoteException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(RemoteApiClientException.class)
    public ResponseEntity<String> handleRemoteApiClientException(RemoteApiClientException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ex.getMessage());
    }

    @ExceptionHandler(RemoteApiServerException.class)
    public ResponseEntity<String> handleRemoteApiServerException(RemoteApiServerException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
    }

    @ExceptionHandler(MessageNotOwnedByUserException.class)
    public ResponseEntity<String> handleMessageNotOwnedByUserException(MessageNotOwnedByUserException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

}