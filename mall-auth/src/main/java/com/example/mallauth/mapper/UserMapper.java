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

    @Insert("insert into sys_user(username,password,user_type,status,deleted) values(#{username},#{password},#{userType},#{status},#{deleted})")
    int insertUser(SysUser sysUser);

}
