package com.example.mallproduct.controller;

import com.example.mallcommon.core.Result;
import com.example.mallproduct.dto.CreateOrderDTO;
import com.example.mallproduct.feign.OrderFeignClient;
import com.example.mallproduct.vo.OrderVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product/order")
public class ProductOrderController {

    private final OrderFeignClient orderFeignClient;

    public ProductOrderController(OrderFeignClient orderFeignClient) {
        this.orderFeignClient = orderFeignClient;
    }

    @PostMapping("/create")
    public Result<OrderVO> createOrder(@RequestBody CreateOrderDTO createOrderDTO,
                                       @RequestHeader("X-User-Id") Long userId) {
        return orderFeignClient.createOrder(createOrderDTO, userId);
    }

    @PostMapping("/pay/{orderNo}")
    public Result<OrderVO> payOrder(@PathVariable String orderNo,
                                    @RequestHeader("X-User-Id") Long userId) {
        return orderFeignClient.payOrder(orderNo, userId);
    }

    @GetMapping("/detail/{orderNo}")
    public Result<OrderVO> getOrderDetail(@PathVariable String orderNo,
                                          @RequestHeader("X-User-Id") Long userId) {
        return orderFeignClient.getOrderDetail(orderNo, userId);
    }
}
