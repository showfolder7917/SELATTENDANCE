// 这里用浏览器侧内存数据模拟 attendance 后端，保证全链路 e2e 关注的是前端真实交互和接口接线，而不是本地后端准备成本。

// 统一把前端 requestJson 期望的成功响应包成 CommonResponse 结构，避免每个路由重复手写壳。
const wrapSuccess = (data) => ({
  code: 0,
  data
})

// 为浏览器测试提供统一默认状态工厂，保证不同 e2e 都能在稳定初始数据上叠加各自夹具。
const createDefaultState = () => ({
  tenant: {
    tenantCode: 'TENANT_DEMO',
    tenantName: 'Tokyo School',
    contactName: 'Initial Owner',
    contactPhone: '03-0000-0000',
    contactEmail: 'owner@example.com',
    timezone: 'Asia/Tokyo'
  },
  workplaces: [],
  departments: [],
  employees: [],
  shiftTemplates: [],
  schedules: []
})

// 把浏览器请求体按 JSON 解析成对象，供内存态 mock 后端读取表单提交内容。
const readJsonBody = async (request) => {
  const bodyText = request.postData() || '{}'
  return JSON.parse(bodyText)
}

// 从查询参数里按月生成 3 天最小日历区间，既能覆盖排班流程，又避免 e2e 为了日历数据准备过多噪音。
const buildScheduleDates = (monthText) => [`${monthText}-01`, `${monthText}-02`, `${monthText}-03`]

// 为主数据记录分配递增主键，保证新增、编辑和删除在同一条 e2e 中都能稳定定位目标记录。
const createIdAllocator = (initialValue = 1) => {
  let nextId = initialValue
  return () => nextId++
}

// 把部门记录补上事业所展示名，保证前端部门表格和筛选依赖的回显字段与真实后端保持一致。
const decorateDepartment = (department, workplaces) => ({
  ...department,
  workplaceName: workplaces.find((item) => item.id === department.workplaceId)?.workplaceName || ''
})

// 把员工记录补上部门名、事业所名和映射状态，供员工表格和排班看板直接消费。
const decorateEmployee = (employee, departments, workplaces) => {
  const department = departments.find((item) => item.id === employee.departmentId)
  const workplace = workplaces.find((item) => item.id === employee.workplaceId)
  return {
    ...employee,
    departmentName: department?.departmentName || '',
    workplaceName: workplace?.workplaceName || '',
    externalMappingBound: Boolean(employee.externalEmployeeId)
  }
}

// 统一基于当前员工、模板和排班项生成排班看板，保证浏览器里看到的日历状态和 mock 后端数据完全同步。
const buildScheduleBoard = ({ monthText, employees, departments, workplaces, shiftTemplates, schedules }) => {
  const dates = buildScheduleDates(monthText)
  const decoratedEmployees = employees.map((employee) => decorateEmployee(employee, departments, workplaces))
  const employeeRows = decoratedEmployees.map((employee) => {
    const unassignedCount = dates.filter(
      (dateText) => !schedules.some((item) => item.employeeId === employee.id && item.workDate === dateText)
    ).length
    return {
      employeeId: employee.id,
      employeeNo: employee.employeeNo,
      employeeName: employee.employeeName,
      departmentName: employee.departmentName,
      workplaceName: employee.workplaceName,
      unassignedCount
    }
  })
  const scheduleItems = schedules
    .filter((item) => dates.includes(item.workDate))
    .map((item) => {
      const template = shiftTemplates.find((entry) => entry.id === item.shiftTemplateId)
      return {
        ...item,
        templateName: template?.templateName || '',
        startTime: template?.startTime || '',
        endTime: template?.endTime || '',
        color: template?.color || 'BLUE'
      }
    })
  return {
    month: monthText,
    dates,
    endDate: dates[dates.length - 1],
    employeeRows,
    scheduleItems,
    shiftTemplates: [...shiftTemplates]
  }
}

// 计算当前筛选范围下仍未排班的员工和日期，供“检查未排班”动作验证真正的业务闭环。
const buildUnassignedItems = ({ monthText, employees, departments, workplaces, schedules, filters }) => {
  const dates = buildScheduleDates(monthText)
  return employees
    .map((employee) => decorateEmployee(employee, departments, workplaces))
    .filter((employee) => {
      if (filters.workplaceId && String(employee.workplaceId) !== String(filters.workplaceId)) return false
      if (filters.departmentId && String(employee.departmentId) !== String(filters.departmentId)) return false
      if (
        filters.employeeKeyword &&
        !`${employee.employeeNo}${employee.employeeName}`.includes(String(filters.employeeKeyword))
      ) {
        return false
      }
      return true
    })
    .map((employee) => {
      const missingDates = dates.filter(
        (dateText) => !schedules.some((item) => item.employeeId === employee.id && item.workDate === dateText)
      )
      return {
        employeeId: employee.id,
        employeeNo: employee.employeeNo,
        employeeName: employee.employeeName,
        unassignedCount: missingDates.length,
        missingDates
      }
    })
    .filter((item) => item.unassignedCount > 0)
}

// 按当前页面筛选条件选出排班看板应展示的员工，保证批量排班和未排班检查命中同一批业务对象。
const filterEmployeesForSchedule = ({ employees, filters }) =>
  employees.filter((employee) => {
    if (filters.workplaceId && String(employee.workplaceId) !== String(filters.workplaceId)) return false
    if (filters.departmentId && String(employee.departmentId) !== String(filters.departmentId)) return false
    if (
      filters.employeeKeyword &&
      !`${employee.employeeNo}${employee.employeeName}`.includes(String(filters.employeeKeyword))
    ) {
      return false
    }
    return true
  })

// 为员工表格视觉回归准备一组高低不一、映射状态不一的数据，专门放大“边框线未对齐”这类布局问题。
export const createEmployeeTableLayoutFixture = () => ({
  workplaces: [
    { id: 11, workplaceCode: 'WK001', workplaceName: '东京本部', address: '', phone: '', status: 'ACTIVE' },
    { id: 12, workplaceCode: 'WK002', workplaceName: '横滨教室', address: '', phone: '', status: 'ACTIVE' }
  ],
  departments: [
    { id: 21, workplaceId: 11, departmentCode: 'DEP001', departmentName: '教学部', sortOrder: 10, status: 'ACTIVE' },
    { id: 22, workplaceId: 12, departmentCode: 'DEP002', departmentName: '横滨运营组', sortOrder: 20, status: 'ACTIVE' }
  ],
  employees: [
    {
      id: 31,
      employeeNo: 'E0001',
      employeeName: '山田太郎',
      employeeNameKana: 'ヤマダタロウ',
      employmentType: 'FULL_TIME',
      hireDate: '2026-05-01',
      workplaceId: 11,
      departmentId: 21,
      status: 'ACTIVE',
      externalEmployeeId: 'KOT-90001',
      externalEmployeeNo: 'CARD-90001',
      externalSourceSystem: 'KING_OF_TIME'
    },
    {
      id: 32,
      employeeNo: 'E0002',
      employeeName: '佐藤花子',
      employeeNameKana: 'サトウハナコ',
      employmentType: 'PART_TIME',
      hireDate: '2026-05-02',
      workplaceId: 11,
      departmentId: 21,
      status: 'ACTIVE',
      externalEmployeeId: 'KOT-90002',
      externalEmployeeNo: 'CARD-90002',
      externalSourceSystem: 'KING_OF_TIME'
    },
    {
      id: 33,
      employeeNo: 'E0003',
      employeeName: '铃木一郎',
      employeeNameKana: 'スズキイチロウ',
      employmentType: 'CONTRACT',
      hireDate: '2026-05-03',
      workplaceId: 12,
      departmentId: 22,
      status: 'ACTIVE',
      externalEmployeeId: 'TOT-30003',
      externalEmployeeNo: 'CARD-30003',
      externalSourceSystem: 'TIME_ON_TIME'
    },
    {
      id: 34,
      employeeNo: 'E0004',
      employeeName: '高桥美咲',
      employeeNameKana: 'タカハシミサキ',
      employmentType: 'ARBEIT',
      hireDate: '2026-05-04',
      workplaceId: 12,
      departmentId: 22,
      status: 'ACTIVE',
      externalEmployeeId: '',
      externalEmployeeNo: '',
      externalSourceSystem: ''
    }
  ]
})

// 注册整套 attendance mock API，并把关键请求次数暴露给 e2e 断言，便于验证导出和检查动作真的发生过。
export const registerMockAttendanceApi = async (page, options = {}) => {
  // 各模块各自维护独立主键序列，模拟真实后端多表自增 ID 的行为，避免跨模块记录互相覆盖。
  const allocWorkplaceId = createIdAllocator(11)
  const allocDepartmentId = createIdAllocator(21)
  const allocEmployeeId = createIdAllocator(31)
  const allocShiftTemplateId = createIdAllocator(41)
  const allocScheduleId = createIdAllocator(51)

  // 这里保存整条考勤业务链在浏览器测试期间的共享后端状态，所有 GET/POST/PUT/DELETE 都直接读写它。
  // 允许每条 e2e 在默认状态上叠加自己的主数据夹具，从而复用同一套 mock API 但验证不同布局和业务场景。
  const state = {
    ...createDefaultState(),
    ...(options.stateOverrides || {})
  }

  // 通过请求计数器确认导出和未排班检查这些“没有稳定页面落点”的动作确实打到了接口层。
  const requestLog = {
    employeeExportCount: 0,
    scheduleExportCount: 0,
    unassignedCheckCount: 0
  }

  await page.route('**/api/attendance/**', async (route) => {
    const request = route.request()
    const url = new URL(request.url())
    const method = request.method()
    const pathname = url.pathname

    // 首页轻量壳始终返回当前租户和推荐动作，保证各模块保存后首屏统计可以重新同步。
    if (pathname === '/api/attendance/bootstrap' && method === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(
          wrapSuccess({
            tenant: state.tenant,
            steps: [
              { key: 'tenant', title: 'wizard.tenant', description: 'wizardHint' },
              { key: 'workspace', title: 'workplaceTitle', description: 'sectionWorkplaceHint' }
            ],
            recommendedNextAction: 'wizard.schedule'
          })
        )
      })
      return
    }

    // 租户独立接口负责读取和回写公司基础信息，供 wizard 面板全链路保存动作使用。
    if (pathname === '/api/attendance/tenant/current' && method === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(wrapSuccess(state.tenant))
      })
      return
    }
    if (pathname === '/api/attendance/tenant/current' && method === 'PUT') {
      state.tenant = { ...state.tenant, ...(await readJsonBody(request)) }
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(wrapSuccess(state.tenant))
      })
      return
    }

    // 场所列表和增删改接口为部门、员工和排班提供最上游主数据。
    if (pathname === '/api/attendance/workplaces' && method === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(wrapSuccess(state.workplaces))
      })
      return
    }
    if (pathname === '/api/attendance/workplaces' && method === 'POST') {
      const payload = await readJsonBody(request)
      state.workplaces.push({ ...payload, id: allocWorkplaceId() })
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(wrapSuccess({})) })
      return
    }
    if (pathname.startsWith('/api/attendance/workplaces/') && method === 'PUT') {
      const workplaceId = Number(pathname.split('/').pop())
      const payload = await readJsonBody(request)
      state.workplaces = state.workplaces.map((item) => (item.id === workplaceId ? { ...item, ...payload } : item))
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(wrapSuccess({})) })
      return
    }
    if (pathname.startsWith('/api/attendance/workplaces/') && method === 'DELETE') {
      const workplaceId = Number(pathname.split('/').pop())
      state.workplaces = state.workplaces.filter((item) => item.id !== workplaceId)
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(wrapSuccess({})) })
      return
    }

    // 部门列表会补齐事业所名称，保证部门表格和跨模块跳转的文案与真实后端一致。
    if (pathname === '/api/attendance/departments' && method === 'GET') {
      const departments = state.departments.map((item) => decorateDepartment(item, state.workplaces))
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(wrapSuccess(departments))
      })
      return
    }
    if (pathname === '/api/attendance/departments' && method === 'POST') {
      const payload = await readJsonBody(request)
      state.departments.push({ ...payload, id: allocDepartmentId(), workplaceId: Number(payload.workplaceId) })
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(wrapSuccess({})) })
      return
    }
    if (pathname.startsWith('/api/attendance/departments/') && method === 'PUT') {
      const departmentId = Number(pathname.split('/').pop())
      const payload = await readJsonBody(request)
      state.departments = state.departments.map((item) =>
        item.id === departmentId ? { ...item, ...payload, workplaceId: Number(payload.workplaceId) } : item
      )
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(wrapSuccess({})) })
      return
    }
    if (pathname.startsWith('/api/attendance/departments/') && method === 'DELETE') {
      const departmentId = Number(pathname.split('/').pop())
      state.departments = state.departments.filter((item) => item.id !== departmentId)
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(wrapSuccess({})) })
      return
    }

    // 员工列表会根据筛选条件返回带部门名、事业所名和映射状态的行数据。
    if (pathname === '/api/attendance/employees' && method === 'GET') {
      const employees = state.employees
        .map((item) => decorateEmployee(item, state.departments, state.workplaces))
        .filter((item) => {
          if (url.searchParams.get('keyword') && !`${item.employeeNo}${item.employeeName}`.includes(url.searchParams.get('keyword'))) return false
          if (url.searchParams.get('departmentId') && String(item.departmentId) !== url.searchParams.get('departmentId')) return false
          if (url.searchParams.get('status') && item.status !== url.searchParams.get('status')) return false
          if (url.searchParams.get('employmentType') && item.employmentType !== url.searchParams.get('employmentType')) return false
          return true
        })
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(wrapSuccess(employees))
      })
      return
    }
    if (pathname === '/api/attendance/employees' && method === 'POST') {
      const payload = await readJsonBody(request)
      // 新建员工时补齐外部映射默认字段，保证后续“绑定打卡 ID”弹层总能读取到完整记录结构。
      state.employees.push({
        ...payload,
        id: allocEmployeeId(),
        workplaceId: Number(payload.workplaceId),
        departmentId: Number(payload.departmentId),
        externalEmployeeId: '',
        externalEmployeeNo: '',
        externalSourceSystem: ''
      })
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(wrapSuccess({})) })
      return
    }
    if (pathname.startsWith('/api/attendance/employees/') && !pathname.endsWith('/external-mapping') && method === 'PUT') {
      const employeeId = Number(pathname.split('/').pop())
      const payload = await readJsonBody(request)
      state.employees = state.employees.map((item) =>
        item.id === employeeId
          ? { ...item, ...payload, workplaceId: Number(payload.workplaceId), departmentId: Number(payload.departmentId) }
          : item
      )
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(wrapSuccess({})) })
      return
    }
    if (pathname.startsWith('/api/attendance/employees/') && !pathname.endsWith('/external-mapping') && method === 'DELETE') {
      const employeeId = Number(pathname.split('/').pop())
      state.employees = state.employees.filter((item) => item.id !== employeeId)
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(wrapSuccess({})) })
      return
    }
    if (pathname.endsWith('/external-mapping') && method === 'PUT') {
      const employeeId = Number(pathname.split('/').slice(-2)[0])
      const payload = await readJsonBody(request)
      state.employees = state.employees.map((item) =>
        item.id === employeeId
          ? {
              ...item,
              externalSourceSystem: payload.sourceSystem,
              externalEmployeeId: payload.externalEmployeeId,
              externalEmployeeNo: payload.externalEmployeeNo
            }
          : item
      )
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(wrapSuccess({})) })
      return
    }
    if (pathname === '/api/attendance/employees/import' && method === 'POST') {
      const payload = await readJsonBody(request)
      // CSV 导入直接复用当前主数据编码做关联，模拟真实批量导入先按编码映射场所和部门的逻辑。
      const lines = String(payload.csvText || '')
        .trim()
        .split(/\r?\n/)
      const [headerLine, ...dataLines] = lines
      const headers = headerLine.split(',')
      let successCount = 0
      dataLines.forEach((line) => {
        if (!line.trim()) return
        const values = line.split(',')
        const record = Object.fromEntries(headers.map((header, index) => [header, values[index] || '']))
        const workplace = state.workplaces.find((item) => item.workplaceCode === record.workplaceCode)
        const department = state.departments.find((item) => item.departmentCode === record.departmentCode)
        if (!workplace || !department) return
        state.employees.push({
          id: allocEmployeeId(),
          employeeNo: record.employeeNo,
          employeeName: record.employeeName,
          employeeNameKana: record.employeeNameKana,
          employmentType: record.employmentType || 'FULL_TIME',
          hireDate: record.hireDate || '',
          workplaceId: workplace.id,
          departmentId: department.id,
          status: 'ACTIVE',
          externalEmployeeId: '',
          externalEmployeeNo: '',
          externalSourceSystem: ''
        })
        successCount += 1
      })
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(wrapSuccess({ successCount, failedCount: 0, errors: [] }))
      })
      return
    }
    if (pathname === '/api/attendance/employees/export' && method === 'GET') {
      requestLog.employeeExportCount += 1
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(
          wrapSuccess({
            fileName: 'employees.csv',
            content: 'employeeNo,employeeName\nE001,Sato'
          })
        )
      })
      return
    }

    // 班次模板接口支撑排班模板建立和后续批量排班模板选择。
    if (pathname === '/api/attendance/shift-templates' && method === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(wrapSuccess(state.shiftTemplates))
      })
      return
    }
    if (pathname === '/api/attendance/shift-templates' && method === 'POST') {
      const payload = await readJsonBody(request)
      state.shiftTemplates.push({ ...payload, id: allocShiftTemplateId() })
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(wrapSuccess({})) })
      return
    }
    if (pathname.startsWith('/api/attendance/shift-templates/') && pathname !== '/api/attendance/shift-templates/recommended' && method === 'PUT') {
      const templateId = Number(pathname.split('/').pop())
      const payload = await readJsonBody(request)
      state.shiftTemplates = state.shiftTemplates.map((item) => (item.id === templateId ? { ...item, ...payload } : item))
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(wrapSuccess({})) })
      return
    }
    if (pathname.startsWith('/api/attendance/shift-templates/') && pathname !== '/api/attendance/shift-templates/recommended' && method === 'DELETE') {
      const templateId = Number(pathname.split('/').pop())
      state.shiftTemplates = state.shiftTemplates.filter((item) => item.id !== templateId)
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(wrapSuccess({})) })
      return
    }
    if (pathname === '/api/attendance/shift-templates/recommended' && method === 'POST') {
      state.shiftTemplates.push({
        id: allocShiftTemplateId(),
        templateCode: 'AUTO_DAY',
        templateName: '推荐早班',
        shiftType: 'WORK',
        color: 'BLUE',
        startTime: '09:00:00',
        endTime: '18:00:00',
        scheduledBreakMinutes: 60,
        crossDay: false,
        active: true
      })
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(wrapSuccess({})) })
      return
    }

    // 排班看板会按当前筛选结果返回员工行、日期列、现有排班项和模板集合。
    if (pathname === '/api/attendance/schedules' && method === 'GET') {
      const monthText = url.searchParams.get('month')
      // 排班页筛选条件和未排班筛选都在这里统一解释，保证看板、检查与批量排班命中同一批员工。
      const filters = {
        workplaceId: url.searchParams.get('workplaceId') || '',
        departmentId: url.searchParams.get('departmentId') || '',
        employeeKeyword: url.searchParams.get('employeeKeyword') || '',
        onlyUnassigned: url.searchParams.get('onlyUnassigned') === 'true'
      }
      const filteredEmployees = filterEmployeesForSchedule({ employees: state.employees, filters })
      const board = buildScheduleBoard({
        monthText,
        employees: filteredEmployees,
        departments: state.departments,
        workplaces: state.workplaces,
        shiftTemplates: state.shiftTemplates,
        schedules: state.schedules
      })
      board.employeeRows = filters.onlyUnassigned ? board.employeeRows.filter((item) => item.unassignedCount > 0) : board.employeeRows
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(wrapSuccess(board))
      })
      return
    }
    if (pathname === '/api/attendance/schedules' && method === 'POST') {
      const payload = await readJsonBody(request)
      state.schedules.push({
        id: allocScheduleId(),
        employeeId: Number(payload.employeeId),
        workDate: payload.workDate,
        shiftTemplateId: Number(payload.shiftTemplateId),
        remark: payload.remark || ''
      })
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(wrapSuccess({})) })
      return
    }
    if (pathname.startsWith('/api/attendance/schedules/') && !pathname.includes('batch-assign') && !pathname.includes('copy-last') && method === 'DELETE') {
      const scheduleId = Number(pathname.split('/').pop())
      state.schedules = state.schedules.filter((item) => item.id !== scheduleId)
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(wrapSuccess({})) })
      return
    }
    if (pathname === '/api/attendance/schedules/batch-assign' && method === 'POST') {
      const payload = await readJsonBody(request)
      // 批量排班既要覆盖“新增排班”，也要覆盖“覆盖已有”和“跳过已有”两条业务分支，所以分别计数。
      let createdCount = 0
      let updatedCount = 0
      let skippedCount = 0
      const dates = buildScheduleDates(String(payload.startDate).slice(0, 7)).filter(
        (dateText) => dateText >= payload.startDate && dateText <= payload.endDate
      )
      payload.employeeIds.forEach((employeeId) => {
        dates.forEach((dateText) => {
          const existingItem = state.schedules.find(
            (item) => item.employeeId === Number(employeeId) && item.workDate === dateText
          )
          if (existingItem && payload.overwriteExisting) {
            existingItem.shiftTemplateId = Number(payload.shiftTemplateId)
            existingItem.remark = payload.remark || ''
            updatedCount += 1
            return
          }
          if (existingItem && payload.skipExisting) {
            skippedCount += 1
            return
          }
          state.schedules.push({
            id: allocScheduleId(),
            employeeId: Number(employeeId),
            workDate: dateText,
            shiftTemplateId: Number(payload.shiftTemplateId),
            remark: payload.remark || ''
          })
          createdCount += 1
        })
      })
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(wrapSuccess({ createdCount, updatedCount, skippedCount }))
      })
      return
    }
    if (pathname === '/api/attendance/schedules/copy-last-week' && method === 'POST') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(wrapSuccess({ createdCount: 0, updatedCount: 0, skippedCount: 0 }))
      })
      return
    }
    if (pathname === '/api/attendance/schedules/copy-last-month' && method === 'POST') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(wrapSuccess({ createdCount: 0, updatedCount: 0, skippedCount: 0 }))
      })
      return
    }
    if (pathname === '/api/attendance/schedules/unassigned-check' && method === 'GET') {
      requestLog.unassignedCheckCount += 1
      const monthText = url.searchParams.get('month')
      const items = buildUnassignedItems({
        monthText,
        employees: state.employees,
        departments: state.departments,
        workplaces: state.workplaces,
        schedules: state.schedules,
        filters: {
          workplaceId: url.searchParams.get('workplaceId') || '',
          departmentId: url.searchParams.get('departmentId') || '',
          employeeKeyword: url.searchParams.get('employeeKeyword') || ''
        }
      })
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(wrapSuccess(items))
      })
      return
    }
    if (pathname === '/api/attendance/schedules/export' && method === 'GET') {
      requestLog.scheduleExportCount += 1
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(
          wrapSuccess({
            fileName: 'schedules.csv',
            content: 'employeeNo,workDate,templateName\nE001,2026-05-01,早班'
          })
        )
      })
      return
    }

    // 任何未声明的 attendance 接口都直接失败，防止前端偷偷依赖了未被这条全链 mock 覆盖的后端路径。
    await route.abort()
  })

  return { state, requestLog }
}
