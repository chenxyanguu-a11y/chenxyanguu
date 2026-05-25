package com.example.mallproduct.es;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(indexName = "product")
public class ProductDoc {

    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long merchantId;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String productName;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String productDesc;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Keyword)
    private String mainImage;

    @Field(type = FieldType.Integer)
    private Integer availableStock;

    /**
     * 状态：0下架，1上架，2待审核
     */
    @Field(type = FieldType.Integer)
    private Integer status;

    /**
     * 审核状态：0待审核，1通过，2拒绝
     */
    @Field(type = FieldType.Integer)
    private Integer auditStatus;

    /**
     * 逻辑删除：0未删除，1已删除
     */
    @Field(type = FieldType.Integer)
    private Integer deleted;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createTime;
}
