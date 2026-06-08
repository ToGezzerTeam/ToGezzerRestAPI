package com.togezzer.restapi.user.messaging;

import com.togezzer.restapi.user.dto.UserDto;
import com.togezzer.restapi.user.dto.UserEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserEventProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private UserEventProducer userEventProducer;

    private final String exchange = "message.exchange";
    private final String routingKeyUser = "routing-user";

    @BeforeEach
    void setup() {
        userEventProducer = new UserEventProducer(rabbitTemplate, exchange, routingKeyUser);
    }

    @Test
    void should_publish_user_event_to_correct_exchange_and_routing_key() {
        // Arrange
        final var userEventDto = UserEventDTO.builder()
                .uuid(UUID.randomUUID())
                .userName("testuser")
                .serverUuid(UUID.randomUUID()).build();

        // Act
        userEventProducer.publishToQueues(userEventDto);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(exchange, routingKeyUser, userEventDto);
    }

    @Test
    void should_pass_correct_payload_to_rabbit_template() {
        // Arrange
        final var userEventDto = UserEventDTO.builder()
                .uuid(UUID.randomUUID())
                .userName("testuser")
                .serverUuid(UUID.randomUUID()).build();

        final var exchangeCaptor = ArgumentCaptor.forClass(String.class);
        final var routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        final var payloadCaptor = ArgumentCaptor.forClass(Object.class);

        // Act
        userEventProducer.publishToQueues(userEventDto);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                payloadCaptor.capture()
        );

        assertEquals(exchange, exchangeCaptor.getValue());
        assertEquals(routingKeyUser, routingKeyCaptor.getValue());
        assertEquals(userEventDto, payloadCaptor.getValue());
    }
}
