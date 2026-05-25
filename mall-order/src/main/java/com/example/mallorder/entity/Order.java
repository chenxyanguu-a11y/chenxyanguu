package com.example.mallorder.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {

    private Long id;

    private String orderNo;

    private Long userId;

    private Long productId;

    private String productName;

    private String productImage;

    private BigDecimal productPrice;

    private Integer quantity;

    private BigDecimal totalAmount;

    private Integer orderStatus;

    private LocalDateTime expireTime;

    private LocalDateTime createTime;

    private LocalDateTime payTime;

    private LocalDateTime cancelTime;
}
