package com.togezzer.restapi.message.dto;

import com.togezzer.restapi.message.enums.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class ContentDTO {

    @NotNull
    private ContentType type;

    @NotBlank
    private String value;
}
