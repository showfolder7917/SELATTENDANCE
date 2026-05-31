package com.sp.selfsp.uniauth.auth.dao;

import com.sp.selfsp.uniauth.auth.domain.UniauthAuthUser;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 认证 DAO 只承接登录链路和当前用户上下文组装所需查询，避免管理子域 DAO 混进认证 SQL。
@Mapper
public interface UniauthAuthDao {

    // 按登录名读取用户主数据，供登录链路做密码校验与上下文组装。
    UniauthAuthUser selectUserByLoginName(@Param("loginName") String loginName);

    // 按用户主键读取权限码列表，供 JWT 与接口鉴权消费。
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    // 按用户主键读取菜单码列表，供宿主和权限中心动态导航消费。
    List<String> selectMenuCodesByUserId(@Param("userId") Long userId);

    // 按用户主键读取数据范围列表，供 attendance 宿主做查询过滤。
    List<String> selectDataScopesByUserId(@Param("userId") Long userId);

    // 登录成功后写轻量会话记录，供运营和排障追踪最近登录。
    int insertLoginSession(
        @Param("userId") Long userId,
        @Param("refreshTokenHash") String refreshTokenHash,
        @Param("deviceInfo") String deviceInfo,
        @Param("loginIp") String loginIp,
        @Param("expireAt") LocalDateTime expireAt
    );
}
