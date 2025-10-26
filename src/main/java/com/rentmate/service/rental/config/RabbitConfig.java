package com.rentmate.service.rental.config;

import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String RENTAL_EXCHANGE = "rental.exchange";

    public static final String DELIVERY_EVENTS_QUEUE = "delivery.events.queue";
    public static final String DELIVERY_RETURN_EVENTS_QUEUE = "delivery.return.events.queue";
    public static final String PAYMENT_EVENTS_QUEUE = "payment.events.queue";
    public static final String PAYMENT_RETURN_EVENTS_QUEUE = "payment.return.events.queue";

    public static final String DELIVERY_ROUTING_KEY = "DeliveryService.*"; // delivery.deliveryCost, delivery.delivered
    public static final String DELIVERY_RETURN_ROUTING_KEY = "delivery.return.*"; // delivery.returned, delivery.inReturning
    public static final String PAYMENT_ROUTING_KEY = "payment.*"; // payment.paid, payment.failed
    public static final String PAYMENT_RETURN_ROUTING_KEY = "payment.refund.*"; // payment.refunded

    @PostConstruct
    public void init() {
        System.out.println("✅ RabbitConfig loaded successfully");
    }
//    @Bean
//    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
//        return new RabbitAdmin(connectionFactory);
//    }


    @Bean
    public TopicExchange rentalExchange() {
        System.out.println("Creating exchange: " + RENTAL_EXCHANGE);
        return new TopicExchange(RENTAL_EXCHANGE);
    }

    @Bean
    public Queue paymentQueue(){
        System.out.println("Creating Queue: " + PAYMENT_EVENTS_QUEUE);
        return new Queue(PAYMENT_EVENTS_QUEUE,true);
    }
    @Bean
    public Queue deliveryEventsQueue(){
        System.out.println("Creating Queue: " + DELIVERY_EVENTS_QUEUE);
        return new Queue(DELIVERY_EVENTS_QUEUE,true);
    }
    @Bean
    public Queue deliveryReturnEventsQueue(){
        System.out.println("Creating Queue: " + DELIVERY_RETURN_EVENTS_QUEUE);
        return new Queue(DELIVERY_RETURN_EVENTS_QUEUE,true);
    }
    @Bean
    public Queue paymentRefundQueue(){
        System.out.println("Creating Queue: " + PAYMENT_RETURN_EVENTS_QUEUE);
        return new Queue(PAYMENT_RETURN_EVENTS_QUEUE,true);
    }



    @Bean
    public Binding bindingPaymentQueue(
            @Qualifier("paymentQueue") Queue queue,
            TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(PAYMENT_ROUTING_KEY);
    }

    @Bean
    public Binding bindingDeliveryEventQueue(
            @Qualifier("deliveryEventsQueue") Queue queue,
            TopicExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(DELIVERY_ROUTING_KEY);
    }

    @Bean
    public Binding bindingDeliveryReturnEventQueue(
            @Qualifier("deliveryReturnEventsQueue") Queue queue,
            TopicExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(DELIVERY_RETURN_ROUTING_KEY);
    }

    @Bean
    public Binding bindingPaymentRefundQueue(
            @Qualifier("paymentRefundQueue") Queue queue,
            TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(PAYMENT_RETURN_ROUTING_KEY);
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
