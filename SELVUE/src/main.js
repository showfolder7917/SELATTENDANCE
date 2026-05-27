// 宿主入口只负责启动项目壳，避免根层继续硬编码 attendance 模块的样式和页面装配。
import { createApp } from 'vue'
// 根壳统一接管子工程选择与降级空态，具体业务模块在自己的注册文件里声明资源。
import App from './App.vue'

// 挂载宿主应用，让 src/projects 下被发现到的业务模块按注册信息接入页面。
createApp(App).mount('#app')
