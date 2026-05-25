package com.sp.selfsp;

// SpringApplication 负责启动整个 Spring Boot 生命周期。
import org.springframework.boot.SpringApplication;
// SpringBootApplication 负责开启自动配置、组件扫描和配置装配。
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SELFSP Java 工程启动入口。
 */
// 声明 SpringBootApplication 注解，让当前代码接入既定框架能力。
@SpringBootApplication
// 定义 SelfspApplication，承接当前文件对应的业务职责。
public class SelfspApplication {

    /**
     * 启动用户 API 工程。
     *
     * @param args 启动参数
     */
    // 定义 main 处理入口，承接当前业务动作。
    public static void main(String[] args) {
        // 统一从这里拉起容器，保证控制器、服务、DAO 和 H2 配置一起生效。
        // 执行当前业务步骤，推进本行对应的 general 处理。
        SpringApplication.run(SelfspApplication.class, args);
    }
}
