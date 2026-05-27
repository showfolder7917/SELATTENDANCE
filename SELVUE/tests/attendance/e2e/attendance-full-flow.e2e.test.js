// 引入 Playwright 断言与测试工具，在真实浏览器里把 attendance 从主数据维护一直跑到排班落地与导出。
import { expect, test } from '@playwright/test'
import { registerMockAttendanceApi } from './mockAttendanceApi.js'

test.describe('attendance e2e full flow', () => {
  // 这一条用例串起租户、场所、部门、员工、映射、导入、班次和排班流程，验证前端关键业务链在真实浏览器中可贯通。
  test('runs the attendance business flow from tenant setup to batch scheduling', async ({ page }) => {
    const { requestLog } = await registerMockAttendanceApi(page)
    const sectionNav = page.locator('.selattendance-section-menu')

    // 批量排班和复制动作都依赖浏览器确认框，这里统一自动确认，保证流程能走完整条业务链。
    page.on('dialog', async (dialog) => {
      await dialog.accept()
    })

    // 先打开 attendance 工程入口，后续所有表单、导航和排班动作都在真实工作台页面上完成。
    await page.goto('/?project=attendance')

    // 在向导页回写租户资料，验证首页基础信息保存链路和保存反馈文案可用。
    await page.getByLabel('公司编码').fill('TENANT_001')
    await page.getByLabel('公司/教室名称').fill('东京第一教室')
    await page.getByLabel('联系人').fill('山田老师')
    await page.getByLabel('联系电话').fill('03-1111-2222')
    await page.getByLabel('联系邮箱').fill('school@example.com')
    await page.getByRole('button', { name: '保存' }).click()
    await expect(page.getByText('已保存')).toBeVisible()

    // 切到事业所模块并创建第一个事业所，给后续部门、员工和排班提供上游主数据。
    await sectionNav.getByRole('button', { name: /^事业所/ }).click()
    const workplaceForm = page.locator('article').filter({ has: page.getByLabel('事业所编码') })
    await workplaceForm.getByLabel('事业所编码').fill('WK001')
    await workplaceForm.getByLabel('事业所名称').fill('新宿校')
    await workplaceForm.getByLabel('地址').fill('东京新宿区 1-2-3')
    await workplaceForm.getByLabel('电话').fill('03-2222-3333')
    await workplaceForm.getByRole('button', { name: '保存' }).click()
    await expect(page.getByRole('cell', { name: '新宿校' })).toBeVisible()

    // 切到部门模块并登记新部门，验证事业所到部门的主数据链已经贯通。
    await sectionNav.getByRole('button', { name: /^部门/ }).click()
    const departmentForm = page.locator('article').filter({ has: page.getByLabel('部门编码') })
    await departmentForm.getByLabel('部门编码').fill('DEP001')
    await departmentForm.getByLabel('部门名称').fill('教务部')
    await departmentForm.getByLabel('排序').fill('10')
    await departmentForm.getByRole('button', { name: '保存' }).click()
    await expect(page.getByRole('cell', { name: '教务部' })).toBeVisible()

    // 切到员工模块并创建首个员工，验证部门和事业所默认值能直接进入员工保存链路。
    await sectionNav.getByRole('button', { name: /^员工/ }).click()
    const employeeForm = page.locator('article').filter({ has: page.getByLabel('员工编号') })
    await employeeForm.getByLabel('员工编号').fill('E001')
    await employeeForm.getByLabel('员工姓名').fill('佐藤花子')
    await employeeForm.getByLabel('员工假名').fill('サトウハナコ')
    await employeeForm.getByLabel('雇佣类型').fill('FULL_TIME')
    await employeeForm.getByLabel('入社日').fill('2026-05-01')
    await employeeForm.locator('.seladmin-action-row').first().getByRole('button', { name: '保存' }).click()
    await expect(page.getByRole('cell', { name: /佐藤花子/ })).toBeVisible()

    // 在同一模块绑定外部打卡 ID，验证员工映射这条独立回写链也能贯通。
    await page.getByRole('button', { name: '绑定打卡 ID' }).first().click()
    const mappingSection = page.locator('.seladmin-subsection').filter({ has: page.getByText('绑定打卡 ID') })
    await mappingSection.getByLabel('外部系统').fill('KING_OF_TIME')
    await mappingSection.getByLabel('外部打卡 ID').fill('EXT-001')
    await mappingSection.getByLabel('外部打卡编号').fill('CARD-001')
    await mappingSection.getByRole('button', { name: '保存' }).click()
    await expect(page.getByText('已保存').last()).toBeVisible()

    // 继续在员工模块做 CSV 导入与导出，验证批量导入和文件导出接口都被真实页面触发。
    const importSection = page.locator('.seladmin-subsection').filter({ has: page.getByText('员工 CSV 导入') })
    await importSection
      .locator('textarea')
      .fill(
        'employeeNo,employeeName,employeeNameKana,employmentType,workplaceCode,departmentCode,hireDate,email,phone\nE002,田中次郎,タナカジロウ,PART_TIME,WK001,DEP001,2026-05-02,tanaka@example.com,03-9999-0000'
      )
    await importSection.getByRole('button', { name: '导入 CSV' }).click()
    await expect(importSection.getByText('1 / 0')).toBeVisible()
    await importSection.getByRole('button', { name: '导出 CSV' }).click()
    await expect.poll(() => requestLog.employeeExportCount).toBe(1)

    // 切到班次模板模块并创建一个真实排班模板，给排班向导提供可选模板。
    await sectionNav.getByRole('button', { name: /^班次模板/ }).click()
    const shiftForm = page.locator('article').filter({ has: page.getByLabel('模板编码') })
    await shiftForm.getByLabel('模板编码').fill('SHIFT_AM')
    await shiftForm.getByLabel('模板名称').fill('早班')
    await shiftForm.getByLabel('班次类型').fill('WORK')
    await shiftForm.getByLabel('颜色').fill('BLUE')
    await shiftForm.getByLabel('开始时间').fill('09:00:00')
    await shiftForm.getByLabel('结束时间').fill('18:00:00')
    await shiftForm.getByLabel('休息分钟').fill('60')
    await shiftForm.getByRole('button', { name: '保存' }).click()
    await expect(page.getByRole('cell', { name: '早班' })).toBeVisible()

    // 切到排班管理后先做一次未排班检查，验证员工主数据已经进入排班看板且能识别缺口。
    await sectionNav.getByRole('button', { name: /^排班管理/ }).click()
    const scheduleActionBar = page.locator('.selattendance-schedule-actionbar')
    await expect(page.locator('.selattendance-schedule-grid').getByText('佐藤花子')).toBeVisible()
    await scheduleActionBar.getByRole('button', { name: '检查未排班' }).click()
    await expect(page.getByText('仍需补排班的员工')).toBeVisible()
    await expect.poll(() => requestLog.unassignedCheckCount).toBe(1)

    // 打开批量排班向导并沿 5 步完整走完，验证这条长链路可以真正把班次写到日历看板。
    await scheduleActionBar.getByRole('button', { name: '批量排班' }).click()
    const batchActionRow = page.locator('.selattendance-schedule-side .seladmin-action-row').last()
    await batchActionRow.getByRole('button', { name: '下一步' }).click()
    const monthValue = await page.getByLabel('排班月份').inputValue()
    await page.getByLabel('开始日期').fill(`${monthValue}-01`)
    await page.getByLabel('结束日期').fill(`${monthValue}-03`)
    await batchActionRow.getByRole('button', { name: '下一步' }).click()
    await page.locator('.selattendance-batch-panel').getByRole('button', { name: '早班' }).click()
    await batchActionRow.getByRole('button', { name: '下一步' }).click()
    await batchActionRow.getByRole('button', { name: '下一步' }).click()
    await page.getByRole('button', { name: '确认批量排班' }).click()
    await expect(page.getByText(/批量排班完成：新增/)).toBeVisible()
    await expect(page.locator('.selattendance-schedule-grid').getByText('早班').first()).toBeVisible()

    // 批量排班落地后再做一次未排班检查并导出排班，验证下游检查与导出链路也能复用同一批数据。
    await scheduleActionBar.getByRole('button', { name: '检查未排班' }).click()
    await expect.poll(() => requestLog.unassignedCheckCount).toBe(2)
    await expect(page.getByText('仍需补排班的员工')).toHaveCount(0)
    await scheduleActionBar.getByRole('button', { name: '导出排班表' }).click()
    await expect.poll(() => requestLog.scheduleExportCount).toBe(1)
  })
})
