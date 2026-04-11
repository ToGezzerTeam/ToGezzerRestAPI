package com.togezzer.restapi.message.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMessageDTO extends BaseMessageWithContentDTO {
    private String answerTo;
}
