package com.example.mallorder.config;

import com.example.mallorder.constant.OrderRabbitConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange orderDelayExchange() {
        return new DirectExchange(OrderRabbitConstants.ORDER_DELAY_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange orderReleaseExchange() {
        return new DirectExchange(OrderRabbitConstants.ORDER_RELEASE_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", OrderRabbitConstants.ORDER_DELAY_MILLISECONDS);
        arguments.put("x-dead-letter-exchange", OrderRabbitConstants.ORDER_RELEASE_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", OrderRabbitConstants.ORDER_RELEASE_ROUTING_KEY);
        return new Queue(OrderRabbitConstants.ORDER_DELAY_QUEUE, true, false, false, arguments);
    }

    @Bean
    public Queue orderReleaseQueue() {
        return new Queue(OrderRabbitConstants.ORDER_RELEASE_QUEUE, true);
    }

    @Bean
    public Binding orderDelayBinding(@Qualifier("orderDelayQueue") Queue orderDelayQueue,
                                     @Qualifier("orderDelayExchange") DirectExchange orderDelayExchange) {
        return BindingBuilder.bind(orderDelayQueue)
                .to(orderDelayExchange)
                .with(OrderRabbitConstants.ORDER_DELAY_ROUTING_KEY);
    }

    @Bean
    public Binding orderReleaseBinding(@Qualifier("orderReleaseQueue") Queue orderReleaseQueue,
                                       @Qualifier("orderReleaseExchange") DirectExchange orderReleaseExchange) {
        return BindingBuilder.bind(orderReleaseQueue)
                .to(orderReleaseExchange)
                .with(OrderRabbitConstants.ORDER_RELEASE_ROUTING_KEY);
    }
}
