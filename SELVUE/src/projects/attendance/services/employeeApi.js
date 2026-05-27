import { requestJson } from '../../../shared/services/request'

// 构建员工查询参数，供独立员工区块按筛选条件局部刷新数据。
const buildEmployeeQuery = (params = {}) => {
  // 初始化 URL 查询参数容器，统一承接筛选项的序列化。
  const searchParams = new URLSearchParams()
  // 逐项写入存在值的筛选条件，避免把空条件传给后端造成噪音。
  Object.entries(params).forEach(([key, value]) => {
    // 空串、null 和 undefined 都不写入，保持接口只接收有效筛选项。
    if (value === '' || value === null || typeof value === 'undefined') return
    // 布尔值和数字统一转字符串，保证 URL 查询参数格式稳定。
    searchParams.set(key, String(value))
  })
  // 返回编码后的查询串，供员工独立读取接口拼接使用。
  return searchParams.toString()
}

// 读取员工列表，供员工区块按筛选条件独立加载数据。
export const listEmployees = (params = {}) => {
  // 先构建筛选查询串，避免手写 URL 时漏掉可选参数。
  const query = buildEmployeeQuery(params)
  // 无筛选条件时直接走基础列表接口，避免生成多余的问号。
  if (!query) {
    return requestJson('/api/attendance/employees')
  }
  // 有筛选条件时拼接查询串，按区块需要局部刷新员工数据。
  return requestJson(`/api/attendance/employees?${query}`)
}

// 新增员工主数据，供员工区块登记新员工资料。
export const createEmployee = (payload) =>
  requestJson('/api/attendance/employees', { method: 'POST', body: JSON.stringify(payload) })

// 更新员工主数据，供员工区块维护既有员工资料。
export const updateEmployee = (id, payload) =>
  requestJson(`/api/attendance/employees/${id}`, { method: 'PUT', body: JSON.stringify(payload) })

// 删除员工主数据，供员工区块移除离场或误建员工。
export const deleteEmployee = (id) =>
  requestJson(`/api/attendance/employees/${id}`, { method: 'DELETE' })

// 绑定外部系统映射，供员工区块维护考勤系统与本地员工的对照关系。
export const bindExternalMapping = (id, payload) =>
  requestJson(`/api/attendance/employees/${id}/external-mapping`, { method: 'PUT', body: JSON.stringify(payload) })

// 导入员工 CSV 文本，供员工区块批量落地员工资料。
export const importEmployees = (payload) =>
  requestJson('/api/attendance/employees/import', { method: 'POST', body: JSON.stringify(payload) })

// 导出员工 CSV，供员工区块批量核对或对外交换数据。
export const exportEmployees = () => requestJson('/api/attendance/employees/export')
