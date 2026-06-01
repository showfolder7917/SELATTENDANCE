package com.sp.selfsp.layout1demo;

import com.sp.selfsp.attendance.support.AttendanceControllerIntegrationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 布局1号控制器测试负责证明 demo 工程的前后台聚合接口在当前宿主应用里可直接使用。
@AutoConfigureMockMvc
class Layout1DemoControllerTest extends AttendanceControllerIntegrationSupport {

    // mockMvc 用来在不启动真实端口的情况下验证布局1号 bootstrap 接口。
    @Autowired
    private MockMvc mockMvc;

    // bootstrapShouldReturnLayoutOneStructure 用来验证布局1号最小契约是否稳定可读。
    @Test
    void bootstrapShouldReturnLayoutOneStructure() throws Exception {
        // 直接读取布局1号 bootstrap，证明当前 demo 接口不依赖登录也能作为模板工程入口使用。
        mockMvc.perform(get("/api/layout1-demo/bootstrap"))
            // 接口成功返回 200 才说明前端 demo 可以稳定拿到首屏结构数据。
            .andExpect(status().isOk())
            // layoutCode 必须固定为布局1号，供前端和提示词统一引用。
            .andExpect(jsonPath("$.data.layoutCode").value("布局1号"))
            // 英雄栏标题必须稳定存在，供前端第一屏直接展示。
            .andExpect(jsonPath("$.data.heroOut.title").value("布局1号 Demo 工程"))
            // 左栏导航至少要有 7 项，证明这一布局不是只有单卡示例。
            .andExpect(jsonPath("$.data.navItems.length()").value(7))
            // 中栏摘要卡至少要有 4 项，证明顶部统计位已经具备稳定结构。
            .andExpect(jsonPath("$.data.summaryCards.length()").value(4))
            // 表格行至少要有 6 条，证明中栏表格区已能承接真实列表示例。
            .andExpect(jsonPath("$.data.tableRows.length()").value(6))
            // 右栏处理项至少要有 3 张卡，证明表单栏以外的附加动作也已经纳入契约。
            .andExpect(jsonPath("$.data.actionCards.length()").value(3))
            // 提示词路径必须直接返回，方便前端把“如何按布局1号重建”展示给使用者。
            .andExpect(jsonPath("$.data.promptDocPath").value("SELATTENDANCE/doc/项目架构/主要架构/示例/布局1号/布局1号_AI重建提示词_2026-06-01.md"));
    }
}
