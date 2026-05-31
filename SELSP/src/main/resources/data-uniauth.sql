MERGE INTO ua_tenant (id, tenant_code, tenant_name, tenant_status, contact_name, contact_email, contact_phone)
KEY(id)
VALUES
  (1, 'DEFAULT', '默认租户', 'enabled', '平台管理员', 'admin@selsp.com', '03-0000-0000');

MERGE INTO ua_user (id, tenant_id, login_name, password_hash, display_name, display_name_kana, locale, email, phone, user_status, locked_flag)
KEY(id)
VALUES
  (1, 1, 'admin', 'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', '平台管理员', 'プラットフォーム管理者', 'zh-CN', 'admin@selsp.com', '03-0000-0000', 'ACTIVE', 0),
  (2, 1, 'tenant-admin', 'tPCCMM3dTBvFKoduEttTT4tA7tsIunilUB0c3464yzM=', '租户管理员', 'テナント管理者', 'ja-JP', 'tenant@selsp.com', '03-1111-1111', 'ACTIVE', 0);

MERGE INTO ua_role (id, tenant_id, role_code, role_name, role_desc, builtin_flag, role_status)
KEY(id)
VALUES
  (1, NULL, 'PLATFORM_ADMIN', '平台管理员', '拥有平台租户与权限中心全部能力', 1, 'ACTIVE'),
  (2, 1, 'TENANT_ADMIN', '租户管理员', '拥有当前租户的管理能力', 1, 'ACTIVE'),
  (3, 1, 'EMPLOYEE', '普通员工', '只读自己的业务数据', 1, 'ACTIVE');

MERGE INTO ua_module (id, module_code, module_name, module_type, module_desc, entry_project, owner_system, route_key, enabled_flag)
KEY(id)
VALUES
  (1, 'attendance', '考勤系统', 'business', '承接考勤、排班、打卡、月次和规则等业务域。', 'attendance', 'attendance', '/attendance', 1),
  (2, 'uniauth', '统一权限中心', 'platform', '承接租户、用户、角色、模块和菜单等权限主数据。', 'uniauth', 'uniauth', '/uniauth', 1),
  (3, 'seltheme', '主题工程', 'theme', '承接共享主题、预览和主题包元数据。', 'seltheme', 'seltheme', '/themes', 1);

MERGE INTO ua_menu (id, module_code, menu_code, parent_id, menu_type, route_path, component_name, icon_name, sort_order, title_zh, title_ja, enabled_flag)
KEY(id)
VALUES
  (1, 'uniauth', 'uniauth.dashboard', NULL, 'group', '/uniauth', 'UniauthWorkbenchView', 'shield', 10, '权限中心', '権限センター', 1),
  (2, 'uniauth', 'uniauth.tenant', 1, 'page', '/uniauth?tab=tenant', 'UniauthWorkbenchView', 'building', 20, '租户管理', 'テナント管理', 1),
  (3, 'uniauth', 'uniauth.user', 1, 'page', '/uniauth?tab=user', 'UniauthWorkbenchView', 'users', 30, '用户管理', 'ユーザー管理', 1),
  (4, 'uniauth', 'uniauth.role', 1, 'page', '/uniauth?tab=role', 'UniauthWorkbenchView', 'badge', 40, '角色管理', 'ロール管理', 1),
  (5, 'uniauth', 'uniauth.menu', 1, 'page', '/uniauth?tab=menu', 'UniauthWorkbenchView', 'menu', 50, '菜单管理', 'メニュー管理', 1),
  (6, 'attendance', 'attendance.home', NULL, 'group', '/attendance', 'AttendanceWorkbenchView', 'clock', 60, '考勤系统', '勤怠システム', 1);

MERGE INTO ua_permission (id, module_code, permission_code, permission_name, permission_type, resource_key, action_key, scope_type, enabled_flag)
KEY(id)
VALUES
  (1, 'uniauth', 'uniauth.bootstrap.read', '读取权限中心工作台', 'api', 'bootstrap', 'read', 'all', 1),
  (8, 'uniauth', 'uniauth.module.view', '查看模块列表', 'api', 'module', 'view', 'all', 1),
  (9, 'uniauth', 'uniauth.module.create', '创建模块', 'button', 'module', 'create', 'all', 1),
  (10, 'uniauth', 'uniauth.module.update', '更新模块', 'button', 'module', 'update', 'all', 1),
  (2, 'uniauth', 'uniauth.tenant.write', '维护租户', 'button', 'tenant', 'write', 'all', 1),
  (3, 'uniauth', 'uniauth.user.write', '维护用户', 'button', 'user', 'write', 'tenant', 1),
  (4, 'uniauth', 'uniauth.role.write', '维护角色', 'button', 'role', 'write', 'tenant', 1),
  (5, 'uniauth', 'uniauth.menu.read', '查看菜单树', 'menu', 'menu', 'read', 'all', 1),
  (6, 'attendance', 'attendance.host.context.read', '读取宿主上下文', 'api', 'host-context', 'read', 'tenant', 1),
  (7, 'uniauth', 'uniauth.menu.write', '维护菜单树', 'button', 'menu', 'write', 'all', 1);

MERGE INTO ua_user_role (id, user_id, role_id)
KEY(id)
VALUES
  (1, 1, 1),
  (2, 2, 2);

MERGE INTO ua_role_permission (id, role_id, permission_id)
KEY(id)
VALUES
  (1, 1, 1),
  (13, 1, 8),
  (14, 1, 9),
  (15, 1, 10),
  (2, 1, 2),
  (3, 1, 3),
  (4, 1, 4),
  (5, 1, 5),
  (6, 1, 6),
  (7, 1, 7),
  (8, 2, 1),
  (9, 2, 3),
  (10, 2, 4),
  (11, 2, 5),
  (12, 2, 6);

MERGE INTO ua_role_menu (id, role_id, menu_id)
KEY(id)
VALUES
  (1, 1, 1),
  (2, 1, 2),
  (3, 1, 3),
  (4, 1, 4),
  (5, 1, 5),
  (6, 1, 6),
  (7, 2, 1),
  (8, 2, 3),
  (9, 2, 4),
  (10, 2, 6);

MERGE INTO ua_role_data_scope (id, role_id, module_code, scope_type, scope_value)
KEY(id)
VALUES
  (1, 1, 'attendance', 'all', '*'),
  (2, 2, 'attendance', 'tenant', '1');
