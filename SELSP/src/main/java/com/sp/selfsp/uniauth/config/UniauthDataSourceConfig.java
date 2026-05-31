package com.sp.selfsp.uniauth.config;

import javax.sql.DataSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

// 第九阶段把 uniauth 独立成第二数据源，避免把权限中心表继续混在 attendance 主库里。
@Configuration
// 这里显式把 uniauth DAO 绑定到第二个 SqlSessionFactory，避免权限查询误打到 attendance 数据源。
@MapperScan(
    basePackages = "com.sp.selfsp.uniauth",
    annotationClass = Mapper.class,
    sqlSessionFactoryRef = "uniauthSqlSessionFactory"
)
public class UniauthDataSourceConfig {

    // 单独读取 uniauth 数据源配置，保证权限中心可以使用独立数据库地址和账号。
    @Bean
    // 配置前缀直接对应 application.properties 里的第二数据源配置块。
    @ConfigurationProperties("spring.datasource.uniauth")
    public DataSourceProperties uniauthDataSourceProperties() {
        // 交给 Spring 绑定属性对象，避免在代码里硬编码数据库连接信息。
        return new DataSourceProperties();
    }

    // 构建 uniauth 数据源，让权限中心的表、事务和初始化脚本全部跑在独立库上。
    @Bean
    public DataSource uniauthDataSource() {
        // 用 DataSourceProperties 统一创建数据源，保持和主业务库同一套 Spring Boot 约定。
        return uniauthDataSourceProperties().initializeDataSourceBuilder().build();
    }

    // 为 uniauth 单独创建 MyBatis 会话工厂，保证权限 DAO 只扫描自己的 XML 映射。
    @Bean
    public SqlSessionFactory uniauthSqlSessionFactory(@Qualifier("uniauthDataSource") DataSource dataSource) throws Exception {
        // 把第二数据源包进专属的 SqlSessionFactoryBean，避免 mapper 误用默认工厂。
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        // 会话工厂固定绑定 uniauth 数据源，确保租户、用户、角色查询都落到权限库。
        factoryBean.setDataSource(dataSource);
        // 只加载 uniauth 目录下的 mapper XML，避免权限工厂把 attendance 的 SQL 一起吃进去。
        factoryBean.setMapperLocations(
            new PathMatchingResourcePatternResolver().getResources("classpath*:/com/sp/selfsp/uniauth/**/*.xml")
        );
        // 开启下划线到驼峰的映射，减少权限中心 Map 结果到字段名的手工转换成本。
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        // 统一把 snake_case 列名映射成 camelCase 字段名，保持前后端字段语义一致。
        configuration.setMapUnderscoreToCamelCase(true);
        // 把映射配置交给 MyBatis 工厂，保证权限 DAO 和现有 attendance 口径一致。
        factoryBean.setConfiguration(configuration);
        // 返回真正的 SqlSessionFactory，供 uniauth DAO 执行 SQL。
        return factoryBean.getObject();
    }

    // 第二数据源也要有自己的事务管理器，后续租户启停和用户权限变更才能独立提交或回滚。
    @Bean
    public DataSourceTransactionManager uniauthTransactionManager(@Qualifier("uniauthDataSource") DataSource dataSource) {
        // 事务管理器只管理权限库，避免跨库事务边界混乱。
        return new DataSourceTransactionManager(dataSource);
    }

    // 启动时自动初始化权限中心 schema 和种子数据，保证本地和测试环境开箱即可登录。
    @Bean
    public InitializingBean uniauthDatabaseInitializer(@Qualifier("uniauthDataSource") DataSource dataSource) {
        // 用初始化回调在 Spring 完成数据源创建后立刻执行建表和种子脚本。
        return () -> {
            // 资源填充器统一执行 schema 与 data 脚本，避免再单独写一次手工 JDBC 初始化。
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            // 先建权限中心的全部核心表，保证后续种子插入不会因缺表失败。
            populator.addScript(new ClassPathResource("schema-uniauth.sql"));
            // 再插入平台管理员、默认租户、权限和菜单样本，便于第九阶段立即联调。
            populator.addScript(new ClassPathResource("data-uniauth.sql"));
            // 真正把脚本打到第二数据源里，而不是默认 attendance 数据源。
            populator.execute(dataSource);
        };
    }
}
