package com.example.mallorder.mq;

import com.example.mallorder.constant.OrderRabbitConstants;
import com.example.mallorder.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderDelayConsumer {

    private final OrderService orderService;

    public OrderDelayConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = OrderRabbitConstants.ORDER_RELEASE_QUEUE)
    public void handleOrderRelease(String orderNo) {
        orderService.cancelOrderIfPending(orderNo);
    }
}
