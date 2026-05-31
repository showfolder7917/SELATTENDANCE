// 权限中心工程把第九阶段独立身份域挂进宿主工程发现机制，避免宿主写死模块入口。
import UniauthWorkbenchView from './views/UniauthWorkbenchView.vue'
// 权限中心自己的视觉样式跟随工程注册加载，保证删除工程时不会污染其他模块。
import './style.css'

// 向宿主暴露权限中心工程元数据，供项目切换器和动态工程发现统一消费。
export default {
  // 工程 id 作为宿主 URL 和切换器识别权限中心的稳定键。
  id: 'uniauth',
  // 标签直接告诉用户当前工程是权限中心，而不是业务工作台。
  label: 'UniAuth',
  // 描述用于后续宿主扩展时解释当前工程职责。
  description: 'Unified tenant, user, role and menu control center',
  // 顺序放在 attendance 前面，让未登录用户先看到身份入口。
  order: 5,
  // 权限中心入口必须对未登录状态公开，否则用户无法进入登录页。
  publicEntry: true,
  // 根组件承载登录、工作台和宿主上下文验证三类核心能力。
  component: UniauthWorkbenchView
}
