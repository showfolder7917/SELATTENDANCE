package com.sp.selfsp.uniauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.sp.selfsp.attendance.support.AttendanceControllerIntegrationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 控制器测试集中验证第九阶段最小闭环，确保权限中心和 attendance 宿主桥接能在同一应用上下文里跑通。
@AutoConfigureMockMvc
class UniauthControllerTest extends AttendanceControllerIntegrationSupport {

    // MockMvc 负责在不启动真实 HTTP 端口的情况下覆盖登录、工作台和保存接口主链路。
    @Autowired
    private MockMvc mockMvc;

    // 登录、工作台和宿主桥接主链路必须先跑通，才能证明 JWT 与统一上下文已经接入成功。
    @Test
    void loginBootstrapAndHostContextShouldReturnUnifiedSnapshot() throws Exception {
        // 先通过 admin 示例账号获取最小闭环 access token，后续所有鉴权接口都复用这份 token。
        String accessToken = loginAndReadToken("admin", "admin123");
        // 用 Bearer token 读取当前用户快照，确认登录后至少能返回平台管理员身份信息。
        mockMvc.perform(get("/api/uniauth/auth/me").header("Authorization", "Bearer " + accessToken))
            // 当前 token 合法时 me 接口必须返回 200，说明 JWT 解析链路已生效。
            .andExpect(status().isOk())
            // 当前账号名必须保持为 admin，证明 token 中的主身份字段没有丢失。
            .andExpect(jsonPath("$.data.username").value("admin"))
            // 当前租户编码必须仍是默认租户，证明租户上下文也随 token 成功带入。
            .andExpect(jsonPath("$.data.tenantCode").value("DEFAULT"));

        // 再读取权限中心 bootstrap，确认租户、用户、角色和菜单聚合接口都能被平台管理员访问。
        mockMvc.perform(get("/api/uniauth/bootstrap").header("Authorization", "Bearer " + accessToken))
            // bootstrap 是第九阶段工作台入口，成功意味着权限中心主工作台可初始化。
            .andExpect(status().isOk())
            // 工作台模块数至少应为 3，证明 attendance、uniauth、seltheme 三个已托管工程已完成初始化。
            .andExpect(jsonPath("$.data.summary.moduleCount").value(greaterThanOrEqualTo(3)))
            // 工作台摘要中的租户数至少应为 1，证明种子租户已经完成初始化。
            .andExpect(jsonPath("$.data.summary.tenantCount").value(greaterThanOrEqualTo(1)))
            // 工作台用户数至少应为 2，证明平台管理员和租户管理员账号都已落入最小示例数据。
            .andExpect(jsonPath("$.data.summary.userCount").value(greaterThanOrEqualTo(2)))
            // 权限中心菜单数至少应为 6，证明动态菜单树种子数据已被加载。
            .andExpect(jsonPath("$.data.summary.menuCount").value(greaterThanOrEqualTo(6)));

        // 最后读取 attendance 宿主桥接接口，确认宿主也能消费同一份 JWT 上下文而不是只在 uniauth 生效。
        mockMvc.perform(get("/api/attendance/host/context").header("Authorization", "Bearer " + accessToken))
            // 宿主上下文成功返回说明第九阶段最小桥接目标已经实现。
            .andExpect(status().isOk())
            // 当前展示名应来自权限中心登录者，证明宿主读到的是同一上下文而不是硬编码用户。
            .andExpect(jsonPath("$.data.displayName").value("平台管理员"))
            // 菜单码里必须包含 attendance.home，证明宿主后续可以按菜单权限隐藏工程入口。
            .andExpect(jsonPath("$.data.menuCodes[?(@ == 'attendance.home')]").exists())
            // 数据范围里必须带 attendance:all:*，证明平台管理员具备宿主全量数据范围。
            .andExpect(jsonPath("$.data.dataScopes[0]").value("attendance:all:*"));
    }

    // 四类保存接口必须可用，才能证明权限中心不是只有读接口和登录接口的空壳。
    @Test
    void saveEndpointsShouldPersistTenantUserRoleAndMenu() throws Exception {
        // 先通过平台管理员登录获取写权限，后续四个保存接口都使用这份 token。
        String accessToken = loginAndReadToken("admin", "admin123");
        // 先创建一个新模块，验证权限中心现在已经补上模块管理能力。
        Map<String, Object> modulePayload = new LinkedHashMap<>();
        // 模块编码作为统一权限中心托管工程的稳定键，测试必须显式提供唯一值。
        modulePayload.put("moduleCode", "stage9-module");
        // 模块名称用于列表和返回结构展示。
        modulePayload.put("moduleName", "第九阶段模块");
        // 模块类型使用 business，覆盖最常见的业务工程接入口径。
        modulePayload.put("moduleType", "business");
        // 模块说明用于证明普通描述字段也能成功保存。
        modulePayload.put("moduleDesc", "验证模块主数据维护已补齐");
        // 入口工程明确写成 attendance，证明权限中心可以托管业务宿主工程。
        modulePayload.put("entryProject", "attendance");
        // 归属系统保持和入口工程一致，覆盖 ownerSystem 默认外的显式提交流程。
        modulePayload.put("ownerSystem", "attendance");
        // 路由键用于未来宿主入口治理，测试里显式覆盖该字段。
        modulePayload.put("routeKey", "/attendance?section=stage9-module");
        // 模块启用状态设为 true，验证布尔字段保存链路。
        modulePayload.put("enabledFlag", true);
        // 模块保存成功后应直接回显模块编码和入口工程，证明模块主数据已落库。
        mockMvc.perform(
                post("/api/uniauth/modules")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(JSON)
                    .content(writeJson(modulePayload))
            )
            // 平台管理员拥有模块创建权限，因此新增模块必须成功。
            .andExpect(status().isOk())
            // 模块编码回显一致，说明当前托管工程已创建成功。
            .andExpect(jsonPath("$.data.moduleCode").value("stage9-module"))
            // 入口工程回显一致，说明模块入口元数据也已落库。
            .andExpect(jsonPath("$.data.entryProject").value("attendance"));

        // 先准备唯一租户编码，避免测试重复运行时与历史写入数据冲突。
        String tenantCode = "TENANT_STAGE9_CASE";
        // 平台管理员再创建一个新租户，作为后续用户和角色的租户归属。
        Map<String, Object> tenantPayload = new LinkedHashMap<>();
        // 租户编码是跨系统稳定键，测试必须显式提供。
        tenantPayload.put("tenantCode", tenantCode);
        // 租户名称用于验证保存后是否能从响应里原样回读。
        tenantPayload.put("tenantName", "第九阶段租户");
        // 租户状态设为 enabled，保证后续用户账号可正常登录和归属。
        tenantPayload.put("tenantStatus", "enabled");
        // 联系人字段用于覆盖租户保存里除编码名称外的普通资料。
        tenantPayload.put("contactName", "阶段九负责人");
        // 联系邮箱字段用于证明租户保存可以同时处理联系资料。
        tenantPayload.put("contactEmail", "phase9@selsp.com");
        // 提交租户保存接口并读取 data 节点，为后续用户和角色创建准备真实租户 id。
        MvcResult tenantResult = mockMvc.perform(
                post("/api/uniauth/tenants")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(JSON)
                    .content(writeJson(tenantPayload))
            )
            // 平台管理员拥有租户写权限，因此当前请求必须成功。
            .andExpect(status().isOk())
            // 保存结果里应返回刚创建的租户编码，证明新增租户已真正落库。
            .andExpect(jsonPath("$.data.tenantCode").value(tenantCode))
            .andReturn();
        // 从返回体里提取真实租户 id，供用户和角色关系写入复用。
        long tenantId = readData(tenantResult).get("id").asLong();

        // 接着创建一个新角色，验证权限码、菜单码和数据范围三类关系能同时落库。
        Map<String, Object> rolePayload = new LinkedHashMap<>();
        // 角色绑定到刚创建的租户，证明租户级权限模型可写入。
        rolePayload.put("tenantId", tenantId);
        // 角色编码作为稳定键，便于后续用户角色绑定直接引用。
        rolePayload.put("roleCode", "ROLE_STAGE9_CASE");
        // 角色名称用于列表和返回结构展示。
        rolePayload.put("roleName", "第九阶段角色");
        // 角色描述用于证明普通说明字段也能被保存。
        rolePayload.put("roleDesc", "验证 role、menu、permission、scope 一次性保存");
        // 权限码列表绑定用户与角色维护权限，证明按钮权限关系表可重建。
        rolePayload.put("permissionCodes", java.util.List.of("uniauth.user.write", "uniauth.role.write"));
        // 菜单码列表显式加入 attendance.home，证明宿主工程入口授权可通过角色菜单关系下发。
        rolePayload.put("menuCodes", java.util.List.of("attendance.home", "uniauth.user"));
        // 数据范围类型选择 tenant，验证租户级数据权限表达可保存。
        rolePayload.put("dataScopeType", "tenant");
        // 数据范围值写成新租户 id，证明当前角色的数据权限可以绑定到具体租户。
        rolePayload.put("dataScopeValue", String.valueOf(tenantId));
        // 角色保存后直接断言返回结构，确认主资料已生成。
        MvcResult roleResult = mockMvc.perform(
                post("/api/uniauth/roles")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(JSON)
                    .content(writeJson(rolePayload))
            )
            // 平台管理员拥有角色写权限，因此角色保存必须成功。
            .andExpect(status().isOk())
            // 返回结构里应保留当前角色编码，证明新增角色成功落库。
            .andExpect(jsonPath("$.data.roleCode").value("ROLE_STAGE9_CASE"))
            .andReturn();
        // 读取角色主键，供后续用户角色绑定使用。
        long roleId = readData(roleResult).get("id").asLong();

        // 然后创建一个新用户，验证账号主表和用户角色关系表能同时完成写入。
        Map<String, Object> userPayload = new LinkedHashMap<>();
        // 新用户绑定刚创建的租户，验证租户级账号归属。
        userPayload.put("tenantId", tenantId);
        // 登录名使用唯一值，避免与种子用户或重复执行冲突。
        userPayload.put("loginName", "phase9-user");
        // 新增用户必须显式提供密码，否则无法建立可登录账号。
        userPayload.put("password", "phase9-pass");
        // 显示名用于后续工作台列表和审计日志展示。
        userPayload.put("displayName", "第九阶段新用户");
        // locale 设成 ja-JP，证明双语用户偏好可以随账号主数据保存。
        userPayload.put("locale", "ja-JP");
        // 当前用户直接绑定前面创建的租户角色，验证用户角色关系表写入。
        userPayload.put("roleIds", java.util.List.of(roleId));
        // 用户保存后断言主资料返回值，确认账号已落库。
        mockMvc.perform(
                post("/api/uniauth/users")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(JSON)
                    .content(writeJson(userPayload))
            )
            // 平台管理员拥有用户写权限，因此新账号保存必须成功。
            .andExpect(status().isOk())
            // 返回的登录名必须就是刚提交的唯一值，证明账号主数据已创建。
            .andExpect(jsonPath("$.data.loginName").value("phase9-user"))
            // 返回 locale 必须是 ja-JP，证明双语偏好字段也成功保存。
            .andExpect(jsonPath("$.data.locale").value("ja-JP"));

        // 最后创建一个菜单节点，证明菜单树维护接口和双语标题保存链路可用。
        Map<String, Object> menuPayload = new LinkedHashMap<>();
        // 模块编码显式归属 attendance，验证跨宿主导航节点也能由 uniauth 维护。
        menuPayload.put("moduleCode", "attendance");
        // 菜单编码使用唯一值，避免与种子菜单冲突。
        menuPayload.put("menuCode", "attendance.phase9.case");
        // 页面级菜单需要显式标明 page 类型，便于宿主后续路由消费。
        menuPayload.put("menuType", "page");
        // 路由路径用于未来把菜单真正映射到宿主页面入口。
        menuPayload.put("routePath", "/attendance?section=phase9");
        // 组件名保持 attendance 工作台根组件，证明菜单维护可写入宿主组件名。
        menuPayload.put("componentName", "AttendanceWorkbenchView");
        // 排序值保证新节点可以稳定插入到菜单树顺序中。
        menuPayload.put("sortOrder", 99);
        // 中文标题用于宿主中文菜单展示。
        menuPayload.put("titleZh", "第九阶段入口");
        // 日文标题用于宿主日文菜单展示。
        menuPayload.put("titleJa", "第九段階入口");
        // 菜单默认启用，保证宿主后续可以真正看见该节点。
        menuPayload.put("enabledFlag", true);
        // 菜单保存成功后需要直接回显唯一菜单编码和双语标题。
        mockMvc.perform(
                post("/api/uniauth/menus")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(JSON)
                    .content(writeJson(menuPayload))
            )
            // 平台管理员现在拥有菜单写权限，因此新增节点必须成功。
            .andExpect(status().isOk())
            // 菜单编码回显一致说明新菜单节点已落库。
            .andExpect(jsonPath("$.data.menuCode").value("attendance.phase9.case"))
            // 中文标题回显一致说明双语标题链路至少已保存一侧文本。
            .andExpect(jsonPath("$.data.titleZh").value("第九阶段入口"));
    }

    // 登录辅助方法统一负责获取 access token，避免每个测试重复解析登录响应。
    private String loginAndReadToken(String loginName, String password) throws Exception {
        // 登录请求体使用稳定字段名，保持和前端登录表单一致。
        Map<String, Object> loginPayload = new LinkedHashMap<>();
        // 登录名决定后端要查哪条用户主数据。
        loginPayload.put("loginName", loginName);
        // 密码决定是否允许当前账号建立新的 JWT 会话。
        loginPayload.put("password", password);
        // 发起登录请求并保留响应，供后续提取 access token。
        MvcResult loginResult = mockMvc.perform(
                post("/api/uniauth/auth/login")
                    .contentType(JSON)
                    .content(writeJson(loginPayload))
            )
            // 示例账号登录成功是整个第九阶段最小闭环的前提。
            .andExpect(status().isOk())
            // accessToken 必须存在，否则后续所有鉴权接口都无法继续。
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
            .andReturn();
        // 把统一响应壳解析成 data 节点，便于读取 token 文本。
        JsonNode loginData = readData(loginResult);
        // 取出 accessToken 作为后续 Bearer 头值。
        String accessToken = loginData.get("accessToken").asText();
        // token 必须是非空文本，说明登录接口确实完成签发。
        assertTrue(!accessToken.isBlank());
        // 当前登录名必须与响应里的 currentUser.username 对齐，证明 token 和快照来自同一账号。
        assertEquals(loginName, loginData.get("currentUser").get("username").asText());
        return accessToken;
    }
}
