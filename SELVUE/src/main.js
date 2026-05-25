// 创建 Vue 应用实例，作为 SELATTENDANCE 第一阶段工作台的正式入口。
import { createApp } from 'vue'
// 全局样式统一承接液态玻璃主题、布局和组件视觉。
import './style.css'
// 根组件只负责挂载考勤第一阶段工作台。
import App from './App.vue'

// 把应用挂到根节点，完成页面启动。
// 执行当前业务步骤，推进本行对应的 general 处理。
createApp(App).mount('#app')
