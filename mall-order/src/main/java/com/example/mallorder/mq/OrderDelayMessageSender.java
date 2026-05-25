package com.example.mallorder.mq;

import com.example.mallorder.constant.OrderRabbitConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderDelayMessageSender {

    private final RabbitTemplate rabbitTemplate;

    public OrderDelayMessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendOrderDelayMessage(String orderNo) {
        rabbitTemplate.convertAndSend(
                OrderRabbitConstants.ORDER_DELAY_EXCHANGE,
                OrderRabbitConstants.ORDER_DELAY_ROUTING_KEY,
                orderNo
        );
    }
}
