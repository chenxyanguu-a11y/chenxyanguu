package com.example.mallproduct.feign;

import com.example.mallcommon.core.Result;
import com.example.mallproduct.dto.CreateOrderDTO;
import com.example.mallproduct.vo.OrderVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "mall-order")
public interface OrderFeignClient {

    @PostMapping("/internal/order/create")
    Result<OrderVO> createOrder(@RequestBody CreateOrderDTO createOrderDTO,
                                @RequestHeader("X-User-Id") Long userId);

    @PostMapping("/internal/order/pay/{orderNo}")
    Result<OrderVO> payOrder(@PathVariable("orderNo") String orderNo,
                             @RequestHeader("X-User-Id") Long userId);

    @GetMapping("/internal/order/detail/{orderNo}")
    Result<OrderVO> getOrderDetail(@PathVariable("orderNo") String orderNo,
                                   @RequestHeader("X-User-Id") Long userId);
}
