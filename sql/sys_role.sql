CREATE TABLE sys_role (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
                          role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
                          role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
                          description VARCHAR(255) DEFAULT NULL COMMENT '描述',
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          UNIQUE KEY uk_role_code (role_code)
) COMMENT='角色表';