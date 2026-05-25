package com.example.malluser.mapper;

import com.example.malluser.dto.UserInfoResponse;
import com.example.malluser.entity.SysUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("select * from sys_user where username=#{username}")
    SysUser selectByUsername(String username);

    @Insert("insert into sys_user(username,password,nickname,phone,email,user_type,status,deleted) values(#{username},#{password},#{nickname},#{phone},#{email},#{userType},#{status},#{deleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUser(SysUser sysUser);

    @Insert("insert into sys_user_role(user_id,role_id) values(#{userId},#{roleId})")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Select("""
            select
                u.id,
                u.username,
                u.nickname,
                u.phone,
                u.email,
                u.user_type as userType,
                u.status,
                u.deleted,
                ur.role_id   -- 现在可以正常访问了
            from sys_user u
            left join sys_user_role ur on u.id = ur.user_id -- 使用左连接关联角色表
            where u.id <> #{currentUserId}
              and not exists (
                  select 1
                  from sys_user_role sub_ur
                  where sub_ur.user_id = u.id
                    and sub_ur.role_id = 1
              )
            """)
    List<UserInfoResponse> selectNonAdminUsers(@Param("currentUserId") Long currentUserId);
}
