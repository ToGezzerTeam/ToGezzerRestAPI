package com.togezzer.restapi.room.messaging;

import com.togezzer.restapi.room.dto.RoomEventDTO;
import com.togezzer.restapi.room.enums.StatusEvent;
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
class RoomEventProducerTest {
    @Mock
    private RabbitTemplate rabbitTemplate;

    private RoomEventProducer roomEventProducer;

    private final String exchange = "message.exchange";
    private final String routingKeyRoom = "routing-room";

    @BeforeEach
    void setup() {
        roomEventProducer = new RoomEventProducer(rabbitTemplate, exchange, routingKeyRoom);
    }

    @Test
    void should_publish_room_event_to_correct_exchange_and_routing_key() {
        // Arrange
        final var roomEventDTO = RoomEventDTO.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .name("Test room")
                .build();

        // Act
        roomEventProducer.publishToQueues(roomEventDTO);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(exchange, routingKeyRoom, roomEventDTO);
    }

    @Test
    void should_pass_correct_payload_to_rabbit_template() {
        // Arrange
        final var uuid = UUID.randomUUID();
        final var roomEventDTO = RoomEventDTO.builder()
                .id(1L)
                .uuid(uuid)
                .name("Test room")
                .statusEvent(StatusEvent.CREATED)
                .build();

        final var exchangeCaptor = ArgumentCaptor.forClass(String.class);
        final var routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        final var payloadCaptor = ArgumentCaptor.forClass(Object.class);

        // Act
        roomEventProducer.publishToQueues(roomEventDTO);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                payloadCaptor.capture()
        );

        assertEquals(exchange, exchangeCaptor.getValue());
        assertEquals(routingKeyRoom, routingKeyCaptor.getValue());
        assertEquals(roomEventDTO, payloadCaptor.getValue());
    }
}
