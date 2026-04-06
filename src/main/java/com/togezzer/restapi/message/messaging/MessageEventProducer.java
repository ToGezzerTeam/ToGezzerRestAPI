package com.togezzer.restapi.message.messaging;

import com.togezzer.restapi.message.dto.MessageDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MessageEventProducer {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKeyChatSauvegarde;
    private final String routingKeyLiveChatService;

    public MessageEventProducer(
            RabbitTemplate rabbitTemplate,
            @Value("${togezzer.rabbitmq.message-exchange:message.exchange}") String exchange,
            @Value("${togezzer.rabbitmq.routing-key.chat-sauvegarde:routing-message-chat-sauvegarde}") String routingKeyChatSauvegarde,
            @Value("${togezzer.rabbitmq.routing-key.live-chat-service:routing-message-live-chat-service}") String routingKeyLiveChatService
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKeyChatSauvegarde = routingKeyChatSauvegarde;
        this.routingKeyLiveChatService = routingKeyLiveChatService;
    }

    public void publishToQueues(MessageDTO messageDTO) {
        rabbitTemplate.convertAndSend(exchange, routingKeyChatSauvegarde, messageDTO);
        rabbitTemplate.convertAndSend(exchange, routingKeyLiveChatService, messageDTO);
    }
}