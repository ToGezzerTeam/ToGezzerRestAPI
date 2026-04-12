package com.togezzer.restapi.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
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

    @Value("${togezzer.rabbitmq.routing-key.chat-sauvegarde:routing-message-chat-sauvegarde}")
    private String chatSauvegardeRoutingKey;

    @Value("${togezzer.rabbitmq.routing-key.live-chat-service:routing-message-live-chat-service}")
    private String liveChatServiceRoutingKey;

    @Bean
    public DirectExchange messageExchange() {
        return new DirectExchange(messageExchangeName);
    }

    @Bean
    public Queue messageQueueChatSauvegarde() {
        return new Queue(chatSauvegardeQueueName, true);
    }

    @Bean
    public Queue messageQueueLiveChatService() {
        return new Queue(liveChatServiceQueueName, true);
    }

    @Bean
    public Binding bindingChatSauvegarde(Queue messageQueueChatSauvegarde, DirectExchange messageExchange) {
        return BindingBuilder.bind(messageQueueChatSauvegarde).to(messageExchange).with(chatSauvegardeRoutingKey);
    }

    @Bean
    public Binding bindingLiveChatService(Queue messageQueueLiveChatService, DirectExchange messageExchange) {
        return BindingBuilder.bind(messageQueueLiveChatService).to(messageExchange).with(liveChatServiceRoutingKey);
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