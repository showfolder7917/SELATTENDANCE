// 引入空租户工厂和首页壳工厂，验证 tenant 面板依赖的默认字段不会在测试侧入口切换后丢失。
import { createBootstrapShell, createEmptyTenant } from '@tests-attendance'

describe('attendance unit tenant shell', () => {
  // 校验空租户结构，保证租户面板在接口返回前就有完整字段可回显和回写。
  it('creates an empty tenant shell with the current timezone default', () => {
    expect(createEmptyTenant()).toEqual({
      tenantCode: '',
      tenantName: '',
      contactName: '',
      contactPhone: '',
      contactEmail: '',
      timezone: 'Asia/Tokyo'
    })
  })

  // 校验首页壳中的租户摘要默认值，保证 bootstrap 壳和独立租户接口读取前的首屏提示一致。
  it('uses the empty tenant shell as the bootstrap tenant summary default', () => {
    const shell = createBootstrapShell()

    expect(shell.tenantSummary).toEqual(createEmptyTenant())
  })
})
