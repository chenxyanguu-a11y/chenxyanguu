CREATE TABLE seckill_activity (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '秒杀活动ID',
                                  activity_name VARCHAR(100) NOT NULL COMMENT '活动名称',
                                  product_id BIGINT NOT NULL COMMENT '商品ID',
                                  seckill_price DECIMAL(10,2) NOT NULL COMMENT '秒杀价格',
                                  seckill_stock INT NOT NULL COMMENT '秒杀库存',
                                  start_time DATETIME NOT NULL COMMENT '开始时间',
                                  end_time DATETIME NOT NULL COMMENT '结束时间',
                                  status TINYINT DEFAULT 0 COMMENT '状态：0未开始，1进行中，2已结束',
                                  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  KEY idx_product_id (product_id),
                                  KEY idx_start_end_time (start_time, end_time),
                                  KEY idx_status (status)
) COMMENT='秒杀活动表';