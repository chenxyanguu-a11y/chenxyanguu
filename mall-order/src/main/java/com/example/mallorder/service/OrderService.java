package com.example.mallorder.service;

import com.example.mallorder.dto.CreateOrderDTO;
import com.example.mallorder.vo.OrderVO;

public interface OrderService {

    OrderVO createOrder(CreateOrderDTO createOrderDTO, Long userId);

    OrderVO payOrder(String orderNo, Long userId);

    OrderVO getOrderDetail(String orderNo, Long userId);

    void cancelOrderIfPending(String orderNo);
}
