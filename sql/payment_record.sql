CREATE TABLE payment_record (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '支付记录ID',
                                order_no VARCHAR(64) NOT NULL COMMENT '订单号',
                                user_id BIGINT NOT NULL COMMENT '用户ID',
                                pay_amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
                                pay_type TINYINT DEFAULT 1 COMMENT '支付方式：1模拟支付',
                                pay_status TINYINT DEFAULT 0 COMMENT '支付状态：0未支付，1支付成功，2支付失败',
                                transaction_no VARCHAR(64) DEFAULT NULL COMMENT '交易流水号',
                                create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
                                UNIQUE KEY uk_transaction_no (transaction_no),
                                KEY idx_order_no (order_no),
                                KEY idx_user_id (user_id),
                                KEY idx_pay_status (pay_status)
) COMMENT='支付记录表';