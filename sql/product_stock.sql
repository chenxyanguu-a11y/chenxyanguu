CREATE TABLE product_stock (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '库存ID',
                               product_id BIGINT NOT NULL COMMENT '商品ID',
                               total_stock INT NOT NULL COMMENT '总库存',
                               available_stock INT NOT NULL COMMENT '可用库存',
                               locked_stock INT DEFAULT 0 COMMENT '锁定库存',
                               version INT DEFAULT 0 COMMENT '乐观锁版本号',
                               update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               UNIQUE KEY uk_product_id (product_id),
                               KEY idx_available_stock (available_stock)
) COMMENT='库存表';