package com.togezzer.restapi.message;

import com.togezzer.restapi.message.dto.DeleteMessageDTO;
import com.togezzer.restapi.message.dto.UpdateMessageDTO;
import com.togezzer.restapi.message.service.MessageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/api/messages", produces = MediaType.APPLICATION_JSON_VALUE)
public class MessageController {
    private final MessageService messageService;

    @PatchMapping("{roomUuid}/{messageUuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMessage(@PathVariable @NotNull(message = "Room's UUID is required") UUID roomUuid,
                              @PathVariable @NotNull(message = "Message's UUID is required") UUID messageUuid,
                              @Valid @RequestBody UpdateMessageDTO updateMessageDTO) {
        this.messageService.updateMessage(roomUuid, messageUuid, updateMessageDTO);
    }

    @DeleteMapping("{roomUuid}/{messageUuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(@PathVariable @NotNull(message = "Room's UUID is required") UUID roomUuid,
                              @PathVariable @NotNull(message = "Message's UUID is required") UUID messageUuid,
                              @Valid @RequestBody DeleteMessageDTO deleteMessageDTO) {
        this.messageService.deleteMessage(roomUuid, messageUuid, deleteMessageDTO);
    }
}