package com.example.mallproduct.dto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDto {

    /**
     * 搜索关键词，例如：苹果手机
     */
    private String productName;

    /**
     * 分类ID
     */
    private Long categoryId;


    /**
     * 最低价
     */
    private BigDecimal minPrice;

    /**
     * 最高价
     */
    private BigDecimal maxPrice;

    /**
     * 排序字段：price / sales / createTime
     */
    private String sortBy;

    /**
     * asc / desc
     */
    private String sortOrder;

    private Integer pageNum;

    private Integer pageSize ;
}