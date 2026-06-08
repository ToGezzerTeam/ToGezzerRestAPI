package com.togezzer.restapi.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${togezzer.rabbitmq.message-exchange:message.exchange}")
    private String messageExchangeName;

    @Value("${togezzer.rabbitmq.queue.chat-sauvegarde:queue-message-chat-sauvegarde}")
    private String chatSauvegardeQueueName;

    @Value("${togezzer.rabbitmq.queue.live-chat-service:queue-message-live-chat-service}")
    private String liveChatServiceQueueName;

    @Value("${togezzer.rabbitmq.queue.room:queue-room}")
    private String roomQueueName;

    @Value("${togezzer.rabbitmq.queue.user:queue-user}")
    private String userQueueName;

    @Value("${togezzer.rabbitmq.routing-key.chat-sauvegarde:routing-message-chat-sauvegarde}")
    private String chatSauvegardeRoutingKey;

    @Value("${togezzer.rabbitmq.routing-key.live-chat-service:routing-message-live-chat-service}")
    private String liveChatServiceRoutingKey;

    @Value("${togezzer.rabbitmq.routing-key.room:rooting-room}")
    private String roomRootingKey;

    @Value("${togezzer.rabbitmq.routing-key.user:rooting-user}")
    private String userRootingKey;

    @Value("${togezzer.rabbitmq.exchanges.chat-sauvegarde.dlq}")
    private String dlqChatSauvegardeExchange;

    @Value("${togezzer.rabbitmq.routing-keys.chat-sauvegarde.dlq}")
    private String dlqChatSauvegardeRoutingKey;

    @Bean
    public DirectExchange messageExchange() {
        return new DirectExchange(messageExchangeName);
    }

    @Bean
    public Queue messageQueueChatSauvegarde() {
        return QueueBuilder.durable(chatSauvegardeQueueName)
                .withArgument("x-dead-letter-exchange", dlqChatSauvegardeExchange)
                .withArgument("x-dead-letter-routing-key", dlqChatSauvegardeRoutingKey)
                .build();
    }

    @Bean
    public Queue messageQueueLiveChatService() {
        return new Queue(liveChatServiceQueueName, true);
    }

    @Bean
    public Queue roomQueueService(){return new Queue(roomQueueName,true);}

    @Bean
    public Queue userQueueService(){return new Queue(userQueueName,true);}

    @Bean
    public Binding bindingChatSauvegarde(Queue messageQueueChatSauvegarde, DirectExchange messageExchange) {
        return BindingBuilder.bind(messageQueueChatSauvegarde).to(messageExchange).with(chatSauvegardeRoutingKey);
    }

    @Bean
    public Binding bindingLiveChatService(Queue messageQueueLiveChatService, DirectExchange messageExchange) {
        return BindingBuilder.bind(messageQueueLiveChatService).to(messageExchange).with(liveChatServiceRoutingKey);
    }

    @Bean
    public Binding bindingRoomService(Queue roomQueueService, DirectExchange messageExchange){
        return BindingBuilder.bind(roomQueueService).to(messageExchange).with(roomRootingKey);
    }

    @Bean
    public Binding bindingUserService(Queue userQueueService, DirectExchange messageExchange){
        return BindingBuilder.bind(userQueueService).to(messageExchange).with(userRootingKey);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         JacksonJsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}