package com.togezzer.restapi.user.messaging;

import com.togezzer.restapi.user.dto.UserEventDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserEventProducer {
    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKeyUser;

    public UserEventProducer(
            RabbitTemplate rabbitTemplate,
            @Value("${togezzer.rabbitmq.message-exchange:message.exchange}") String exchange,
            @Value("${togezzer.rabbitmq.routing-key.user:rooting-user}") String routingKeyUser
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKeyUser = routingKeyUser;
    }

    public void publishToQueues(UserEventDTO userEventDTO) {
        rabbitTemplate.convertAndSend(exchange, routingKeyUser, userEventDTO);
    }
}
