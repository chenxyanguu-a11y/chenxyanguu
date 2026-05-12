package com.example.mallproduct.mapper;

import com.example.mallproduct.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
            where id > #{lastId}
            order by id asc
            limit #{limit}
            """)
    List<Product> selectNextBatch(@Param("lastId") Long lastId, @Param("limit") int limit);
}
