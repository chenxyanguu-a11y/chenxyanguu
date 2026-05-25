package com.example.mallproduct.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateDTO {

    private Long categoryId;

    private String productName;

    private String productDesc;

    private BigDecimal price;

    private String mainImage;
}
