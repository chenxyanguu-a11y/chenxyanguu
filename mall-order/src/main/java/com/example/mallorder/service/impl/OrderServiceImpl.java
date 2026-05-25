package com.example.mallorder.service.impl;

import com.example.mallorder.dto.CreateOrderDTO;
import com.example.mallorder.entity.Order;
import com.example.mallorder.entity.Product;
import com.example.mallorder.exception.BusinessException;
import com.example.mallorder.mapper.OrderMapper;
import com.example.mallorder.mapper.ProductMapper;
import com.example.mallorder.mq.OrderDelayMessageSender;
import com.example.mallorder.service.OrderService;
import com.example.mallorder.vo.OrderVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderServiceImpl implements OrderService {

    private static final int PRODUCT_STATUS_ON_SALE = 1;
    private static final int PRODUCT_AUDIT_PASSED = 1;
    private static final int PRODUCT_NOT_DELETED = 0;
    private static final int ORDER_STATUS_PENDING_PAY = 0;
    private static final int ORDER_STATUS_PAID = 1;
    private static final int ORDER_STATUS_CANCELED = 2;
    private static final DateTimeFormatter ORDER_NO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final OrderDelayMessageSender orderDelayMessageSender;

    public OrderServiceImpl(OrderMapper orderMapper,
                            ProductMapper productMapper,
                            OrderDelayMessageSender orderDelayMessageSender) {
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
        this.orderDelayMessageSender = orderDelayMessageSender;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(CreateOrderDTO createOrderDTO, Long userId) {
        validateUserId(userId);
        if (createOrderDTO == null || createOrderDTO.getProductId() == null) {
            throw new BusinessException(400, "商品ID不能为空");
        }
        if (createOrderDTO.getQuantity() == null || createOrderDTO.getQuantity() <= 0) {
            throw new BusinessException(400, "购买数量必须大于0");
        }

        Product product = productMapper.selectById(createOrderDTO.getProductId());
        validateProduct(product);

        int affectedRows = productMapper.deductStock(createOrderDTO.getProductId(), createOrderDTO.getQuantity());
        if (affectedRows == 0) {
            throw new BusinessException(400, "库存不足，下单失败");
        }

        Order order = buildOrder(product, createOrderDTO.getQuantity(), userId);
        orderMapper.insertOrder(order);
        sendDelayMessageAfterCommit(order.getOrderNo());
        return toOrderVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO payOrder(String orderNo, Long userId) {
        validateUserId(userId);
        Order order = getExistingOrder(orderNo);
        validateOrderOwner(order, userId);

        if (Objects.equals(order.getOrderStatus(), ORDER_STATUS_PAID)) {
            throw new BusinessException(400, "订单已支付，请勿重复支付");
        }
        if (Objects.equals(order.getOrderStatus(), ORDER_STATUS_CANCELED)) {
            throw new BusinessException(400, "订单已取消，不能支付");
        }
        if (!Objects.equals(order.getOrderStatus(), ORDER_STATUS_PENDING_PAY)) {
            throw new BusinessException(400, "订单状态异常，不能支付");
        }

        int affectedRows = orderMapper.markPaid(orderNo, userId);
        if (affectedRows == 0) {
            throw new BusinessException(400, "支付失败，订单状态已变化");
        }
        return toOrderVO(orderMapper.selectByOrderNo(orderNo));
    }

    @Override
    public OrderVO getOrderDetail(String orderNo, Long userId) {
        validateUserId(userId);
        Order order = getExistingOrder(orderNo);
        validateOrderOwner(order, userId);
        return toOrderVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrderIfPending(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || !Objects.equals(order.getOrderStatus(), ORDER_STATUS_PENDING_PAY)) {
            return;
        }

        int affectedRows = orderMapper.cancelPendingOrder(orderNo);
        if (affectedRows == 0) {
            return;
        }
        productMapper.restoreStock(order.getProductId(), order.getQuantity());
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "用户ID不能为空");
        }
    }

    private void validateProduct(Product product) {
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (!Objects.equals(product.getStatus(), PRODUCT_STATUS_ON_SALE)) {
            throw new BusinessException(400, "商品未上架");
        }
        if (!Objects.equals(product.getAuditStatus(), PRODUCT_AUDIT_PASSED)) {
            throw new BusinessException(400, "商品未审核通过");
        }
        if (!Objects.equals(product.getDeleted(), PRODUCT_NOT_DELETED)) {
            throw new BusinessException(400, "商品不存在");
        }
    }

    private Order buildOrder(Product product, Integer quantity, Long userId) {
        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        LocalDateTime now = LocalDateTime.now();

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setProductId(product.getId());
        order.setProductName(product.getProductName());
        order.setProductImage(product.getMainImage());
        order.setProductPrice(product.getPrice());
        order.setQuantity(quantity);
        order.setTotalAmount(totalAmount);
        order.setOrderStatus(ORDER_STATUS_PENDING_PAY);
        order.setExpireTime(now.plusMinutes(15));
        order.setCreateTime(now);
        return order;
    }

    private String generateOrderNo() {
        String timePart = LocalDateTime.now().format(ORDER_NO_TIME_FORMATTER);
        int randomPart = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return timePart + randomPart;
    }

    private void sendDelayMessageAfterCommit(String orderNo) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orderDelayMessageSender.sendOrderDelayMessage(orderNo);
            }
        });
    }

    private Order getExistingOrder(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            throw new BusinessException(400, "订单号不能为空");
        }
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        return order;
    }

    private void validateOrderOwner(Order order, Long userId) {
        if (!Objects.equals(order.getUserId(), userId)) {
            throw new BusinessException(403, "无权操作该订单");
        }
    }

    private OrderVO toOrderVO(Order order) {
        OrderVO orderVO = new OrderVO();
        orderVO.setOrderNo(order.getOrderNo());
        orderVO.setUserId(order.getUserId());
        orderVO.setProductId(order.getProductId());
        orderVO.setProductName(order.getProductName());
        orderVO.setProductImage(order.getProductImage());
        orderVO.setProductPrice(order.getProductPrice());
        orderVO.setQuantity(order.getQuantity());
        orderVO.setOrderStatus(order.getOrderStatus());
        orderVO.setTotalAmount(order.getTotalAmount());
        orderVO.setExpireTime(order.getExpireTime());
        orderVO.setCreateTime(order.getCreateTime());
        orderVO.setPayTime(order.getPayTime());
        orderVO.setCancelTime(order.getCancelTime());
        return orderVO;
    }
}
