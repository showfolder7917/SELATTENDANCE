package com.sp.selfsp.uniauth.user.dao;

import com.sp.selfsp.uniauth.user.domain.in.UniauthUserSaveIn;
import com.sp.selfsp.uniauth.user.domain.out.UniauthUserItemOut;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 用户 DAO 只承接账号主表和用户角色关系读写，不再混入租户、角色、菜单 SQL。
@Mapper
public interface UniauthUserDao {

    // 新增账号主表记录，供平台或租户管理员建立新账号。
    int insertUser(@Param("in") UniauthUserSaveIn in, @Param("passwordHash") String passwordHash);

    // 更新账号主表记录，供管理员维护用户资料与可选密码重置。
    int updateUser(@Param("in") UniauthUserSaveIn in, @Param("passwordHash") String passwordHash);

    // 按登录名回查账号主数据，供新增后拿到真实用户主键。
    UniauthUserItemOut selectUserByLoginName(@Param("loginName") String loginName);

    // 按主键回查账号主数据，供保存后返回真实落库结果。
    UniauthUserItemOut selectUserById(@Param("id") Long id);

    // 删除当前账号旧角色关系，准备重建最终角色集合。
    int deleteUserRoles(@Param("userId") Long userId);

    // 写入一条账号与角色关系，表达当前账号拥有某个角色。
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
