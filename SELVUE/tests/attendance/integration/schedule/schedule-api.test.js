// 模拟共享请求桥，验证 schedule 模块的看板读取、单条排班和批量动作都连接到正确 endpoint。
import { requestJson } from '@shared/services/request.js'
import {
  batchAssignSchedules,
  checkUnassignedSchedules,
  copyLastMonthSchedules,
  copyLastWeekSchedules,
  createSchedule,
  deleteSchedule,
  exportSchedules,
  fetchScheduleBoard,
  updateSchedule
} from '@tests-attendance'

// 用请求 mock 隔离真实网络，把断言聚焦到排班模块的查询串和写接口契约。
vi.mock('@shared/services/request.js', () => ({
  requestJson: vi.fn()
}))

describe('attendance integration schedule api', () => {
  // 每个用例前都重置请求 mock，保证排班不同动作的调用次序和参数断言清晰可读。
  beforeEach(() => {
    requestJson.mockReset()
  })

  // 校验排班看板、未排班检查和导出接口都共用同一套查询串口径。
  it('serializes the schedule filters into board, unassigned-check and export endpoints', async () => {
    requestJson.mockResolvedValue([])

    const params = {
      month: '2026-05',
      workplaceId: 11,
      departmentId: 21,
      employeeKeyword: 'Sato',
      onlyUnassigned: true
    }

    await fetchScheduleBoard(params)
    await checkUnassignedSchedules(params)
    await exportSchedules(params)

    expect(requestJson).toHaveBeenNthCalledWith(
      1,
      '/api/attendance/schedules?month=2026-05&workplaceId=11&departmentId=21&employeeKeyword=Sato&onlyUnassigned=true'
    )
    expect(requestJson).toHaveBeenNthCalledWith(
      2,
      '/api/attendance/schedules/unassigned-check?month=2026-05&workplaceId=11&departmentId=21&employeeKeyword=Sato&onlyUnassigned=true'
    )
    expect(requestJson).toHaveBeenNthCalledWith(
      3,
      '/api/attendance/schedules/export?month=2026-05&workplaceId=11&departmentId=21&employeeKeyword=Sato&onlyUnassigned=true'
    )
  })

  // 校验排班新增、更新、删除和批量动作的路径与方法，保证日历写回链路持续稳定。
  it('writes single and batch schedule actions to the expected endpoints', async () => {
    const schedulePayload = { employeeId: 1, workDate: '2026-05-01' }
    const batchPayload = { employeeIds: [1, 2], month: '2026-05' }

    requestJson.mockResolvedValue({})

    await createSchedule(schedulePayload)
    await updateSchedule(51, schedulePayload)
    await deleteSchedule(51)
    await batchAssignSchedules(batchPayload)
    await copyLastWeekSchedules(batchPayload)
    await copyLastMonthSchedules(batchPayload)

    expect(requestJson).toHaveBeenNthCalledWith(1, '/api/attendance/schedules', {
      method: 'POST',
      body: JSON.stringify(schedulePayload)
    })
    expect(requestJson).toHaveBeenNthCalledWith(2, '/api/attendance/schedules/51', {
      method: 'PUT',
      body: JSON.stringify(schedulePayload)
    })
    expect(requestJson).toHaveBeenNthCalledWith(3, '/api/attendance/schedules/51', {
      method: 'DELETE'
    })
    expect(requestJson).toHaveBeenNthCalledWith(4, '/api/attendance/schedules/batch-assign', {
      method: 'POST',
      body: JSON.stringify(batchPayload)
    })
    expect(requestJson).toHaveBeenNthCalledWith(5, '/api/attendance/schedules/copy-last-week', {
      method: 'POST',
      body: JSON.stringify(batchPayload)
    })
    expect(requestJson).toHaveBeenNthCalledWith(6, '/api/attendance/schedules/copy-last-month', {
      method: 'POST',
      body: JSON.stringify(batchPayload)
    })
  })
})
