package com.sp.selfsp.uniauth.role.dao;

import com.sp.selfsp.uniauth.role.domain.in.UniauthRoleSaveIn;
import com.sp.selfsp.uniauth.role.domain.out.UniauthRoleItemOut;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 角色 DAO 只承接角色主表、角色权限、角色菜单和角色数据范围关系读写。
@Mapper
public interface UniauthRoleDao {

    // 新增角色主表记录，供平台或租户管理员建立新角色。
    int insertRole(@Param("in") UniauthRoleSaveIn in);

    // 更新角色主表记录，供管理员调整角色名称和说明。
    int updateRole(@Param("in") UniauthRoleSaveIn in);

    // 按角色编码回查角色主数据，供新增后获取真实主键。
    UniauthRoleItemOut selectRoleByCode(@Param("roleCode") String roleCode);

    // 按主键回查角色主数据，供保存后返回真实结果。
    UniauthRoleItemOut selectRoleById(@Param("id") Long id);

    // 删除旧角色权限关系，准备重建最终权限集合。
    int deleteRolePermissions(@Param("roleId") Long roleId);

    // 删除旧角色菜单关系，准备重建最终菜单集合。
    int deleteRoleMenus(@Param("roleId") Long roleId);

    // 删除旧角色数据范围，准备重建最终范围表达。
    int deleteRoleDataScopes(@Param("roleId") Long roleId);

    // 按权限码查询权限主键列表，供角色权限关系写入复用。
    List<Long> selectPermissionIdsByCodes(@Param("codes") List<String> codes);

    // 按菜单码查询菜单主键列表，供角色菜单关系写入复用。
    List<Long> selectMenuIdsByCodes(@Param("codes") List<String> codes);

    // 写入一条角色与权限关系。
    int insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    // 写入一条角色与菜单关系。
    int insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    // 写入一条角色与数据范围关系。
    int insertRoleDataScope(
        @Param("roleId") Long roleId,
        @Param("moduleCode") String moduleCode,
        @Param("scopeType") String scopeType,
        @Param("scopeValue") String scopeValue
    );
}
