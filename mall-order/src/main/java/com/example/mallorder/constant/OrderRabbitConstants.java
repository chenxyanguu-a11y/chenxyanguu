package com.example.mallorder.constant;

public final class OrderRabbitConstants {

    private OrderRabbitConstants() {
    }

    public static final String ORDER_DELAY_EXCHANGE = "mall.order.delay.exchange";
    public static final String ORDER_RELEASE_EXCHANGE = "mall.order.release.exchange";
    public static final String ORDER_DELAY_QUEUE = "mall.order.delay.queue";
    public static final String ORDER_RELEASE_QUEUE = "mall.order.release.queue";
    public static final String ORDER_DELAY_ROUTING_KEY = "order.delay";
    public static final String ORDER_RELEASE_ROUTING_KEY = "order.release";
    public static final Integer ORDER_DELAY_MILLISECONDS = 15 * 60 * 1000;
}
