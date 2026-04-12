package com.togezzer.restapi.message.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ContentType {
    TEXT;

    @JsonValue
    public String toDbValue(){
        return this.name().toLowerCase();
    }

    @JsonCreator
    public static ContentType from(String contentType){
        return ContentType.valueOf(contentType.toUpperCase());
    }
}
