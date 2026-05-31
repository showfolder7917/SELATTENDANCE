# SELSP Java 架构

## 1. 文档定位

本文档只描述当前项目后端工程 `SELSP` 的正式 Java 架构。

唯一主参照：

- `SELSP/src/main/java/com/sp/selfsp/attendance`

跟随参照：

- `SELSP/src/main/java/com/sp/selfsp/uniauth`

## 2. 真实目录基线

当前后端正式结构以真实目录为准：

```text
SELSP/
├─ src/main/java/com/sp/selfsp/
│  ├─ attendance/
│  ├─ uniauth/
│  └─ ...
├─ src/main/resources/
│  ├─ application.properties
│  ├─ schema.sql
│  ├─ data.sql
│  ├─ schema-uniauth.sql
│  └─ data-uniauth.sql
└─ src/test/
```

约束：

- `attendance` 是考勤业务域
- `uniauth` 是统一权限域
- 新增后端模块先参照 `attendance`

## 3. 模块拆分规则

`attendance` 已经证明的模块粒度就是正式粒度参考。

后续新增模块时，优先按以下级别组织：

- `bootstrap`
- `tenant`
- `workplace`
- `department`
- `employee`
- `schedule`
- `shifttemplate`
- `punch`
- `daily`
- `casefile`
- `monthly`
- `rule`
- `host`
- `punch/connector`

若某一模块演进成稳定子域，则继续细分。例如 `uniauth` 现在已经收敛为：

- `auth`
- `bootstrap`
- `tenant`
- `user`
- `role`
- `menu`
- `common`
- `security`
- `config`

禁止再回到大一统的：

- `UniauthController`
- `UniauthService`
- `UniauthDao`
- 模块根大而全 `domain`

## 4. 分层规则

模块内按正式分层组织：

- `controller`
- `dao`
- `dao/mapper`
- `service`
- `service/impl`
- `domain/in`
- `domain/out`

当子域已经稳定后：

- 领域对象放子域 `domain`
- 输入对象放子域 `domain/in`
- 输出对象放子域 `domain/out`

共享例外：

- 只有明确跨子域共享的对象，才允许进入 `common/domain/...`

明确禁止：

- 在模块根长期挂大一统 `domain/out/XXXOut`
- 一个根级 `domain` 同时服务多个稳定子域
- 为图省事把所有对象继续挂在 `com.sp.selfsp.xxx.domain`

## 5. Map 契约规则

正式业务主链路禁止长期使用：

- `Map<String, Object>` 作为 controller 入参
- `Map<String, Object>` 作为 service 主出参
- `Map<String, Object>` 作为 dao 正式返回

必须收敛成明确对象的场景：

- 登录返回
- 列表项
- 详情项
- 保存回查
- 工作台聚合
- 宿主桥接正式返回

允许保留 `Map` 的场景只限：

- JWT payload
- 临时技术元数据
- 调试性过渡结构

只要字段已经稳定进入正式业务页面，就必须改成对象。

## 6. DAO 与 XML 规则

MyBatis XML 统一参照成熟业务 XML，例如：

- `attendance/department/dao/mapper/AttendanceDepartmentDao.xml`

统一要求：

- 正式查询对象必须声明 `resultMap`
- 共用列集必须抽 `<sql id="...Columns">`
- `select` 优先 `<include refid="...Columns"/>`
- 正式列表 / 详情 / 保存回查禁止长期 `resultType="map"`
- XML 命名、缩进、字段顺序保持稳定

禁止：

- 一个 XML 同时混多套风格
- 依赖 SQL 别名 + `map` 作为正式出参
- 新模块自己发明一套 XML 风格

## 7. Service 边界规则

正式要求：

- Controller 只做协议层
- Service 只做业务编排与事务边界
- DAO 只做持久化
- 共用算法抽成专门 `calculator / support / mapper`

必须避免的坑：

- 上层 service 同时直接依赖下层 `service + dao`
- 审批流、重算流、月次聚合、规则聚合混在一个服务里
- 重复算法复制两套

## 8. 数据源与数据库规则

当前项目已经是双域结构：

- `attendance_db`
- `uniauth_db`

正式原则不是“一个工程一个数据库”，而是：

- 一个**独立业务域**一个数据库更合理
- 同仓库可以多工程
- 同工程也可以双数据源
- 关键是业务域边界清晰，不混库、不混职责

正式要求：

- `attendance` 业务数据继续在 `attendance_db`
- `uniauth` 权限数据继续在 `uniauth_db`
- 两边 schema、seed、datasource 配置必须显式存在
- 不允许把 `uniauth` 再回塞进 `attendance_db`

## 9. 启动与联调规则

正式要求：

- 后端必须支持本地单后端启动
- 全套必须支持一键启动
- 端口、数据源、失败日志必须明确

联调结论必须区分：

- 后端编译通过
- 接口联调通过
- 测试断言失败
- 仓库既有编译问题

不能把“编译通过到测试阶段”直接写成“整个后端闭环完成”。

## 10. 一次做成自检清单

新增正式模块或正式子域前，提交前至少自检：

- 是否先参照 `attendance` 现有模块拆分
- 是否已按 `controller/service/dao/domain/in/domain/out` 分层
- 若子域稳定，是否已继续细分
- 是否还存在根级大一统 `domain`
- 是否还存在主链路 `Map`
- XML 是否已 `resultMap + Columns`
- 注释是否符合当前 AGENTS 要求

## 11. 当前结论

后端架构执行原则：

- **先看 `attendance`**
- **再看 `uniauth` 跟随如何落**
- **最后才看模板**

若模板与真实项目冲突：

- **优先按真实项目规范修模板**
- **禁止让真实项目回退去兼容旧模板**
