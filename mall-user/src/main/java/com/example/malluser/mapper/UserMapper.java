package com.example.malluser.mapper;

import com.example.malluser.entity.SysUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("select * from sys_user where username=#{username}")
    SysUser selectByUsername(String username);

    @Insert("insert into sys_user(username,password,nickname,phone,email,user_type,status,deleted) values(#{username},#{password},#{nickname},#{phone},#{email},#{userType},#{status},#{deleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUser(SysUser sysUser);

    @Insert("insert into sys_user_role(user_id,role_id) values(#{userId},#{roleId})")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
