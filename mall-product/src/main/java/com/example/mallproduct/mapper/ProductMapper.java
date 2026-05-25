package com.example.mallproduct.mapper;

import com.example.mallproduct.entity.Product;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper {

    @Insert("""
            insert into product(
                merchant_id,
                category_id,
                product_name,
                product_desc,
                price,
                main_image,
                status,
                audit_status,
                deleted
            ) values (
                #{merchantId},
                #{categoryId},
                #{productName},
                #{productDesc},
                #{price},
                #{mainImage},
                #{status},
                #{auditStatus},
                #{deleted}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertProduct(Product product);

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
            where p.id > #{lastId}
            order by p.id asc
            limit #{limit}
            """)
    List<Product> selectNextBatch(@Param("lastId") Long lastId, @Param("limit") int limit);
}
