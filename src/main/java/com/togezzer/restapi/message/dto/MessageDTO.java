package com.togezzer.restapi.message.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.togezzer.restapi.message.enums.MessageState;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageDTO {

    @NotBlank
    private String uuid;

    @NotBlank
    private String roomId;

    @NotBlank
    private String authorId;

    private String answerTo;

    @NotNull
    @Valid
    private ContentDTO content;

    @NotNull
    @Valid
    private MessageState state;

    @NotNull
    private Instant createdAt;

    private Instant updatedAt;

    private Instant deletedAt;

    private String deletedBy;
}
