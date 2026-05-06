CREATE TABLE sys_user_role (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                               user_id BIGINT NOT NULL COMMENT '用户ID',
                               role_id BIGINT NOT NULL COMMENT '角色ID',
                               UNIQUE KEY uk_user_role (user_id, role_id),
                               KEY idx_user_id (user_id),
                               KEY idx_role_id (role_id)
) COMMENT='用户角色关联表';