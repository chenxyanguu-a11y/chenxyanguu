package com.example.mallorder.mapper;

import com.example.mallorder.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper {

    @Select("""
            select id,
                   merchant_id,
                   category_id,
                   product_name,
                   product_desc,
                   price,
                   main_image,
                   status,
                   audit_status,
                   deleted,
                   create_time
            from product
            where id = #{id}
            """)
    Product selectById(Long id);

    @Update("""
            update product_stock
            set available_stock = available_stock - #{quantity}
            where product_id = #{productId}
              and available_stock >= #{quantity}
            """)
    int deductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Update("""
            update product_stock
            set available_stock = available_stock + #{quantity}
            where product_id = #{productId}
            """)
    int restoreStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
