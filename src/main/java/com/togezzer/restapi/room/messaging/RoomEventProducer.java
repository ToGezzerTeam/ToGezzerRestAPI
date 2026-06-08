package com.togezzer.restapi.room.messaging;

import com.togezzer.restapi.room.dto.RoomEventDTO;
import com.togezzer.restapi.user.UserDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RoomEventProducer {
    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKeyRoom;

    public RoomEventProducer(
            RabbitTemplate rabbitTemplate,
            @Value("${togezzer.rabbitmq.message-exchange:message.exchange}") String exchange,
            @Value("${togezzer.rabbitmq.routing-key.room:rooting-room}") String routingKeyRoom
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKeyRoom = routingKeyRoom;
    }

    public void publishToQueues(RoomEventDTO roomEventDTO) {
        rabbitTemplate.convertAndSend(exchange, routingKeyRoom, roomEventDTO);
    }
}
