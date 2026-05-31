// 暴露认证接口，供登录和当前用户快照恢复复用。
export { loginUniauth, fetchCurrentUser } from './authApi'

// 暴露工作台聚合接口，供初始化和刷新入口复用。
export { fetchUniauthWorkbench } from './bootstrapApi'

// 暴露模块写接口，供模块区块独立保存统一权限中心托管模块。
export { saveModule } from './moduleApi'

// 暴露租户写接口，供租户区块独立保存。
export { saveTenant } from './tenantApi'

// 暴露用户写接口，供用户区块独立保存。
export { saveUser } from './userApi'

// 暴露角色写接口，供角色区块独立保存。
export { saveRole } from './roleApi'

// 暴露菜单写接口，供菜单区块独立保存。
export { saveMenu } from './menuApi'

// 暴露宿主桥接读取接口，供权限中心验证 attendance 是否已接入统一登录态。
export { fetchAttendanceHostContext } from './hostApi'
