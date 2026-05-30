import { assignRule, createRule, fetchRuleWorkbench, updateRule } from '../../services'

export const createRuleSection = ({
  state,
  setSectionLoading,
  setSectionError,
  pushToast,
  t,
  refreshShell,
  requestConfirm
}) => {
  // 第七阶段规则工作台独立读取聚合结果，供规则、适用和预警三块共用同一批数据。
  const loadRuleWorkbench = async () => {
    setSectionLoading('rule', true)
    setSectionError('rule', '')
    try {
      // 把当前筛选条件作为正式查询口径发给后端，避免前端自己拼预警逻辑。
      const payload = await fetchRuleWorkbench({ ...state.ruleFilters })
      state.ruleWorkbench = {
        rules: payload.rules || [],
        assignments: payload.assignments || [],
        alerts: payload.alerts || [],
        summary: payload.summary || {
          highRiskCount: 0,
          reminderCount: 0,
          boundEmployeeCount: 0
        }
      }
      // 成功后标记规则区块已完成首次加载，避免重复首刷。
      state.bootstrapShell.sectionStates.rule = true
    } catch (error) {
      const message = error?.message || t('ruleLoadFailed')
      setSectionError('rule', message)
      pushToast(message)
    } finally {
      setSectionLoading('rule', false)
    }
  }

  // 规则表单重置回第七阶段推荐默认值，方便管理员快速新建标准规则。
  const resetRuleForm = () => {
    state.ruleForm = {
      id: null,
      ruleCode: '',
      ruleName: '',
      standardDailyMinutes: 480,
      standardWeeklyMinutes: 2400,
      autoBreakEnabled: true,
      autoBreakThresholdMinutes: 360,
      autoBreakDeductMinutes: 60,
      nightWorkStart: '22:00',
      nightWorkEnd: '05:00',
      roundingUnitMinutes: 15,
      roundingMode: 'ROUND_NEAREST',
      monthlyOvertimeAlertHours: 45,
      yearlyOvertimeAlertHours: 360,
      paidLeaveReminderEnabled: true,
      activeFlag: true,
      note: ''
    }
  }

  // 员工适用表单重置回推荐起始值，避免换员工时带出上一人的规则选择。
  const resetRuleAssignmentForm = () => {
    state.ruleAssignmentForm = {
      employeeId: '',
      ruleId: '',
      effectiveStartDate: state.ruleFilters.yearMonth ? `${state.ruleFilters.yearMonth}-01` : '',
      effectiveEndDate: '',
      note: ''
    }
  }

  // 保存规则配置时根据是否存在主键决定走新增还是更新。
  const submitRule = async () => {
    try {
      const payload = { ...state.ruleForm }
      // 编辑既有规则前先让管理员确认影响范围，避免误以为历史月次会自动跟着回刷。
      if (payload.id) {
        const targetName = payload.ruleName || payload.ruleCode || t('ruleFormTitle')
        const confirmed = await requestConfirm({
          title: t('ruleImpactTitle'),
          message: t('ruleImpactMessage').replace('{name}', targetName),
          confirmLabel: t('ruleImpactConfirm'),
          confirmVariant: 'primary'
        })
        if (!confirmed) {
          return
        }
      }
      setSectionLoading('rule', true)
      setSectionError('rule', '')
      const response = payload.id
        ? await updateRule(payload.id, payload)
        : await createRule(payload)
      // 保存后直接把后端返回的正式规则回填到表单，避免用户不知道自己保存的是哪条规则。
      state.ruleForm = { ...response }
      await loadRuleWorkbench()
      await refreshShell()
      pushToast(t('ruleToastSaved'))
    } catch (error) {
      const message = error?.message || t('ruleSaveFailed')
      setSectionError('rule', message)
      pushToast(message)
    } finally {
      setSectionLoading('rule', false)
    }
  }

  // 编辑规则时把当前规则完整回填进主表单，便于就地修正规则口径。
  const editRule = (rule) => {
    state.ruleForm = {
      id: rule.id,
      ruleCode: rule.ruleCode || '',
      ruleName: rule.ruleName || '',
      standardDailyMinutes: rule.standardDailyMinutes ?? 480,
      standardWeeklyMinutes: rule.standardWeeklyMinutes ?? 2400,
      autoBreakEnabled: Boolean(rule.autoBreakEnabled),
      autoBreakThresholdMinutes: rule.autoBreakThresholdMinutes ?? 0,
      autoBreakDeductMinutes: rule.autoBreakDeductMinutes ?? 0,
      nightWorkStart: rule.nightWorkStart || '22:00',
      nightWorkEnd: rule.nightWorkEnd || '05:00',
      roundingUnitMinutes: rule.roundingUnitMinutes ?? 15,
      roundingMode: rule.roundingMode || 'ROUND_NEAREST',
      monthlyOvertimeAlertHours: rule.monthlyOvertimeAlertHours ?? 45,
      yearlyOvertimeAlertHours: rule.yearlyOvertimeAlertHours ?? 360,
      paidLeaveReminderEnabled: Boolean(rule.paidLeaveReminderEnabled),
      activeFlag: Boolean(rule.activeFlag),
      note: rule.note || ''
    }
  }

  // 点员工适用行时把当前适用关系回填到右侧表单，便于直接调整规则或生效日期。
  const editAssignment = (assignment) => {
    state.ruleAssignmentForm = {
      employeeId: assignment.employeeId,
      ruleId: assignment.ruleId || '',
      effectiveStartDate: assignment.effectiveStartDate || `${state.ruleFilters.yearMonth}-01`,
      effectiveEndDate: assignment.effectiveEndDate || '',
      note: assignment.note || ''
    }
  }

  // 保存员工适用时，强制要求先选员工再选规则，避免生成无主体或无规则的脏数据。
  const submitRuleAssignment = async () => {
    if (!state.ruleAssignmentForm.employeeId) {
      pushToast(t('ruleAssignmentNeedEmployee'))
      return
    }
    if (!state.ruleAssignmentForm.ruleId) {
      pushToast(t('ruleAssignmentNeedRule'))
      return
    }
    setSectionLoading('rule', true)
    setSectionError('rule', '')
    try {
      await assignRule(state.ruleAssignmentForm.employeeId, { ...state.ruleAssignmentForm })
      await loadRuleWorkbench()
      await refreshShell()
      pushToast(t('ruleAssignmentToastSaved'))
    } catch (error) {
      const message = error?.message || t('ruleAssignmentSaveFailed')
      setSectionError('rule', message)
      pushToast(message)
    } finally {
      setSectionLoading('rule', false)
    }
  }

  return {
    loadRuleWorkbench,
    resetRuleForm,
    resetRuleAssignmentForm,
    submitRule,
    editRule,
    editAssignment,
    submitRuleAssignment
  }
}
