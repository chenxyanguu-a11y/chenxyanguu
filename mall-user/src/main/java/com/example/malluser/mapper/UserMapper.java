package com.example.malluser.mapper;

import com.example.malluser.entity.SysUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("select * from sys_user where username=#{username}")
    SysUser selectByUsername(String username);

    @Insert("insert into sys_user(username,password,phone,user_type,status,deleted) values(#{username},#{password},#{phone},#{userType},#{status},#{deleted})")
    int insertUser(SysUser sysUser);
}
