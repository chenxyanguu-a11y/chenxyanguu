package com.example.mallorder.controller;

import com.example.mallcommon.core.Result;
import com.example.mallorder.dto.CreateOrderDTO;
import com.example.mallorder.service.OrderService;
import com.example.mallorder.vo.OrderVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/order")
public class InternalOrderController {

    private final OrderService orderService;

    public InternalOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public Result<OrderVO> createOrder(@RequestBody CreateOrderDTO createOrderDTO,
                                       @RequestHeader("X-User-Id") Long userId) {
        return new Result<>(200, "下单成功", orderService.createOrder(createOrderDTO, userId));
    }

    @PostMapping("/pay/{orderNo}")
    public Result<OrderVO> payOrder(@PathVariable String orderNo,
                                    @RequestHeader("X-User-Id") Long userId) {
        return new Result<>(200, "支付成功", orderService.payOrder(orderNo, userId));
    }

    @GetMapping("/detail/{orderNo}")
    public Result<OrderVO> getOrderDetail(@PathVariable String orderNo,
                                          @RequestHeader("X-User-Id") Long userId) {
        return new Result<>(200, "查询成功", orderService.getOrderDetail(orderNo, userId));
    }
}
