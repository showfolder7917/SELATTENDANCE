// 根组件负责承接布局1号 demo 的整页工作台视图。
import Layout1DemoView from './views/Layout1DemoView.vue'
// 页面样式随模块注册一起加载，保证宿主切到 demo 时立即拿到布局1号外观。
import './style.css'

// 对宿主暴露一个可直接访问的公共 demo 工程，供用户和 AI 以后按“布局1号”复建。
export default {
  // id 作为宿主 URL 和工程发现逻辑的稳定键。
  id: 'layout1demo',
  // label 作为 project 切换器里给用户看到的展示名。
  label: 'Layout1Demo',
  // description 说明当前工程是公共布局模板示例，而不是正式业务模块。
  description: 'Layout No.1 reusable workbench demo',
  // order 把 demo 放在 attendance 后面，避免抢占正式业务工程顺序。
  order: 20,
  // publicEntry 设为 true，保证没有业务权限时也能直接打开 demo 查看模板。
  publicEntry: true,
  // component 指向当前 demo 的页面根组件。
  component: Layout1DemoView
}
