CREATE TABLE order_item (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单详情ID',
                            order_id BIGINT NOT NULL COMMENT '订单ID',
                            order_no VARCHAR(64) NOT NULL COMMENT '订单号',
                            product_id BIGINT NOT NULL COMMENT '商品ID',
                            product_name VARCHAR(100) NOT NULL COMMENT '商品名称',
                            product_image VARCHAR(255) DEFAULT NULL COMMENT '商品图片',
                            product_price DECIMAL(10,2) NOT NULL COMMENT '商品单价',
                            quantity INT NOT NULL COMMENT '购买数量',
                            total_price DECIMAL(10,2) NOT NULL COMMENT '小计金额',
                            create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            KEY idx_order_id (order_id),
                            KEY idx_order_no (order_no),
                            KEY idx_product_id (product_id)
) COMMENT='订单详情表';