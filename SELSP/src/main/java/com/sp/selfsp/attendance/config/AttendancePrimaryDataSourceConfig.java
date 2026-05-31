package com.sp.selfsp.attendance.config;

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
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

// attendance 主数据源配置显式恢复原业务库，避免接入 uniauth 第二数据源后把主库自动装配顶掉。
@Configuration
// 主业务 DAO 统一绑定到主库会话工厂，保证既有考勤 SQL 仍然落到 attendance 库而不是 uniauth 库。
@MapperScan(
    basePackages = "com.sp.selfsp.attendance",
    annotationClass = Mapper.class,
    sqlSessionFactoryRef = "sqlSessionFactory"
)
public class AttendancePrimaryDataSourceConfig {

    // 主库配置继续读取 spring.datasource 前缀，保持既有 application.properties 不需要迁移字段名。
    @Bean
    // 当前主库属性对象必须是主候选项，避免业务层注入时误拿到 uniauth 的配置对象。
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        // 直接交给 Spring 绑定主库连接配置，保持和引入第二数据源前一致。
        return new DataSourceProperties();
    }

    // 显式创建 attendance 主数据源，让业务主库和权限中心副库同时存在。
    @Bean
    // 主数据源必须设为首选项，保证所有未特别声明的数据访问都继续走 attendance 主库。
    @Primary
    public DataSource dataSource() {
        // 使用主库属性对象构建数据源，避免手工拼接 JDBC 连接参数。
        return dataSourceProperties().initializeDataSourceBuilder().build();
    }

    // 主业务 SqlSessionFactory 负责加载所有 attendance 子域的 mapper XML。
    @Bean(name = "sqlSessionFactory")
    // 既有业务 DAO 默认依赖这个工厂，因此它必须保持主候选项地位。
    @Primary
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) throws Exception {
        // 为主库创建独立 MyBatis 工厂，确保考勤 SQL 与权限中心 SQL 各自隔离。
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        // 主库工厂固定绑定 attendance 数据源，避免旧业务 SQL 误打第二数据库。
        factoryBean.setDataSource(dataSource);
        // 只加载 attendance 目录下的 mapper XML，让主库不去解析 uniauth SQL。
        factoryBean.setMapperLocations(
            new PathMatchingResourcePatternResolver().getResources("classpath*:/com/sp/selfsp/attendance/**/*.xml")
        );
        // 主库同样保持下划线转驼峰口径，避免新旧子域字段映射风格分裂。
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        // 打开下划线映射，让考勤各阶段现有 Map 结果保持稳定字段名。
        configuration.setMapUnderscoreToCamelCase(true);
        // 把映射配置写回 MyBatis 工厂，保证主库 mapper 与历史行为一致。
        factoryBean.setConfiguration(configuration);
        // 返回真正的主业务会话工厂，供所有 attendance DAO 复用。
        return factoryBean.getObject();
    }

    // 主事务管理器继续沿用默认名字 transactionManager，避免既有 @Transactional 失去绑定目标。
    @Bean(name = "transactionManager")
    // 事务管理器设成主候选项后，旧业务服务层无需任何改动即可继续提交主库事务。
    @Primary
    public DataSourceTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) {
        // 主事务管理器只包住 attendance 主库，和 uniauth 的副库事务边界保持独立。
        return new DataSourceTransactionManager(dataSource);
    }

    // 主库初始化器显式执行 schema.sql 和 data.sql，确保接入第二数据源后既有示例数据仍能自动落库。
    @Bean
    public InitializingBean attendanceDatabaseInitializer(@Qualifier("dataSource") DataSource dataSource) {
        // 启动回调在主数据源就绪后立即执行，保证测试和本地一键启动仍有完整业务数据。
        return () -> {
            // 主库脚本填充器统一承接建表和种子数据初始化。
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            // 先建 attendance 全量业务表，保证后续种子插入不会因缺表失败。
            populator.addScript(new ClassPathResource("schema.sql"));
            // 再灌入既有阶段种子数据，保证所有考勤控制器测试仍有联调基础。
            populator.addScript(new ClassPathResource("data.sql"));
            // 真正把脚本执行到主业务库，而不是权限中心副库。
            populator.execute(dataSource);
        };
    }
}
