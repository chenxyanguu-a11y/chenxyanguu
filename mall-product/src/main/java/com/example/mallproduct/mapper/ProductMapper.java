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
