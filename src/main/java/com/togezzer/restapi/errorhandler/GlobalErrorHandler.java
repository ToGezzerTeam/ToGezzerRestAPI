package com.togezzer.restapi.errorhandler;

import com.togezzer.restapi.exception.AlreadyInRoomException;
import com.togezzer.restapi.exception.RoomNotFoundException;
import com.togezzer.restapi.exception.UserNotFoundException;
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
}