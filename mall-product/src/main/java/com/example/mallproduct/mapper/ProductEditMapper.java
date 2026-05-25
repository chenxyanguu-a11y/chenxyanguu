package com.example.mallproduct.mapper;

import com.example.mallproduct.dto.ProductUpdateDTO;
import com.example.mallproduct.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ProductEditMapper {

    @Select("""
            select p.id,
                   p.merchant_id,
                   p.category_id,
                   p.product_name,
                   p.product_desc,
                   p.price,
                   p.main_image,
                   coalesce(ps.available_stock, 0) as available_stock,
                   p.status,
                   p.audit_status,
                   p.deleted,
                   p.create_time
            from product p
            left join product_stock ps on ps.product_id = p.id
            where p.id = #{id}
            """)
    Product selectById(Long id);

    @Update("""
            update product
            set category_id = #{dto.categoryId},
                product_name = #{dto.productName},
                product_desc = #{dto.productDesc},
                price = #{dto.price},
                main_image = #{dto.mainImage},
                status = #{status},
                audit_status = #{auditStatus}
            where id = #{id}
            """)
    int updateProductById(@Param("id") Long id,
                          @Param("dto") ProductUpdateDTO productUpdateDTO,
                          @Param("status") Integer status,
                          @Param("auditStatus") Integer auditStatus);

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
            order by id desc
            """)
    List<Product> selectAllProducts();

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
            where product_name like concat('%', #{productName}, '%')
            order by id desc
            """)
    List<Product> selectByProductName(String productName);

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
            where product_name like concat('%', #{productName}, '%')
              and merchant_id = #{merchantId}
            order by id desc
            """)
    List<Product> selectByProductNameAndMerchantId(@Param("productName") String productName,
                                                   @Param("merchantId") Long merchantId);
}
