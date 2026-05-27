// 注册 memory 工程模块，供宿主通过自动发现机制在安装目录后直接激活和切换。
import MemoryWorkbenchView from './views/MemoryWorkbenchView.vue'
// 引入 memory 工程自己的样式入口，保证该工程安装后即带上独立外观。
import './style.css'

// 导出 memory 工程元数据，宿主只依赖这份注册信息而不依赖具体工程目录名以外的硬编码。
export default {
  id: 'memory',
  label: 'Memory',
  description: 'Memory workspace starter module',
  order: 20,
  component: MemoryWorkbenchView
}
