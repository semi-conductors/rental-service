package com.rentmate.service.rental.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public TopicExchange rentalExchange() {
        return new TopicExchange("rental.exchange");
    }

    @Bean
    public Queue rentalCreatedQueue() {
        return new Queue("rental.created.queue", true);
    }

    @Bean
    public Binding bindingRentalCreatedQueue(Queue rentalCreatedQueue, TopicExchange rentalExchange) {
        return BindingBuilder.bind(rentalCreatedQueue).to(rentalExchange).with("rental.created");
    }
    @Bean
    public Binding bindingPaymentEventsQueue(Queue paymentEventsQueue, TopicExchange rentalExchange) {
        return BindingBuilder.bind(paymentEventsQueue).to(rentalExchange).with("payment.*");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
