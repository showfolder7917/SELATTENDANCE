package com.sp.selfsp.uniauth.menu.dao;

import com.sp.selfsp.uniauth.menu.domain.in.UniauthMenuSaveIn;
import com.sp.selfsp.uniauth.menu.domain.out.UniauthMenuItemOut;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 菜单 DAO 只承接菜单树节点主表读写，不再混入角色、用户和租户 SQL。
@Mapper
public interface UniauthMenuDao {

    // 新增菜单节点，供权限中心建立新的宿主或平台导航入口。
    int insertMenu(@Param("in") UniauthMenuSaveIn in);

    // 更新菜单节点，供管理员调整路由、标题和启停状态。
    int updateMenu(@Param("in") UniauthMenuSaveIn in);

    // 按菜单编码回查正式结果，供保存后返回真实落库节点。
    UniauthMenuItemOut selectMenuByCode(@Param("menuCode") String menuCode);
}
