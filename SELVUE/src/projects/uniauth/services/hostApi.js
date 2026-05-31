// 宿主桥接读取接口独立拆分，避免桥接验证和权限中心主接口继续耦合在同一个服务文件。
import { requestJson } from '../../../shared/services/request'

// 宿主上下文接口用于验证 attendance 是否已消费当前统一登录态。
export function fetchAttendanceHostContext() {
  // 宿主上下文是只读校验动作，因此直接走 GET。
  return requestJson('/api/attendance/host/context')
}
