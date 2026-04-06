package com.togezzer.restapi.message.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum MessageState {
    CREATED,
    UPDATED,
    DELETED;

    @JsonValue
    public String toDbValue(){
        return this.name().toLowerCase();
    }

    @JsonCreator
    public static MessageState from(String messageState){
        return MessageState.valueOf(messageState.toUpperCase());
    }
}

