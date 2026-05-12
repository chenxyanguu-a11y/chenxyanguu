package com.example.mallproduct.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {

    private Long id;

    private Long merchantId;

    private Long categoryId;

    private String productName;

    private String productDesc;

    private BigDecimal price;

    private String mainImage;

    private Integer status;

    private Integer auditStatus;

    private Integer deleted;

    private LocalDateTime createTime;
}
