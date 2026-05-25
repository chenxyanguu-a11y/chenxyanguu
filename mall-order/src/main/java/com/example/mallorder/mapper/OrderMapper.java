package com.example.mallorder.mapper;

import com.example.mallorder.entity.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrderMapper {

    @Insert("""
            insert into orders(
                order_no,
                user_id,
                product_id,
                product_name,
                product_image,
                product_price,
                quantity,
                total_amount,
                order_status,
                expire_time
            ) values (
                #{orderNo},
                #{userId},
                #{productId},
                #{productName},
                #{productImage},
                #{productPrice},
                #{quantity},
                #{totalAmount},
                #{orderStatus},
                #{expireTime}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOrder(Order order);

    @Select("""
            select id,
                   order_no,
                   user_id,
                   product_id,
                   product_name,
                   product_image,
                   product_price,
                   quantity,
                   total_amount,
                   order_status,
                   expire_time,
                   create_time,
                   pay_time,
                   cancel_time
            from orders
            where order_no = #{orderNo}
            """)
    Order selectByOrderNo(String orderNo);

    @Update("""
            update orders
            set order_status = 1,
                pay_time = now()
            where order_no = #{orderNo}
              and user_id = #{userId}
              and order_status = 0
            """)
    int markPaid(@Param("orderNo") String orderNo, @Param("userId") Long userId);

    @Update("""
            update orders
            set order_status = 2,
                cancel_time = now()
            where order_no = #{orderNo}
              and order_status = 0
            """)
    int cancelPendingOrder(String orderNo);
}
