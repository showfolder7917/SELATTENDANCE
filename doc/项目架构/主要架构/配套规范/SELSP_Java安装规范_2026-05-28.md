# SELSP Java 安装规范

## 0. 文档目标

本规范用于统一 `SELSP` 这个 Java 后端项目的模块安装方式、目录边界、配置接入方式、测试接入方式和安装后验证要求。

目标是保证后续新增模块时：
- 能按统一结构接入
- 不破坏现有宿主工程
- 不形成跨模块硬耦合
- 安装后可验证
- 移除时影响范围可控

## 1. 适用范围

本规范适用于：
- `C:\opt\workspace\SEL\SELATTENDANCE\SELSP`

当前项目基础结构包括：
- `src/main/java`
- `src/main/resources`
- `src/test/java`
- `src/test/resources`
- `build.gradle`
- `settings.gradle`
- `gradle.properties`

## 2. 安装单元定义

### 2.1 安装单元
`SELSP` 中一个可维护的后端业务模块，安装单元定义为：

- 一个明确的业务目录
- 一组完整的 controller / service / dao / mapper / bean / test
- 一套明确的资源配置与 SQL 映射

### 2.2 当前推荐业务粒度
推荐按业务域安装，而不是按技术层打散安装。

例如：
- `attendance`
- `uniauth`
- 后续新增的其他业务域

不推荐：
- 单独按 `controller` 安装一个模块
- 单独按 `mapper` 安装一个模块

## 3. 标准目录结构

### 3.1 主代码结构

```text
src/main/java/com/sp/selfsp/
  <domain>/
    controller/
    service/
      impl/
    dao/
    bootstrap/
    tenant/
    workplace/
    department/
    employee/
    shifttemplate/
    schedule/

  common/
  config/
```

### 3.2 资源结构

```text
src/main/resources/
  application.properties
  mapper/
    *.xml
```

### 3.3 测试结构

```text
src/test/java/com/sp/selfsp/
  <domain>/
    support/
    <module>/
      *ControllerTest.java
      *ServiceBoundaryTest.java
      *MapperTest.java
      *RegressionTest.java

src/test/resources/
  application-test.properties
  reset-<domain>-test-data.sql
```

## 4. 安装接入规则

### 4.1 controller 安装规则
- controller 必须放在业务域目录下的 `controller/`
- controller 只负责：
  - 接收请求
  - 参数绑定
  - 返回响应
- controller 不允许直接访问 DAO
- controller 必须通过 service 或 query service 访问业务数据

### 4.2 service 安装规则
- service 接口放在 `service/`
- 实现类放在 `service/impl/`
- service 负责：
  - 业务规则
  - 状态流转
  - 参数校验
  - 事务边界
- service 不允许承担页面拼装型跨域聚合
- 若必须做聚合读取，应引入专门的 query service / read port

### 4.3 dao 安装规则
- DAO 放在业务域目录下的 `dao/`
- DAO 只负责数据库访问
- DAO 不负责业务语义拼装
- 跨域读模型必须显式声明，不允许随意在一个 DAO 里侵入多个业务域语义

### 4.4 mapper XML 安装规则
- MyBatis XML 统一放在 `src/main/resources/mapper/`
- 文件命名应与 DAO/Mapper 语义一致
- 一个 mapper 文件内允许包含同一业务域的一组 SQL
- 不允许把多个无关业务域的 SQL 混塞进同一个 mapper XML

### 4.5 bean 安装规则
- 输入输出 bean、DB bean、接口 bean 应按现有项目习惯放在对应公共包或业务包
- 新模块必须保持输入、输出、DB 映射 bean 语义分离
- 不允许一个 bean 同时承担：
  - 接口输入
  - DB 查询结果
  - 页面视图组装

## 5. 配置安装规范

### 5.1 主配置
- 主配置统一收敛在：
  - `src/main/resources/application.properties`
- 新模块若需要新增配置项，必须：
  - 使用清晰前缀
  - 避免覆盖现有无关配置
  - 在文档中说明用途

### 5.2 测试配置
- 测试必须使用独立配置：
  - `src/test/resources/application-test.properties`
- 不允许测试隐式复用主配置作为唯一数据源配置

### 5.3 profile 规范
- Java 测试必须显式使用：
  - `@ActiveProfiles("test")`
- 不允许依赖默认 profile 猜测测试环境

## 6. 构建与依赖规范

### 6.1 Gradle 规范
- 项目构建入口以：
  - `build.gradle`
  - `gradlew.bat`
  为准
- 新模块接入依赖时，应优先复用现有依赖体系
- 不允许为单一小功能无节制引入大型新框架

### 6.2 Java 版本规范
- 当前项目已存在 `-PjavaVersion=21` 的测试执行方式
- 若模块对 Java 版本有额外要求，必须先确认不会破坏现有构建链路

### 6.3 第三方依赖接入规则
- 新增依赖前应判断：
  - 是否已有同类能力
  - 是否仅为局部功能引入过重依赖
  - 是否增加测试和部署复杂度

## 7. 解耦安装原则

### 7.1 业务边界原则
- 新模块必须先有明确业务边界，再安装代码
- 不允许“先写功能，再拼目录”

### 7.2 依赖方向原则
- `controller -> service -> dao`
- 禁止：
  - `controller -> dao`
  - `serviceA -> daoB` 随意跨域直连
  - 页面聚合类直接抓多个 DAO

### 7.3 聚合查询原则
- 若确实需要聚合查询，允许保留聚合应用层
- 但聚合层应依赖：
  - query service
  - read port
- 不应直接依赖多个底层 DAO

### 7.4 可移除性原则
- 模块安装后，应尽量把影响范围控制在本业务域内
- 移除模块时，不应导致其他无关模块无法启动
- 若模块提供公共能力，必须先沉淀到明确的公共层，再被其他模块引用

## 8. 测试安装规范

### 8.1 必备测试层
新安装模块至少应具备：
- `ControllerTest`
- `ServiceBoundaryTest`
- `MapperTest`

若模块存在历史 bug 风险或复杂链路，应再补：
- `RegressionTest`

### 8.2 测试基座
- 测试应优先复用现有 `support` 基座
- 不允许每个测试类重复粘贴大量相同启动注解和 SQL reset 配置

### 8.3 测试数据
- 测试数据必须可重置
- 不允许依赖上一个测试留下的状态
- 数据准备方式应统一收敛到：
  - SQL reset
  - 可复用 fixture

## 9. 安装步骤建议

新增一个 Java 模块时，建议按以下顺序安装：

1. 建立业务目录
2. 建立 controller / service / dao / mapper 基础骨架
3. 接入主配置所需的最小配置项
4. 补 mapper XML
5. 补基础输入输出 bean
6. 先打通主路径
7. 再补测试：
   - controller
   - service
   - mapper
   - regression
8. 运行测试和构建验证

## 10. 安装后验证规范

### 10.1 最低验证要求
只要安装了新模块，至少应完成：
- 模块相关测试通过
- 主构建通过
- 关键接口可访问

### 10.2 推荐命令

```powershell
.\gradlew.bat test --tests "com.sp.selfsp.<domain>.*" -PjavaVersion=21 --no-daemon
.\gradlew.bat jacocoTestReport -x test -PjavaVersion=21 --no-daemon
```

### 10.3 验证范围
- 主路径
- 边界路径
- 邻接回归路径

## 11. 禁止事项

- 禁止 controller 直连 DAO
- 禁止 service 随意跨业务域直连别域 DAO
- 禁止把多个无关业务域 SQL 混入同一个 mapper XML
- 禁止测试只靠 mock 而没有真实数据库流程验证
- 禁止新增模块后不补测试
- 禁止把业务私有逻辑伪装成公共能力提前沉到 common

## 12. 当前项目落地建议

针对 `SELSP` 当前现状，建议后续新增模块时保持：
- 主代码继续按业务域分包
- mapper 继续统一收敛到 `resources/mapper`
- 测试继续采用：
  - `controller`
  - `service`
  - `mapper`
  - `regression`
  - `support`
 这五层结构
- 测试继续使用：
  - `application-test.properties`
  - `@ActiveProfiles("test")`
  - H2 内存测试库

## 13. 文档维护要求

- 安装规范变更后应同步更新本文件
- 若新增业务域采用了新的统一约束，也应回写本文件
- 本规范优先描述“当前真实落地方式”，不写脱离项目现状的理想化规范
