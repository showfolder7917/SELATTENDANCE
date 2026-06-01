package com.sp.selfsp.layout1demo.service;

import com.sp.selfsp.layout1demo.domain.out.Layout1DemoBootstrapOut;

// 布局1号服务接口只负责向前端提供 demo 工程首屏需要的聚合出参。
public interface Layout1DemoService {

    // getBootstrap 用来读取布局1号前端的英雄栏、导航、表格和表单示例数据。
    Layout1DemoBootstrapOut getBootstrap();
}
