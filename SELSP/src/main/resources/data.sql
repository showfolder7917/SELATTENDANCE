MERGE INTO tenant (id, tenant_code, tenant_name, status, contact_name, contact_phone, contact_email, timezone)
KEY (id)
VALUES (1, 'TENANT_DEMO', '东京第一教室', 'ACTIVE', '佐藤美咲', '03-1234-5678', 'admin@tokyo-school.jp', 'Asia/Tokyo');

MERGE INTO workplace (id, tenant_id, workplace_code, workplace_name, address, phone, status)
KEY (id)
VALUES
  (1, 1, 'TKY-HQ', '東京本部', '東京都千代田区1-1-1', '03-1000-1000', 'ACTIVE'),
  (2, 1, 'YKH-CLS', '横浜教室', '神奈川県横浜市2-2-2', '045-200-2000', 'ACTIVE');

MERGE INTO department (id, tenant_id, workplace_id, parent_id, department_code, department_name, sort_order, status)
KEY (id)
VALUES
  (1, 1, 1, NULL, 'ADMIN', '管理部', 1, 'ACTIVE'),
  (2, 1, 1, NULL, 'TEACH', '教務部', 2, 'ACTIVE'),
  (3, 1, 2, NULL, 'YKH-OPS', '横浜運営チーム', 3, 'ACTIVE');

MERGE INTO employee (
    id, tenant_id, workplace_id, department_id, employee_no, employee_name, employee_name_kana,
    gender, employment_type, hire_date, resign_date, email, phone, status
)
KEY (id)
VALUES
  (1, 1, 1, 2, 'E0001', '山田太郎', 'ヤマダタロウ', 'MALE', 'FULL_TIME', DATE '2026-04-01', NULL, 'taro.yamada@example.jp', '090-1111-1111', 'ACTIVE'),
  (2, 1, 1, 2, 'E0002', '佐藤花子', 'サトウハナコ', 'FEMALE', 'PART_TIME', DATE '2026-04-15', NULL, 'hanako.sato@example.jp', '090-2222-2222', 'ACTIVE'),
  (3, 1, 2, 3, 'E0003', '鈴木一郎', 'スズキイチロウ', 'MALE', 'CONTRACT', DATE '2026-05-01', NULL, 'ichiro.suzuki@example.jp', '090-3333-3333', 'ACTIVE'),
  (4, 1, 2, 3, 'E0004', '高橋美咲', 'タカハシミサキ', 'FEMALE', 'ARBEIT', DATE '2026-05-10', NULL, 'misaki.takahashi@example.jp', '090-4444-4444', 'INACTIVE');

MERGE INTO attendance_rule_config (
    id, tenant_id, rule_code, rule_name, standard_daily_minutes, standard_weekly_minutes,
    auto_break_enabled, auto_break_threshold_minutes, auto_break_deduct_minutes,
    night_work_start, night_work_end, rounding_unit_minutes, rounding_mode,
    monthly_overtime_alert_hours, yearly_overtime_alert_hours,
    paid_leave_reminder_enabled, active_flag, note, deleted_flag
)
KEY (id)
VALUES
  (1, 1, 'JP_STANDARD', '日本标准规则', 480, 2400, 1, 360, 60, '22:00', '05:00', 15, 'ROUND_NEAREST', 45, 360, 1, 1, '适用于常规全职员工', 0),
  (2, 1, 'JP_PART_TIME', '日本兼职规则', 300, 1500, 0, 0, 0, '22:00', '05:00', 15, 'ROUND_DOWN', 30, 240, 1, 1, '适用于兼职和短时雇员', 0);

MERGE INTO employee_work_rule (
    id, tenant_id, employee_id, rule_id, work_rule_type, standard_daily_minutes, standard_weekly_minutes,
    overtime_enabled, night_work_enabled, holiday_work_enabled, rounding_rule_id,
    effective_start_date, effective_end_date, rule_note
)
KEY (id)
VALUES
  (1, 1, 1, 1, 'JP_STANDARD', 480, 2400, 1, 1, 1, NULL, DATE '2026-04-01', NULL, '默认日本标准规则'),
  (2, 1, 2, 2, 'JP_PART_TIME', 300, 1500, 1, 0, 0, NULL, DATE '2026-04-15', NULL, '默认日本兼职规则'),
  (3, 1, 3, 1, 'JP_STANDARD', 480, 2400, 1, 1, 1, NULL, DATE '2026-05-01', NULL, '默认日本标准规则'),
  (4, 1, 4, 2, 'JP_PART_TIME', 240, 1200, 0, 0, 0, NULL, DATE '2026-05-10', NULL, '停用员工保留既有规则');

MERGE INTO employee_external_mapping (
    id, tenant_id, employee_id, source_system, external_employee_id, external_employee_no, status
)
KEY (id)
VALUES
  (1, 1, 1, 'KING_OF_TIME', 'KOT-90001', '90001', 'ACTIVE'),
  (2, 1, 2, 'KING_OF_TIME', 'KOT-90002', '90002', 'ACTIVE'),
  (3, 1, 3, 'TOUCH_ON_TIME', 'TOT-30003', '30003', 'ACTIVE');

MERGE INTO shift_template (
    id, tenant_id, template_code, template_name, shift_type,
    start_time, end_time, cross_day, scheduled_break_minutes, color, is_active
)
KEY (id)
VALUES
  (1, 1, 'EARLY', '早番', 'WORK', '09:00:00', '18:00:00', 0, 60, 'BLUE', 1),
  (2, 1, 'LATE', '遅番', 'WORK', '13:00:00', '22:00:00', 0, 60, 'ORANGE', 1),
  (3, 1, 'NIGHT', '夜勤', 'WORK', '22:00:00', '07:00:00', 1, 60, 'PURPLE', 1),
  (4, 1, 'HALF_AM', '半日早番', 'WORK', '09:00:00', '13:00:00', 0, 0, 'CYAN', 1),
  (5, 1, 'REST', '公休', 'REST', NULL, NULL, 0, 0, 'GRAY', 1),
  (6, 1, 'PAID_LEAVE', '有給', 'PAID_LEAVE', NULL, NULL, 0, 0, 'GREEN', 1);

MERGE INTO shift_schedule (
    id, tenant_id, employee_id, work_date, shift_template_id, scheduled_start_time, scheduled_end_time,
    scheduled_break_minutes, work_day_type, status, locked, remark, created_by, updated_by
)
KEY (id)
VALUES
  (3001, 1, 1, DATE '2026-05-01', 1, TIMESTAMP '2026-05-01 09:00:00', TIMESTAMP '2026-05-01 18:00:00', 60, 'WORKDAY', 'ACTIVE', 0, '日次計算用サンプル', 1, 1),
  (3002, 1, 1, DATE '2026-05-02', 1, TIMESTAMP '2026-05-02 09:00:00', TIMESTAMP '2026-05-02 18:00:00', 60, 'WORKDAY', 'ACTIVE', 0, '日次計算用サンプル', 1, 1),
  (3003, 1, 1, DATE '2026-05-03', 1, TIMESTAMP '2026-05-03 09:00:00', TIMESTAMP '2026-05-03 18:00:00', 60, 'WORKDAY', 'ACTIVE', 0, '日次計算用サンプル', 1, 1),
  (3004, 1, 1, DATE '2026-05-04', 1, TIMESTAMP '2026-05-04 09:00:00', TIMESTAMP '2026-05-04 18:00:00', 60, 'WORKDAY', 'ACTIVE', 0, '日次計算用サンプル', 1, 1),
  (3005, 1, 1, DATE '2026-05-05', 1, TIMESTAMP '2026-05-05 09:00:00', TIMESTAMP '2026-05-05 18:00:00', 60, 'WORKDAY', 'ACTIVE', 0, '日次計算用サンプル', 1, 1),
  (3006, 1, 1, DATE '2026-05-06', 1, TIMESTAMP '2026-05-06 09:00:00', TIMESTAMP '2026-05-06 18:00:00', 60, 'WORKDAY', 'ACTIVE', 0, '日次計算用サンプル', 1, 1),
  (3007, 1, 1, DATE '2026-05-07', 1, TIMESTAMP '2026-05-07 09:00:00', TIMESTAMP '2026-05-07 18:00:00', 60, 'WORKDAY', 'ACTIVE', 0, '日次計算用サンプル', 1, 1),
  (3008, 1, 1, DATE '2026-05-08', 1, TIMESTAMP '2026-05-08 09:00:00', TIMESTAMP '2026-05-08 18:00:00', 60, 'WORKDAY', 'ACTIVE', 0, '日次計算用サンプル', 1, 1),
  (3009, 1, 1, DATE '2026-05-09', 1, TIMESTAMP '2026-05-09 09:00:00', TIMESTAMP '2026-05-09 18:00:00', 60, 'WORKDAY', 'ACTIVE', 0, '日次計算用サンプル', 1, 1),
  (3010, 1, 1, DATE '2026-05-10', 1, TIMESTAMP '2026-05-10 09:00:00', TIMESTAMP '2026-05-10 18:00:00', 60, 'WORKDAY', 'ACTIVE', 0, '日次計算用サンプル', 1, 1),
  (3011, 1, 1, DATE '2026-05-11', 1, TIMESTAMP '2026-05-11 09:00:00', TIMESTAMP '2026-05-11 18:00:00', 60, 'WORKDAY', 'ACTIVE', 0, '日次計算用サンプル', 1, 1),
  (3012, 1, 1, DATE '2026-05-12', 1, TIMESTAMP '2026-05-12 09:00:00', TIMESTAMP '2026-05-12 18:00:00', 60, 'WORKDAY', 'ACTIVE', 0, '日次計算用サンプル', 1, 1),
  (3013, 1, 1, DATE '2026-05-13', 1, TIMESTAMP '2026-05-13 09:00:00', TIMESTAMP '2026-05-13 18:00:00', 60, 'WORKDAY', 'ACTIVE', 0, '日次計算用サンプル', 1, 1),
  (3014, 1, 1, DATE '2026-05-14', 1, TIMESTAMP '2026-05-14 09:00:00', TIMESTAMP '2026-05-14 18:00:00', 60, 'WORKDAY', 'ACTIVE', 0, '日次計算用サンプル', 1, 1),
  (3015, 1, 1, DATE '2026-05-15', 1, TIMESTAMP '2026-05-15 09:00:00', TIMESTAMP '2026-05-15 18:00:00', 60, 'WORKDAY', 'ACTIVE', 0, '日次計算用サンプル', 1, 1),
  (3016, 1, 1, DATE '2026-05-16', 1, TIMESTAMP '2026-05-16 09:00:00', TIMESTAMP '2026-05-16 18:00:00', 60, 'WORKDAY', 'ACTIVE', 0, '日次計算用サンプル', 1, 1),
  (3017, 1, 1, DATE '2026-05-17', 1, TIMESTAMP '2026-05-17 09:00:00', TIMESTAMP '2026-05-17 18:00:00', 60, 'WORKDAY', 'ACTIVE', 0, '日次計算用サンプル', 1, 1),
  (3019, 1, 1, DATE '2026-05-19', 1, TIMESTAMP '2026-05-19 09:00:00', TIMESTAMP '2026-05-19 18:00:00', 60, 'WORKDAY', 'ACTIVE', 0, '缺勤样本', 1, 1),
  (3020, 1, 1, DATE '2026-05-20', 1, TIMESTAMP '2026-05-20 09:00:00', TIMESTAMP '2026-05-20 18:00:00', 60, 'WORKDAY', 'ACTIVE', 0, '缺勤样本', 1, 1),
  (3021, 1, 1, DATE '2026-05-21', 1, TIMESTAMP '2026-05-21 09:00:00', TIMESTAMP '2026-05-21 18:00:00', 60, 'WORKDAY', 'ACTIVE', 0, '缺勤样本', 1, 1);

MERGE INTO punch_source_config (
    id, tenant_id, source_system, config_name, api_base_url, api_key, api_secret, webhook_secret, enabled, config_json
)
KEY (id)
VALUES
  (1, 1, 'CSV_IMPORT', 'CSV 一括取込', NULL, NULL, NULL, NULL, 1, '{"encoding":"UTF-8","timezone":"Asia/Tokyo"}'),
  (2, 1, 'WEBHOOK', 'CUSTOM Webhook', 'https://example-gateway.local', 'demo-key', 'demo-secret', 'demo-hook-secret', 1, '{"mode":"push","timezone":"Asia/Tokyo"}');

MERGE INTO attendance_punch_sync_log (
    id, tenant_id, connector_id, source_system, trigger_type, external_request_id, sync_status, success_count, failed_count,
    error_message, request_snapshot, result_snapshot, retry_flag, retry_count, created_at, updated_at
)
KEY (id)
VALUES
  (1, 1, 2, 'WEBHOOK', 'WEBHOOK', 'hook-ok-001', 'SUCCESS', 1, 0, NULL,
   '{"tenantCode":"DEFAULT","sourceSystem":"WEBHOOK","sourceEventId":"hook-ok-001","externalEmployeeId":"KOT-0001","punchTime":"2026-05-28 09:00:00","punchType":"CLOCK_IN","deviceId":"gate-01","deviceName":"東京本部入口","rawData":{"event":"seed-success"}}',
   '{"processStatus":"PROCESSED","messageCode":"punch.webhook.received"}', 0, 0, TIMESTAMP '2026-05-28 09:01:00', TIMESTAMP '2026-05-28 09:01:00'),
  (2, 1, 2, 'WEBHOOK', 'WEBHOOK', 'hook-failed-001', 'FAILED', 0, 1, '未找到对应员工映射',
   '{"tenantCode":"DEFAULT","sourceSystem":"WEBHOOK","sourceEventId":"hook-failed-001","externalEmployeeId":"WEBHOOK-NEW-9001","punchTime":"2026-05-28 18:00:00","punchType":"CLOCK_OUT","deviceId":"gate-02","deviceName":"東京本部出口","rawData":{"event":"seed-failed"}}',
   '{"processStatus":"UNMATCHED","messageCode":"punch.webhook.received"}', 0, 0, TIMESTAMP '2026-05-28 18:01:00', TIMESTAMP '2026-05-28 18:01:00');

MERGE INTO punch_import_batch (
    id, tenant_id, source_system, import_type, file_name, total_count, success_count, duplicate_count, unmatched_count, error_count, ignored_count, status, started_at, finished_at, created_by, created_at
)
KEY (id)
VALUES
  (1, 1, 'CSV_IMPORT', 'CSV_TEXT', 'punch_runtime_week_1.csv', 27, 24, 0, 1, 1, 1, 'PARTIAL', TIMESTAMP '2026-05-20 09:00:00', TIMESTAMP '2026-05-20 09:02:00', 1, TIMESTAMP '2026-05-20 09:00:00'),
  (2, 1, 'CSV_IMPORT', 'CSV_TEXT', 'punch_runtime_week_2.csv', 27, 23, 0, 2, 1, 1, 'PARTIAL', TIMESTAMP '2026-05-21 09:00:00', TIMESTAMP '2026-05-21 09:03:00', 1, TIMESTAMP '2026-05-21 09:00:00');

MERGE INTO punch_raw_log (
    id, tenant_id, employee_id, external_employee_id, source_system, source_event_id, punch_time, punch_type, device_id, device_name, raw_payload, import_batch_id, process_status, error_message, ignored_reason
)
KEY (id)
VALUES
  (2001, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0001', TIMESTAMP '2026-05-01 09:07:00', 'CLOCK_OUT', 'gate-02', '東京本部端末2', '{"row":1,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2002, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0002', TIMESTAMP '2026-05-02 10:14:00', 'CLOCK_IN', 'gate-03', '東京本部端末1', '{"row":2,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2003, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0003', TIMESTAMP '2026-05-03 08:21:00', 'CLOCK_OUT', 'gate-04', '東京本部端末2', '{"row":3,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2004, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0004', TIMESTAMP '2026-05-04 09:28:00', 'CLOCK_IN', 'gate-01', '東京本部端末1', '{"row":4,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2005, 1, 1, 'KOT-90001', 'WEBHOOK', 'seed-evt-0005', TIMESTAMP '2026-05-05 10:35:00', 'CLOCK_OUT', 'gate-02', '東京本部端末2', '{"row":5,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2006, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0006', TIMESTAMP '2026-05-06 08:42:00', 'CLOCK_IN', 'gate-03', '東京本部端末1', '{"row":6,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2007, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0007', TIMESTAMP '2026-05-07 09:49:00', 'CLOCK_OUT', 'gate-04', '東京本部端末2', '{"row":7,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2008, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0008', TIMESTAMP '2026-05-08 10:56:00', 'CLOCK_IN', 'gate-01', '東京本部端末1', '{"row":8,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2009, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0009', TIMESTAMP '2026-05-09 08:03:00', 'CLOCK_OUT', 'gate-02', '東京本部端末2', '{"row":9,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2010, 1, 1, 'KOT-90001', 'WEBHOOK', 'seed-evt-0010', TIMESTAMP '2026-05-10 09:10:00', 'CLOCK_IN', 'gate-03', '東京本部端末1', '{"row":10,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2011, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0011', TIMESTAMP '2026-05-11 10:17:00', 'CLOCK_OUT', 'gate-04', '東京本部端末2', '{"row":11,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2012, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0012', TIMESTAMP '2026-05-12 08:24:00', 'CLOCK_IN', 'gate-01', '東京本部端末1', '{"row":12,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2013, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0013', TIMESTAMP '2026-05-13 09:31:00', 'CLOCK_OUT', 'gate-02', '東京本部端末2', '{"row":13,"memo":"runtime-seed"}', 1, 'IGNORED', NULL, '管理者が確認済みのため除外'),
  (2014, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0014', TIMESTAMP '2026-05-14 10:38:00', 'CLOCK_IN', 'gate-03', '東京本部端末1', '{"row":14,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2015, 1, 1, 'KOT-90001', 'WEBHOOK', 'seed-evt-0015', TIMESTAMP '2026-05-15 08:45:00', 'CLOCK_OUT', 'gate-04', '東京本部端末2', '{"row":15,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2016, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0016', TIMESTAMP '2026-05-16 09:52:00', 'CLOCK_IN', 'gate-01', '東京本部端末1', '{"row":16,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2017, 1, NULL, 'EXT-UNKNOWN-017', 'CSV_IMPORT', 'seed-evt-0017', TIMESTAMP '2026-05-17 10:59:00', 'CLOCK_OUT', 'gate-02', '東京本部端末2', '{"row":17,"memo":"runtime-seed"}', 1, 'UNMATCHED', '未找到对应员工映射', NULL),
  (2018, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0018', TIMESTAMP '2026-05-18 08:06:00', 'CLOCK_IN', 'gate-03', '東京本部端末1', '{"row":18,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2019, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0019', TIMESTAMP '2026-05-01 09:13:00', 'CLOCK_BAD', 'gate-04', '東京本部端末2', '{"row":19,"memo":"runtime-seed"}', 1, 'ERROR', 'punchType 不合法', NULL),
  (2020, 1, 1, 'KOT-90001', 'WEBHOOK', 'seed-evt-0020', TIMESTAMP '2026-05-02 10:20:00', 'CLOCK_IN', 'gate-01', '東京本部端末1', '{"row":20,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2021, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0021', TIMESTAMP '2026-05-03 08:27:00', 'CLOCK_OUT', 'gate-02', '東京本部端末2', '{"row":21,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2022, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0022', TIMESTAMP '2026-05-04 09:34:00', 'CLOCK_IN', 'gate-03', '東京本部端末1', '{"row":22,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2023, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0023', TIMESTAMP '2026-05-05 10:41:00', 'CLOCK_OUT', 'gate-04', '東京本部端末2', '{"row":23,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2024, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0024', TIMESTAMP '2026-05-06 08:48:00', 'CLOCK_IN', 'gate-01', '東京本部端末1', '{"row":24,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2025, 1, 1, 'KOT-90001', 'WEBHOOK', 'seed-evt-0025', TIMESTAMP '2026-05-07 09:55:00', 'CLOCK_OUT', 'gate-02', '東京本部端末2', '{"row":25,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2026, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0026', TIMESTAMP '2026-05-08 10:02:00', 'CLOCK_IN', 'gate-03', '東京本部端末1', '{"row":26,"memo":"runtime-seed"}', 1, 'IGNORED', NULL, '管理者が確認済みのため除外'),
  (2027, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0027', TIMESTAMP '2026-05-09 08:09:00', 'CLOCK_OUT', 'gate-04', '東京本部端末2', '{"row":27,"memo":"runtime-seed"}', 1, 'PROCESSED', NULL, NULL),
  (2028, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0028', TIMESTAMP '2026-05-10 09:16:00', 'CLOCK_IN', 'gate-01', '東京本部端末1', '{"row":28,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2029, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0029', TIMESTAMP '2026-05-11 10:23:00', 'CLOCK_OUT', 'gate-02', '東京本部端末2', '{"row":29,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2030, 1, 1, 'KOT-90001', 'WEBHOOK', 'seed-evt-0030', TIMESTAMP '2026-05-12 08:30:00', 'CLOCK_IN', 'gate-03', '東京本部端末1', '{"row":30,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2031, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0031', TIMESTAMP '2026-05-13 09:37:00', 'CLOCK_OUT', 'gate-04', '東京本部端末2', '{"row":31,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2032, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0032', TIMESTAMP '2026-05-14 10:44:00', 'CLOCK_IN', 'gate-01', '東京本部端末1', '{"row":32,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2033, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0033', TIMESTAMP '2026-05-15 08:51:00', 'CLOCK_OUT', 'gate-02', '東京本部端末2', '{"row":33,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2034, 1, NULL, 'EXT-UNKNOWN-034', 'CSV_IMPORT', 'seed-evt-0034', TIMESTAMP '2026-05-16 09:58:00', 'CLOCK_IN', 'gate-03', '東京本部端末1', '{"row":34,"memo":"runtime-seed"}', 2, 'UNMATCHED', '未找到对应员工映射', NULL),
  (2035, 1, 1, 'KOT-90001', 'WEBHOOK', 'seed-evt-0035', TIMESTAMP '2026-05-17 10:05:00', 'CLOCK_OUT', 'gate-04', '東京本部端末2', '{"row":35,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2036, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0036', TIMESTAMP '2026-05-18 08:12:00', 'CLOCK_IN', 'gate-01', '東京本部端末1', '{"row":36,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2037, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0037', TIMESTAMP '2026-05-01 09:19:00', 'CLOCK_OUT', 'gate-02', '東京本部端末2', '{"row":37,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2038, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0038', TIMESTAMP '2026-05-02 10:26:00', 'CLOCK_BAD', 'gate-03', '東京本部端末1', '{"row":38,"memo":"runtime-seed"}', 2, 'ERROR', 'punchType 不合法', NULL),
  (2039, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0039', TIMESTAMP '2026-05-03 08:33:00', 'CLOCK_OUT', 'gate-04', '東京本部端末2', '{"row":39,"memo":"runtime-seed"}', 2, 'IGNORED', NULL, '管理者が確認済みのため除外'),
  (2040, 1, 1, 'KOT-90001', 'WEBHOOK', 'seed-evt-0040', TIMESTAMP '2026-05-04 09:40:00', 'CLOCK_IN', 'gate-01', '東京本部端末1', '{"row":40,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2041, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0041', TIMESTAMP '2026-05-05 10:47:00', 'CLOCK_OUT', 'gate-02', '東京本部端末2', '{"row":41,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2042, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0042', TIMESTAMP '2026-05-06 08:54:00', 'CLOCK_IN', 'gate-03', '東京本部端末1', '{"row":42,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2043, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0043', TIMESTAMP '2026-05-07 09:01:00', 'CLOCK_OUT', 'gate-04', '東京本部端末2', '{"row":43,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2044, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0044', TIMESTAMP '2026-05-08 10:08:00', 'CLOCK_IN', 'gate-01', '東京本部端末1', '{"row":44,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2045, 1, 1, 'KOT-90001', 'WEBHOOK', 'seed-evt-0045', TIMESTAMP '2026-05-09 08:15:00', 'CLOCK_OUT', 'gate-02', '東京本部端末2', '{"row":45,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2046, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0046', TIMESTAMP '2026-05-10 09:22:00', 'CLOCK_IN', 'gate-03', '東京本部端末1', '{"row":46,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2047, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0047', TIMESTAMP '2026-05-11 10:29:00', 'CLOCK_OUT', 'gate-04', '東京本部端末2', '{"row":47,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2048, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0048', TIMESTAMP '2026-05-12 08:36:00', 'CLOCK_IN', 'gate-01', '東京本部端末1', '{"row":48,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2049, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0049', TIMESTAMP '2026-05-13 09:43:00', 'CLOCK_OUT', 'gate-02', '東京本部端末2', '{"row":49,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2050, 1, 1, 'KOT-90001', 'WEBHOOK', 'seed-evt-0050', TIMESTAMP '2026-05-14 10:50:00', 'CLOCK_IN', 'gate-03', '東京本部端末1', '{"row":50,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2051, 1, NULL, 'EXT-UNKNOWN-051', 'CSV_IMPORT', 'seed-evt-0051', TIMESTAMP '2026-05-15 08:57:00', 'CLOCK_OUT', 'gate-04', '東京本部端末2', '{"row":51,"memo":"runtime-seed"}', 2, 'UNMATCHED', '未找到对应员工映射', NULL),
  (2052, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0052', TIMESTAMP '2026-05-16 09:04:00', 'CLOCK_IN', 'gate-01', '東京本部端末1', '{"row":52,"memo":"runtime-seed"}', 2, 'IGNORED', NULL, '管理者が確認済みのため除外'),
  (2053, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0053', TIMESTAMP '2026-05-17 10:11:00', 'CLOCK_OUT', 'gate-02', '東京本部端末2', '{"row":53,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2054, 1, 1, 'KOT-90001', 'CSV_IMPORT', 'seed-evt-0054', TIMESTAMP '2026-05-18 08:18:00', 'CLOCK_IN', 'gate-03', '東京本部端末1', '{"row":54,"memo":"runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2055, 1, 1, 'KOT-90001', 'WEBHOOK', 'seed-evt-0055', TIMESTAMP '2026-05-02 18:05:00', 'CLOCK_OUT', 'gate-05', '東京補助端末', '{"row":55,"memo":"daily-runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2056, 1, 1, 'KOT-90001', 'WEBHOOK', 'seed-evt-0056', TIMESTAMP '2026-05-04 17:30:00', 'CLOCK_OUT', 'gate-05', '東京補助端末', '{"row":56,"memo":"daily-runtime-seed"}', 2, 'PROCESSED', NULL, NULL),
  (2057, 1, 1, 'KOT-90001', 'WEBHOOK', 'seed-evt-0057', TIMESTAMP '2026-05-06 18:02:00', 'CLOCK_OUT', 'gate-05', '東京補助端末', '{"row":57,"memo":"daily-runtime-seed"}', 2, 'PROCESSED', NULL, NULL);
