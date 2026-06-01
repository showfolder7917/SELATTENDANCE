package com.sp.selfsp.layout1demo.controller;

import com.sp.selfsp.common.util.CommonResponse;
import com.sp.selfsp.layout1demo.domain.out.Layout1DemoBootstrapOut;
import com.sp.selfsp.layout1demo.service.Layout1DemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 布局1号控制器负责对外暴露 demo 工程的聚合 bootstrap 接口。
@RestController
@RequestMapping("/api/layout1-demo")
public class Layout1DemoController {

    // layout1DemoService 负责真正组装布局1号的页面数据。
    private final Layout1DemoService layout1DemoService;

    // 构造器负责把布局1号聚合服务注入进当前控制器。
    public Layout1DemoController(Layout1DemoService layout1DemoService) {
        // 保存服务依赖，供 bootstrap 接口直接调用。
        this.layout1DemoService = layout1DemoService;
    }

    // bootstrap 接口用于给前端 demo 一次性返回英雄栏、导航、表格和表单所需数据。
    @GetMapping("/bootstrap")
    public CommonResponse<Layout1DemoBootstrapOut> bootstrap() {
        // 成功路径统一返回 CommonResponse.success，保持和 attendance 现有工程同口径。
        return CommonResponse.success(layout1DemoService.getBootstrap());
    }
}
