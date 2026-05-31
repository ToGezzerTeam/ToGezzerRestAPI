package com.togezzer.restapi.message.service;

import com.togezzer.restapi.auth.service.AuthUtils;
import com.togezzer.restapi.exception.UserNotFoundException;
import com.togezzer.restapi.message.dto.ContentDTO;
import com.togezzer.restapi.message.dto.CreateMessageDTO;
import com.togezzer.restapi.message.dto.MessageContext;
import com.togezzer.restapi.message.dto.MessageDTO;
import com.togezzer.restapi.message.dto.MessagesPageResponseDto;
import com.togezzer.restapi.message.dto.UpdateMessageDTO;
import com.togezzer.restapi.message.enums.ContentType;
import com.togezzer.restapi.message.messaging.MessageEventProducer;
import com.togezzer.restapi.user.UserEntity;
import com.togezzer.restapi.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class MessageService {
    private final MessageEventProducer messageEventProducer;
    private final MessageUtils messageUtils;
    private final MessageApiClientService messageApiClientService;
    private final AuthUtils authUtils;
    private final UserRepository userRepository;

    private MessageContext validateAndGetContext(UUID roomUuid, UUID messageUuid) {
        final UUID userUuid = authUtils.getCurrentUserUuid();
        messageUtils.validateEntryExists(roomUuid, userUuid);
        MessageDTO messageDTO = messageUuid != null ? messageUtils.getMessage(roomUuid, messageUuid) : null;
        if (messageDTO != null) messageUtils.isAuthorOfMessage(userUuid, messageDTO);
        return new MessageContext(userUuid, messageDTO);
    }

    public void updateMessage(UUID roomUuid, UUID messageUuid, UpdateMessageDTO updateMessageDTO) {
        MessageContext ctx = validateAndGetContext(roomUuid, messageUuid);
        messageEventProducer.publishToQueues(messageUtils.applyMessageUpdate(ctx.messageDTO(), updateMessageDTO.getMessage()));
    }

    public void deleteMessage(UUID roomUuid, UUID messageUuid) {
        MessageContext ctx = validateAndGetContext(roomUuid, messageUuid);
        messageEventProducer.publishToQueues(messageUtils.applyMessageDeletion(ctx.messageDTO(), ctx.userUuid()));
    }


    public void createMessage(UUID roomUuid, CreateMessageDTO createMessageDTO) {
        MessageContext ctx = validateAndGetContext(roomUuid, null);
        ContentDTO contentDTO = ContentDTO.builder().value(createMessageDTO.getMessage()).type(ContentType.TEXT).build();
        messageEventProducer.publishToQueues(messageUtils.createMessageDTO(roomUuid, contentDTO, createMessageDTO.getAnswerTo(), ctx.userUuid(), authUtils.getCurrentUserName(), UUID.randomUUID()));
    }

    public MessagesPageResponseDto getMessages(UUID roomUuid, String lastMessageUuid, int pageSize) {
        final UUID userUuid = authUtils.getCurrentUserUuid();
        messageUtils.validateEntryExists(roomUuid, userUuid);

        MessagesPageResponseDto page = messageApiClientService
                .getMessagesByRoomId(roomUuid, lastMessageUuid, pageSize);

        enrichWithAuthorNames(page.messageDTOS());

        return page;
    }

    private void enrichWithAuthorNames(List<MessageDTO> messages) {
        Set<UUID> authorIds = messages.stream()
                .map(msg -> UUID.fromString(msg.getAuthorId()))
                .collect(Collectors.toSet());

        Map<UUID, String> authorNames = userRepository.findAllByUuidIn(authorIds)
                .stream()
                .collect(Collectors.toMap(UserEntity::getUuid, UserEntity::getUsername));

        messages.forEach(msg -> {
            UUID authorId = UUID.fromString(msg.getAuthorId());
            String name = authorNames.getOrDefault(authorId, "Unknown");
            msg.setAuthorName(name);
        });
    }

}
