CREATE TABLE product_category (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
                                  parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
                                  category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
                                  sort INT DEFAULT 0 COMMENT '排序',
                                  status TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
                                  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  KEY idx_parent_id (parent_id),
                                  KEY idx_status (status)
) COMMENT='商品分类表';