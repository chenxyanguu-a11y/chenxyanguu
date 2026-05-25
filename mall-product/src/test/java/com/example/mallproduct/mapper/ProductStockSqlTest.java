package com.example.mallproduct.mapper;

import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class ProductStockSqlTest {

    @Test
    void productMapperSelectByIdReadsAvailableStockWithDefaultZero() throws NoSuchMethodException {
        String sql = selectSql(ProductMapper.class.getMethod("selectById", Long.class));

        assertThat(sql).contains("left join product_stock ps on ps.product_id = p.id");
        assertThat(sql).contains("coalesce(ps.available_stock, 0) as available_stock");
    }

    @Test
    void productMapperSelectNextBatchReadsAvailableStockWithDefaultZero() throws NoSuchMethodException {
        String sql = selectSql(ProductMapper.class.getMethod("selectNextBatch", Long.class, int.class));

        assertThat(sql).contains("left join product_stock ps on ps.product_id = p.id");
        assertThat(sql).contains("coalesce(ps.available_stock, 0) as available_stock");
    }

    @Test
    void productEditMapperSelectByIdReadsAvailableStockWithDefaultZero() throws NoSuchMethodException {
        String sql = selectSql(ProductEditMapper.class.getMethod("selectById", Long.class));

        assertThat(sql).contains("left join product_stock ps on ps.product_id = p.id");
        assertThat(sql).contains("coalesce(ps.available_stock, 0) as available_stock");
    }

    private static String selectSql(Method method) {
        return String.join(" ", method.getAnnotation(Select.class).value())
                .replaceAll("\\s+", " ")
                .trim();
    }
}
