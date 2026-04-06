package com.togezzer.restapi.message.messaging;

import com.togezzer.restapi.message.dto.ContentDTO;
import com.togezzer.restapi.message.dto.MessageDTO;
import com.togezzer.restapi.message.enums.ContentType;
import com.togezzer.restapi.message.enums.MessageState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageEventProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void publishToQueues_should_send_to_both_routing_keys() {
        String exchange = "message.exchange";
        String rkA = "routing-message-chat-sauvegarde";
        String rkB = "routing-message-live-chat-service";

        MessageEventProducer producer = new MessageEventProducer(rabbitTemplate, exchange, rkA, rkB);

        MessageDTO dto = MessageDTO.builder()
                .uuid("m")
                .roomId("r")
                .authorId("a")
                .content(ContentDTO.builder().type(ContentType.TEXT).value("hello").build())
                .state(MessageState.CREATED)
                .createdAt(Instant.now())
                .build();

        producer.publishToQueues(dto);

        verify(rabbitTemplate).convertAndSend(exchange, rkA, dto);
        verify(rabbitTemplate).convertAndSend(exchange, rkB, dto);
        verifyNoMoreInteractions(rabbitTemplate);
    }
}
