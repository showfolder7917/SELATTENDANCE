package com.sp.selfsp.uniauth.module.dao;

import com.sp.selfsp.uniauth.module.domain.in.UniauthModuleSaveIn;
import com.sp.selfsp.uniauth.module.domain.out.UniauthModuleItemOut;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 模块 DAO 只负责模块主表读写，避免把菜单和权限定义耦合进模块维护链路。
@Mapper
public interface UniauthModuleDao {

    // 新增模块主数据，供首次接入新工程或新业务域使用。
    int insertModule(@Param("in") UniauthModuleSaveIn saveIn);

    // 更新模块主数据，供后续调整模块名称、入口和启停状态使用。
    int updateModule(@Param("in") UniauthModuleSaveIn saveIn);

    // 按模块编码回查正式落库结果，保证返回对象与数据库一致。
    UniauthModuleItemOut selectModuleByCode(@Param("moduleCode") String moduleCode);
}
