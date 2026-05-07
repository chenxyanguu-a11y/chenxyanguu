CREATE TABLE orders (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
                        order_no VARCHAR(64) NOT NULL COMMENT '订单号',
                        user_id BIGINT NOT NULL COMMENT '用户ID',
                        total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
                        pay_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
                        order_status TINYINT DEFAULT 0 COMMENT '订单状态：0待支付，1已支付，2已发货，3已完成，4已取消，5已退款',
                        pay_status TINYINT DEFAULT 0 COMMENT '支付状态：0未支付，1已支付',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
                        cancel_time DATETIME DEFAULT NULL COMMENT '取消时间',
                        UNIQUE KEY uk_order_no (order_no),
                        KEY idx_user_id (user_id),
                        KEY idx_order_status (order_status),
                        KEY idx_pay_status (pay_status),
                        KEY idx_create_time (create_time),
                        KEY idx_pay_time (pay_time)
) COMMENT='订单表';