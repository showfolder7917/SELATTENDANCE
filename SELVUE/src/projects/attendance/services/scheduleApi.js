import { requestJson } from '../../../shared/services/request'

// 把筛选对象转成查询串，保证排班看板、未排班检查和导出共用同一套过滤口径。
const buildScheduleQuery = (params = {}) => {
  // URLSearchParams 负责把非空筛选项拼成浏览器可识别的查询字符串。
  const searchParams = new URLSearchParams()
  // 月份是排班页面的核心条件，没有月份就无法确定日历区间。
  searchParams.set('month', params.month)
  // 只把真正传入的筛选项带给后端，避免空值污染查询条件。
  Object.entries(params).forEach(([key, value]) => {
    if (key === 'month') return
    if (value === '' || value === null || typeof value === 'undefined') return
    searchParams.set(key, String(value))
  })
  // 返回完整查询串，让服务函数直接拼到接口路径上。
  return searchParams.toString()
}

// 导出 查询排班看板 前端服务动作，供页面读取当前月份的员工行和排班格子。
export const fetchScheduleBoard = (params) =>
  requestJson(`/api/attendance/schedules?${buildScheduleQuery(params)}`)

// 导出 新增单日排班 前端服务动作，供点击日历格子时保存排班。
export const createSchedule = (payload) =>
  requestJson('/api/attendance/schedules', { method: 'POST', body: JSON.stringify(payload) })

// 导出 更新单日排班 前端服务动作，供后续按 id 修改既有排班。
export const updateSchedule = (id, payload) =>
  requestJson(`/api/attendance/schedules/${id}`, { method: 'PUT', body: JSON.stringify(payload) })

// 导出 删除单日排班 前端服务动作，供右侧详情区清空当前格子。
export const deleteSchedule = (id) =>
  requestJson(`/api/attendance/schedules/${id}`, { method: 'DELETE' })

// 导出 批量排班 前端服务动作，供向导一次性给多员工多日期套班次。
export const batchAssignSchedules = (payload) =>
  requestJson('/api/attendance/schedules/batch-assign', { method: 'POST', body: JSON.stringify(payload) })

// 导出 复制上周排班 前端服务动作，供当前月份快速继承上一周样板。
export const copyLastWeekSchedules = (payload) =>
  requestJson('/api/attendance/schedules/copy-last-week', { method: 'POST', body: JSON.stringify(payload) })

// 导出 复制上月排班 前端服务动作，供整月排班快速承接上月结构。
export const copyLastMonthSchedules = (payload) =>
  requestJson('/api/attendance/schedules/copy-last-month', { method: 'POST', body: JSON.stringify(payload) })

// 导出 查询未排班检查 前端服务动作，供右侧提醒卡列出仍需处理的员工与日期。
export const checkUnassignedSchedules = (params) =>
  requestJson(`/api/attendance/schedules/unassigned-check?${buildScheduleQuery(params)}`)

// 导出 导出排班表 前端服务动作，供页面下载当前筛选结果对应的 CSV。
export const exportSchedules = (params) =>
  requestJson(`/api/attendance/schedules/export?${buildScheduleQuery(params)}`)
