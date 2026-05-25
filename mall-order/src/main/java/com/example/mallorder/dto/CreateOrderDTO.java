package com.example.mallorder.dto;

import lombok.Data;

@Data
public class CreateOrderDTO {

    private Long productId;

    private Integer quantity;
}
