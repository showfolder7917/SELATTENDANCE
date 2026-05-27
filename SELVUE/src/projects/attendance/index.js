// 业务模块自己声明页面根组件，并在注册阶段带上专属样式，避免根宿主反向依赖考勤实现细节。
import AttendanceWorkbenchView from './views/AttendanceWorkbenchView.vue'
// 考勤工作台的页面样式跟随模块注册加载，后续删除模块时宿主不会再引用这份样式文件。
import './style.css'

// 向宿主暴露考勤模块的稳定标识、展示信息和根组件，供项目发现入口自动装配。
export default {
  id: 'attendance',
  label: 'Attendance',
  description: 'Attendance administration workbench',
  order: 10,
  component: AttendanceWorkbenchView
}
