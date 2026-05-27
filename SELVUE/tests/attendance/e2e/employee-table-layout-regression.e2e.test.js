// 引入 Playwright 断言与员工表格专用夹具，专门保护“数据行存在但表格线没对齐”的视觉布局回归。
import { expect, test } from '@playwright/test'
import { createEmployeeTableLayoutFixture, registerMockAttendanceApi } from './mockAttendanceApi.js'

test.describe('attendance e2e employee table layout regression', () => {
  // 这条用例不验证业务保存，而是用稳定夹具放大员工表格行高差异，直接检查每一行左右单元格是否仍保持对齐。
  test('keeps employee rows aligned between data columns and action column', async ({ page }, testInfo) => {
    await registerMockAttendanceApi(page, {
      stateOverrides: createEmployeeTableLayoutFixture()
    })

    // 先进入 attendance，再切到员工模块，让真实页面按当前 CSS 和表格 DOM 渲染完整员工列表。
    await page.goto('/?project=attendance')
    await page.locator('.selattendance-section-menu').getByRole('button', { name: /^员工/ }).click()

    const employeePanel = page
      .locator('article.selattendance-data-panel')
      .filter({ has: page.getByRole('columnheader', { name: '员工编号' }) })
    const tableShell = employeePanel.locator('.selattendance-table-shell')
    const rows = tableShell.locator('tbody tr')

    // 夹具应提供多条不同文案长度的数据行，否则无法真实覆盖多行文本与操作列组合场景。
    await expect(employeePanel).toBeVisible()
    await expect(rows).toHaveCount(4)
    await expect(rows.first()).toContainText('山田太郎')
    await expect(rows.last()).toContainText('绑定打卡 ID')

    // 保留员工表格局部截图，后续出现视觉回归时可以直接比对实际渲染而不是只看数字断言。
    await tableShell.screenshot({ path: testInfo.outputPath('employee-table-layout.png') })

    const rowCount = await rows.count()
    for (let rowIndex = 0; rowIndex < rowCount; rowIndex += 1) {
      const cells = rows.nth(rowIndex).locator('td')
      const cellCount = await cells.count()
      const boxes = []

      // 逐列提取单元格几何信息，确保同一行的上边和下边仍在一条水平线上。
      for (let cellIndex = 0; cellIndex < cellCount; cellIndex += 1) {
        const box = await cells.nth(cellIndex).boundingBox()
        expect(box).not.toBeNull()
        boxes.push(box)
      }

      const tops = boxes.map((box) => box.y)
      const bottoms = boxes.map((box) => box.y + box.height)
      const heights = boxes.map((box) => box.height)
      const topSpread = Math.max(...tops) - Math.min(...tops)
      const bottomSpread = Math.max(...bottoms) - Math.min(...bottoms)
      const heightSpread = Math.max(...heights) - Math.min(...heights)

      // 如果表格线错位，通常会先体现在单元格顶部、底部或高度在同一行内出现明显偏差。
      expect(topSpread, `row ${rowIndex + 1} top alignment drift`).toBeLessThanOrEqual(1)
      expect(bottomSpread, `row ${rowIndex + 1} bottom alignment drift`).toBeLessThanOrEqual(1)
      expect(heightSpread, `row ${rowIndex + 1} height alignment drift`).toBeLessThanOrEqual(1)
    }
  })
})
