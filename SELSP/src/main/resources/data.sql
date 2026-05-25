MERGE INTO tenant (id, tenant_code, tenant_name, status, contact_name, contact_phone, contact_email, timezone)
KEY (id)
VALUES (1, 'TENANT_DEMO', '东京第一教室', 'ACTIVE', '佐藤美咲', '03-1234-5678', 'admin@tokyo-school.jp', 'Asia/Tokyo');

MERGE INTO workplace (id, tenant_id, workplace_code, workplace_name, address, phone, status)
KEY (id)
VALUES
  (1, 1, 'TKY-HQ', '东京本部', '東京都千代田区1-1-1', '03-1000-1000', 'ACTIVE'),
  (2, 1, 'YKH-CLS', '横滨教室', '神奈川県横浜市2-2-2', '045-200-2000', 'ACTIVE');

MERGE INTO department (id, tenant_id, workplace_id, parent_id, department_code, department_name, sort_order, status)
KEY (id)
VALUES
  (1, 1, 1, NULL, 'ADMIN', '管理部', 1, 'ACTIVE'),
  (2, 1, 1, NULL, 'TEACH', '教学部', 2, 'ACTIVE'),
  (3, 1, 2, NULL, 'YKH-OPS', '横滨运营组', 3, 'ACTIVE');

MERGE INTO employee (
    id, tenant_id, workplace_id, department_id, employee_no, employee_name, employee_name_kana,
    gender, employment_type, hire_date, resign_date, email, phone, status
)
KEY (id)
VALUES
  (1, 1, 1, 2, 'E0001', '山田太郎', 'ヤマダタロウ', 'MALE', 'FULL_TIME', DATE '2026-04-01', NULL, 'taro.yamada@example.jp', '090-1111-1111', 'ACTIVE'),
  (2, 1, 1, 2, 'E0002', '佐藤花子', 'サトウハナコ', 'FEMALE', 'PART_TIME', DATE '2026-04-15', NULL, 'hanako.sato@example.jp', '090-2222-2222', 'ACTIVE'),
  (3, 1, 2, 3, 'E0003', '铃木一郎', 'スズキイチロウ', 'MALE', 'CONTRACT', DATE '2026-05-01', NULL, 'ichiro.suzuki@example.jp', '090-3333-3333', 'ACTIVE'),
  (4, 1, 2, 3, 'E0004', '高桥美咲', 'タカハシミサキ', 'FEMALE', 'ARBEIT', DATE '2026-05-10', NULL, 'misaki.takahashi@example.jp', '090-4444-4444', 'INACTIVE');

MERGE INTO employee_work_rule (
    id, tenant_id, employee_id, work_rule_type, standard_daily_minutes, standard_weekly_minutes,
    overtime_enabled, night_work_enabled, holiday_work_enabled, rounding_rule_id,
    effective_start_date, effective_end_date
)
KEY (id)
VALUES
  (1, 1, 1, 'STANDARD', 480, 2400, 1, 1, 1, NULL, DATE '2026-04-01', NULL),
  (2, 1, 2, 'PART_TIME', 300, 1500, 1, 0, 0, NULL, DATE '2026-04-15', NULL),
  (3, 1, 3, 'STANDARD', 480, 2400, 1, 1, 1, NULL, DATE '2026-05-01', NULL),
  (4, 1, 4, 'PART_TIME', 240, 1200, 0, 0, 0, NULL, DATE '2026-05-10', NULL);

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
  (1, 1, 'EARLY', '早班', 'WORK', '09:00:00', '18:00:00', 0, 60, 'BLUE', 1),
  (2, 1, 'LATE', '晚班', 'WORK', '13:00:00', '22:00:00', 0, 60, 'ORANGE', 1),
  (3, 1, 'NIGHT', '夜班', 'WORK', '22:00:00', '07:00:00', 1, 60, 'PURPLE', 1),
  (4, 1, 'HALF_AM', '半日早班', 'WORK', '09:00:00', '13:00:00', 0, 0, 'CYAN', 1),
  (5, 1, 'REST', '休息', 'REST', NULL, NULL, 0, 0, 'GRAY', 1),
  (6, 1, 'PAID_LEAVE', '有休', 'PAID_LEAVE', NULL, NULL, 0, 0, 'GREEN', 1);
