package com.example.mallauth.mapper;

import com.example.mallauth.dto.Role;
import com.example.mallauth.entity.SysUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("select * from sys_user where username=#{username}")
    SysUser SelectByName(String username);

    @Select("select r.role_id from sys_user_role r join sys_user u on u.id=r.user_id where u.username=#{username}")
    Integer SelectRoleIdByName(String username);
}
