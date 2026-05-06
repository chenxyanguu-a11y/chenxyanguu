CREATE TABLE sys_permission (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
                                permission_code VARCHAR(100) NOT NULL COMMENT '权限编码',
                                permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
                                path VARCHAR(200) DEFAULT NULL COMMENT '接口路径',
                                method VARCHAR(10) DEFAULT NULL COMMENT '请求方式',
                                UNIQUE KEY uk_permission_code (permission_code),
                                KEY idx_path (path)
) COMMENT='权限表';