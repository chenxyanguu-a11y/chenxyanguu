package com.example.mallproduct.dto;

import lombok.Data;

@Data
public class CreateOrderDTO {

    private Long productId;

    private Integer quantity;
}
