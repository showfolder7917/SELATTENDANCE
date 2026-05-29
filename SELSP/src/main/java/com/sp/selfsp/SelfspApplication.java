package com.sp.selfsp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SELFSP Java 工程启动入口。
 */
@SpringBootApplication
public class SelfspApplication {

    /**
     * 启动用户 API 工程。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        // 从这里启动整个考勤后台，确保接口层、业务层、MyBatis 映射和本地测试库配置一起装配完成。
        SpringApplication.run(SelfspApplication.class, args);
    }
}
