import {
  bindExternalMapping,
  createConnector,
  fetchConnectorWorkbench,
  retryConnectorSyncLog,
  testConnector,
  updateConnector
} from '../../services'

export const createConnectorSection = ({
  state,
  setSectionLoading,
  setSectionError,
  pushToast,
  t
}) => {
  // 第八阶段接入工作台一次性读取配置、映射和同步日志，避免前端各自拼接不同口径。
  const loadConnectorWorkbench = async () => {
    setSectionLoading('connector', true)
    setSectionError('connector', '')
    try {
      // 把头部筛选条件原样发给后端，让接入工作台由服务端统一聚合。
      const payload = await fetchConnectorWorkbench({ ...state.connectorFilters })
      state.connectorWorkbench = {
        connectors: payload.connectors || [],
        mappings: payload.mappings || [],
        syncLogs: payload.syncLogs || [],
        summary: payload.summary || {
          activeConnectorCount: 0,
          mappedEmployeeCount: 0,
          failedSyncCount: 0,
          latestSyncAt: null
        }
      }
      // 成功后标记第八阶段区块已完成首次加载，避免重复首刷。
      state.bootstrapShell.sectionStates.connector = true
    } catch (error) {
      const message = error?.message || t('connectorLoadFailed')
      setSectionError('connector', message)
      pushToast(message)
    } finally {
      setSectionLoading('connector', false)
    }
  }

  // 新建接入配置时统一回到第八阶段推荐默认值，减少管理员第一次接入时的空表单负担。
  const resetConnectorForm = () => {
    state.connectorForm = {
      id: null,
      sourceSystem: 'WEBHOOK',
      connectorName: '',
      providerType: 'CUSTOM_WEBHOOK',
      receiveMode: 'WEBHOOK',
      apiBaseUrl: '',
      apiKey: '',
      apiSecret: '',
      webhookSecret: '',
      syncCron: '',
      workplaceId: '',
      activeFlag: true,
      note: ''
    }
  }

  // 接入映射表单默认先继承当前接入来源系统，避免映射保存时错绑到别的第三方平台。
  const resetConnectorMappingForm = () => {
    state.connectorMappingForm = {
      employeeId: '',
      sourceSystem: state.connectorForm.sourceSystem || 'WEBHOOK',
      externalEmployeeId: '',
      externalEmployeeNo: '',
      status: 'ACTIVE'
    }
  }

  // 保存接入配置时根据是否存在主键决定走新增还是更新。
  const submitConnector = async () => {
    setSectionLoading('connector', true)
    setSectionError('connector', '')
    try {
      const payload = { ...state.connectorForm }
      // 空事业所不发送给后端，避免把空串当成非法主键。
      if (!payload.workplaceId) {
        payload.workplaceId = null
      }
      const response = payload.id
        ? await updateConnector(payload.id, payload)
        : await createConnector(payload)
      // 保存后把正式回写结果回填到表单，方便管理员立刻继续测试连接或复制 Webhook 地址。
      state.connectorForm = { ...state.connectorForm, ...response }
      await loadConnectorWorkbench()
      pushToast(t('connectorToastSaved'))
    } catch (error) {
      const message = error?.message || t('connectorSaveFailed')
      setSectionError('connector', message)
      pushToast(message)
    } finally {
      setSectionLoading('connector', false)
    }
  }

  // 编辑接入配置时把当前接入完整回填到中栏表单，保证后续测试连接和保存都针对同一条配置。
  const editConnector = (connector) => {
    state.connectorForm = {
      id: connector.id,
      sourceSystem: connector.sourceSystem || 'WEBHOOK',
      connectorName: connector.connectorName || '',
      providerType: connector.providerType || 'CUSTOM_WEBHOOK',
      receiveMode: connector.receiveMode || 'WEBHOOK',
      apiBaseUrl: connector.apiBaseUrl || '',
      // 后端只回掩码，编辑旧配置时不自动回填敏感明文，避免误以为能从前端反解密钥。
      apiKey: '',
      apiSecret: '',
      webhookSecret: '',
      syncCron: connector.syncCron || '',
      workplaceId: connector.workplaceId || '',
      activeFlag: Boolean(connector.activeFlag),
      note: connector.note || ''
    }
    // 切换到某条接入后，同步让映射表单默认跟着这条来源系统走。
    state.connectorMappingForm.sourceSystem = connector.sourceSystem || 'WEBHOOK'
  }

  // 测试连接始终针对当前表单正在编辑的正式接入配置，避免对空表单误点导致无意义请求。
  const submitConnectorTest = async () => {
    if (!state.connectorForm.id) {
      pushToast(t('connectorNeedSaveFirst'))
      return
    }
    setSectionLoading('connector', true)
    setSectionError('connector', '')
    try {
      const payload = await testConnector(state.connectorForm.id)
      state.connectorTestResult = payload
      pushToast(payload.message || t('connectorTestFinished'))
    } catch (error) {
      const message = error?.message || t('connectorTestFailed')
      setSectionError('connector', message)
      pushToast(message)
    } finally {
      setSectionLoading('connector', false)
    }
  }

  // 映射保存继续复用员工外部映射接口，只是把入口收进第八阶段工作台，避免管理员来回切模块。
  const submitConnectorMapping = async () => {
    if (!state.connectorMappingForm.employeeId) {
      pushToast(t('connectorMappingNeedEmployee'))
      return
    }
    setSectionLoading('connector', true)
    setSectionError('connector', '')
    try {
      await bindExternalMapping(state.connectorMappingForm.employeeId, { ...state.connectorMappingForm })
      await loadConnectorWorkbench()
      resetConnectorMappingForm()
      pushToast(t('connectorMappingSaved'))
    } catch (error) {
      const message = error?.message || t('connectorMappingSaveFailed')
      setSectionError('connector', message)
      pushToast(message)
    } finally {
      setSectionLoading('connector', false)
    }
  }

  // 点映射列表时把当前员工和第三方编号回填到表单，便于就地修正外部映射。
  const editConnectorMapping = (mapping) => {
    state.connectorMappingForm = {
      employeeId: mapping.employeeId,
      sourceSystem: mapping.sourceSystem || state.connectorForm.sourceSystem || 'WEBHOOK',
      externalEmployeeId: mapping.externalEmployeeId || '',
      externalEmployeeNo: mapping.externalEmployeeNo || '',
      status: mapping.status || 'ACTIVE'
    }
  }

  // 同步日志重试入口直接复用第八阶段后端重试接口，成功后刷新日志和摘要。
  const submitConnectorRetry = async (log) => {
    setSectionLoading('connector', true)
    setSectionError('connector', '')
    try {
      await retryConnectorSyncLog(log.id)
      await loadConnectorWorkbench()
      pushToast(t('connectorRetrySaved'))
    } catch (error) {
      const message = error?.message || t('connectorRetryFailed')
      setSectionError('connector', message)
      pushToast(message)
    } finally {
      setSectionLoading('connector', false)
    }
  }

  return {
    loadConnectorWorkbench,
    resetConnectorForm,
    resetConnectorMappingForm,
    submitConnector,
    editConnector,
    submitConnectorTest,
    submitConnectorMapping,
    editConnectorMapping,
    submitConnectorRetry
  }
}
