package com.example.mallorder.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductStock {

    private Long id;

    private Long productId;

    private Integer totalStock;

    private Integer availableStock;

    private Integer lockedStock;

    private Integer version;

    private LocalDateTime updateTime;
}
