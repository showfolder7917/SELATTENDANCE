import { requestJson } from '../../../shared/services/request'

// 读取全部部门列表，供部门区块和员工/排班筛选联动复用。
export const listDepartments = () => requestJson('/api/attendance/departments')

// 新增部门主数据，供部门区块登记新的组织节点。
export const createDepartment = (payload) =>
  requestJson('/api/attendance/departments', { method: 'POST', body: JSON.stringify(payload) })

// 更新部门主数据，供部门区块维护组织结构资料。
export const updateDepartment = (id, payload) =>
  requestJson(`/api/attendance/departments/${id}`, { method: 'PUT', body: JSON.stringify(payload) })

// 删除部门主数据，供部门区块移除不再使用的组织节点。
export const deleteDepartment = (id) =>
  requestJson(`/api/attendance/departments/${id}`, { method: 'DELETE' })
