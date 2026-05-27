# SELSP Java 测试规范

## 0. 当前实现现状与规范目标

### 0.1 当前实现现状
- `SELSP` 已建立 `attendance` 模块的 `controller` 测试与 `service` 边界测试。
- `SELSP` 已建立独立测试配置：`src/test/resources/application-test.properties`。
- `attendance` 测试已显式使用 `@ActiveProfiles("test")`，测试数据源与主配置解耦。
- `attendance` 已补入首批 `mapper` 测试、`regression` 测试与统一 `support` 基座。

### 0.2 规范目标
- 统一 `SELSP` 后端 Java 测试的分层结构、命名方式、配置方式与注释风格。
- 保证后续新增测试时，不再混乱堆叠到单一测试类中。
- 保证测试可分层执行、可定位问题、可沉淀历史回归点。

## 1. 测试分层原则

### 1.1 controller 层
- 目标：保护 HTTP 接口契约。
- 关注内容：路由、请求参数绑定、状态码、响应包装结构、错误响应。
- 不负责穷尽所有业务分支。

### 1.2 service 层
- 目标：保护业务规则。
- 关注内容：主路径、异常路径、边界条件、状态切换、跨对象业务约束。
- 这是业务改动后最优先补强的一层。

### 1.3 mapper 层
- 目标：保护 SQL 与 MyBatis 映射。
- 关注内容：查询条件、排序、统计、跨表字段映射、导出查询、删除范围。
- 复杂 SQL 不允许只依赖 service 或 controller 间接覆盖。

### 1.4 regression 层
- 目标：保护历史 bug 与高风险回归点。
- 关注内容：已经修过的问题、复杂业务链路、容易反复破坏的旧问题。
- `regression` 测试不按技术层分类，而按“历史问题保护”分类。

### 1.5 support 层
- 目标：统一测试基座。
- 关注内容：Spring 启动方式、`test profile`、SQL reset、JSON 工具、通用常量。
- 所有模块测试都应优先复用 `support`，避免重复粘贴装配注解。

## 2. 标准目录结构

```text
src/test/java/com/sp/selfsp/attendance/
  support/
    AttendanceIntegrationSupport.java
    AttendanceControllerIntegrationSupport.java
    AttendanceServiceIntegrationSupport.java
    AttendanceMapperIntegrationSupport.java

  bootstrap/
    AttendanceBootstrapControllerTest.java
    AttendanceBootstrapServiceBoundaryTest.java
    AttendanceBootstrapMapperTest.java

  tenant/
    AttendanceTenantControllerTest.java
    AttendanceTenantServiceBoundaryTest.java
    AttendanceTenantMapperTest.java

  workplace/
    AttendanceWorkplaceControllerTest.java
    AttendanceWorkplaceServiceBoundaryTest.java
    AttendanceWorkplaceMapperTest.java

  department/
    AttendanceDepartmentControllerTest.java
    AttendanceDepartmentServiceBoundaryTest.java
    AttendanceDepartmentMapperTest.java

  employee/
    AttendanceEmployeeControllerTest.java
    AttendanceEmployeeServiceBoundaryTest.java
    AttendanceEmployeeMapperTest.java
    AttendanceEmployeeRegressionTest.java

  schedule/
    AttendanceScheduleControllerTest.java
    AttendanceScheduleServiceBoundaryTest.java
    AttendanceScheduleMapperTest.java
    AttendanceScheduleRegressionTest.java

  shifttemplate/
    AttendanceShiftTemplateControllerTest.java
    AttendanceShiftTemplateServiceBoundaryTest.java
    AttendanceShiftTemplateMapperTest.java
```

## 3. 测试类命名规范

- `*ControllerTest`
  - 用于接口契约测试。
- `*ServiceBoundaryTest`
  - 用于业务规则、异常路径和边界分支测试。
- `*MapperTest`
  - 用于 SQL 与 MyBatis 结果映射测试。
- `*RegressionTest`
  - 用于历史 bug 与高风险回归点测试。
- `*IntegrationSupport`
  - 用于统一测试基座，不承载具体业务断言。

## 4. 测试配置规范

### 4.1 测试 profile
- 测试必须显式使用 `@ActiveProfiles("test")`。
- 不允许让测试隐式依赖主配置启动。

### 4.2 测试配置文件
- 测试专用配置放在：
  - `src/test/resources/application-test.properties`
- 当前标准做法：
  - 使用 H2 内存库
  - 测试数据源与主配置使用不同库名

### 4.3 当前标准测试库
- 当前测试数据源：
  - `jdbc:h2:mem:selattendance-test`
- 主配置数据源：
  - `jdbc:h2:mem:selattendance`
- 两者必须保持分离，不允许混用同一库名。

### 4.4 数据重置
- 当前统一重置脚本：
  - `src/test/resources/reset-attendance-test-data.sql`
- `attendance` 模块测试应通过基座或测试类统一执行 SQL reset。
- 高依赖数据的测试不允许直接依赖上一个测试留下的状态。

## 5. support 基座规范

### 5.1 AttendanceIntegrationSupport
- 作为所有集成测试的顶层基座。
- 负责统一：
  - `@SpringBootTest`
  - `@ActiveProfiles("test")`
  - `@Sql(reset-attendance-test-data.sql)`
  - 租户常量等公共上下文

### 5.2 AttendanceControllerIntegrationSupport
- 继承 `AttendanceIntegrationSupport`
- 负责统一：
  - JSON 序列化
  - `CommonResponse.data` 读取
  - `MockMvc` 相关公共辅助

### 5.3 AttendanceServiceIntegrationSupport
- 继承 `AttendanceIntegrationSupport`
- 负责承接 service 层真实数据库流程测试。

### 5.4 AttendanceMapperIntegrationSupport
- 继承 `AttendanceIntegrationSupport`
- 负责承接 mapper 层 SQL 与映射测试。

## 6. 注释规范

### 6.1 Java 测试文件注释规则
- `import` 不写注释。
- 不写字段注释、行内解释注释、类说明注释。
- 只保留方法注释。
- 注释使用中文。
- 注释使用标准 Java 样式注释。

### 6.2 测试方法注释格式
- 测试方法统一格式：

```java
/**
 * 测试目的：验证shouldCreateEmployeeAndDefaultRule场景。
 */
```

### 6.3 辅助方法注释格式
- 辅助方法统一格式：

```java
/**
 * 辅助目的：为employeeSaveIn提供测试支撑。
 */
```

## 7. controller 测试规范

- 必须走真实 `MockMvc`。
- 必须断言：
  - HTTP 状态码
  - 响应包装结构
  - 关键业务字段
- 接口有错误响应时，必须补错误路径测试。
- controller 测试应保护接口契约，不应替代 service 层规则测试。

## 8. service 测试规范

- 必须优先走真实 Spring + DAO + H2 流程。
- 必须覆盖：
  - 主路径
  - 参数非法
  - 重复数据
  - 空结果
  - 状态切换
  - 边界日期或范围逻辑
- 高复杂模块优先补 `schedule` 与 `employee`。

## 9. mapper 测试规范

- 必须直接调用 DAO，不通过 service 包装。
- 必须覆盖：
  - 条件过滤
  - 排序
  - 联表字段
  - 统计结果
  - 删除范围
  - 导出查询
- 对复杂 SQL，mapper 层必须有直接测试，不允许只靠上层间接命中。

## 10. regression 测试规范

### 10.1 放置规则
- 回归测试优先放在各模块目录下。
- 例如：
  - `employee/AttendanceEmployeeRegressionTest.java`
  - `schedule/AttendanceScheduleRegressionTest.java`

### 10.2 使用规则
- 每修复一个明确 bug，应新增至少一个最小回归测试。
- 回归测试命名要直接反映问题场景。
- 回归测试可覆盖 controller、service、mapper 任一层历史问题，但归类仍放 `RegressionTest`。

## 11. 编写优先级

新增业务或修 bug 时，测试补充优先级如下：

1. 先补 `service` 或 `controller` 主验证层。
2. 若涉及复杂 SQL，再补 `mapper`。
3. 若属于已修历史问题，再补 `regression`。
4. 若出现重复装配，再回收进 `support`。

## 12. 执行与验证规范

- 只要本轮改了测试或后端业务代码，必须执行测试验证。
- 至少要跑与改动层级直接相关的测试。
- 完成后应补一轮相邻回归。

推荐命令示例：

```powershell
.\gradlew.bat test --tests "com.sp.selfsp.attendance.*MapperTest" -PjavaVersion=21 --no-daemon
.\gradlew.bat test --tests "com.sp.selfsp.attendance.*RegressionTest" -PjavaVersion=21 --no-daemon
.\gradlew.bat test --tests "com.sp.selfsp.attendance.*" -PjavaVersion=21 --no-daemon
```

## 13. 后续补强建议

- 为 `bootstrap / tenant / workplace / department / shifttemplate` 继续补 `RegressionTest`
- 对复杂导出和统计 SQL 继续细化 `MapperTest`
- 逐步把更多重复注解和重复数据装配收敛到 `support`
- 在后续条件允许时，可补覆盖率门槛规则
