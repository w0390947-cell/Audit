<template>
  <div class="app-container audit-ai-detail-page" v-loading="loading">
    <div class="head-card">
      <div class="page-title">
        <i class="el-icon-back back-icon" @click="goBack" />
        <span>详情</span>
      </div>
    </div>

    <div class="review-detail-blocks">
      <div class="section-card">
        <div class="section-title">任务基础信息</div>
        <table class="info-table">
          <tbody>
            <tr>
              <th>任务编号</th>
              <td>{{ displayValue(detail.taskNo) }}</td>
              <th>产品名称</th>
              <td>{{ displayValue(detail.productName) }}</td>
            </tr>
            <tr>
              <th>送检单位</th>
              <td>{{ displayValue(detail.deliveryUnit) }}</td>
              <th>关联审核版本</th>
              <td>{{ displayValue(reviewDetail.currentVersionNo || detail.reviewVersionId) }}</td>
            </tr>
            <tr>
              <th>报告文件</th>
              <td>{{ displayValue(detail.reportFileName) }}</td>
              <th>提交人</th>
              <td>{{ displayValue(detail.submitter) }}</td>
            </tr>
            <tr>
              <th>提交时间</th>
              <td>{{ parseTime(detail.submitTime, '{y}-{m}-{d} {h}:{i}:{s}') || '--' }}</td>
              <th>AI任务状态</th>
              <td>
                <dict-tag v-if="detail.taskStatus" :options="dict.type.audit_ai_task_status" :value="detail.taskStatus" />
                <span v-else>--</span>
              </td>
            </tr>
            <tr>
              <th>优先级</th>
              <td>
                <span v-if="detail.priority" :class="['priority-text', 'priority-' + detail.priority]">{{ priorityLabel(detail.priority) }}</span>
                <span v-else>--</span>
              </td>
              <th>AI分析次数</th>
              <td>{{ displayValue(detail.aiAnalysisCount) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="section-card">
        <div class="section-title">流转状态</div>
        <div class="stage-section">
          <div class="stage-line" />
          <div v-for="(item, index) in displayAiFlowStageList" :key="item.stageKey || item.stageCode || index" class="stage-item">
            <div :class="['stage-node', 'stage-' + item.status]">{{ index + 1 }}</div>
            <div class="stage-name-row">
              <span class="stage-name">{{ item.stageName }}</span>
              <i :class="[flowStatusIcon(item.status), 'stage-status-icon', 'stage-status-' + item.status]" />
              <span :class="['stage-status-text', 'stage-status-' + item.status]">{{ item.statusText }}</span>
            </div>
            <div class="stage-time">{{ item.timeText }}</div>
            <div class="stage-card">
              <div class="stage-card-head">
                <span>{{ item.summary }}</span>
                <el-button
                  v-if="item.hasLog"
                  type="text"
                  size="mini"
                  class="stage-log-btn"
                  @click="openStageLog(item)"
                >处理日志</el-button>
                <span v-else class="stage-log-empty">暂无日志</span>
              </div>
              <div v-for="(line, lineIndex) in item.lines" :key="'stage_' + index + '_' + lineIndex" class="stage-card-line">
                {{ line }}
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>

    <div class="detail-grid">
      <div class="preview-column">
        <div class="section-card">
          <div class="section-title">报告预览</div>
          <div class="report-shell" v-loading="previewLoading">
            <object
              v-if="previewReady"
              :key="previewViewerKey"
              :data="previewViewerSrc"
              type="application/pdf"
              class="preview-frame"
            >
              <iframe :src="previewViewerSrc" class="preview-frame" frameborder="0" />
            </object>
            <div v-else class="preview-empty">
              <i class="el-icon-document" />
              <span>{{ previewEmptyText }}</span>
            </div>
          </div>
          <div class="preview-actions">
            <el-button type="primary" size="small" icon="el-icon-refresh" @click="getPreview" :loading="previewLoading">刷新预览</el-button>
            <el-button size="small" icon="el-icon-download" @click="downloadReport">下载原文件</el-button>
          </div>
        </div>
      </div>

      <div class="side-column">
        <div class="section-card">
          <div class="section-title result-title">
            <span>检测结果</span>
            <el-button
              size="mini"
              type="primary"
              plain
              icon="el-icon-download"
              :disabled="!canDownloadDetectionResult"
              @click="downloadDetectionResultPdf"
            >下载检测结果</el-button>
          </div>
          <div v-if="displayIssueList.length">
            <div
              v-for="(item, index) in displayIssueList"
              :key="issueKey(item, index)"
              :class="['issue-card', 'issue-card-clickable', { 'issue-card-active': selectedIssueKey === issueKey(item, index) }]"
              role="button"
              tabindex="0"
              @click="handleIssueClick(item, index)"
              @keyup.enter="handleIssueClick(item, index)"
            >
              <div class="issue-title" :class="{ 'format': index > 0 }">
                <i class="el-icon-warning" />
                <span>{{ item.issueTitle || '识别异常类型' }}</span>
                <span v-if="resolveIssuePage(item)" class="issue-page-tag">第{{ resolveIssuePage(item) }}页</span>
              </div>
              <div v-for="(line, lineIndex) in issueLines(item)" :key="'issue_' + index + '_' + lineIndex" class="issue-line">
                {{ line }}
              </div>
              <div v-if="issueQuote(item)" class="issue-quote">
                <div class="issue-quote-label">报告原文引用</div>
                <div class="issue-quote-text">{{ issueQuote(item) }}</div>
              </div>
              <div
                v-if="issueBasisReferences(item).length"
                class="issue-basis"
                @click.stop
                @keyup.enter.stop
              >
                <el-collapse>
                  <el-collapse-item
                    :title="'审核依据引用（' + issueBasisReferences(item).length + '条）'"
                    :name="'basis_' + issueKey(item, index)"
                  >
                    <div
                      v-for="(basis, basisIndex) in issueBasisReferences(item)"
                      :key="'basis_' + index + '_' + basisIndex"
                      class="issue-basis-item"
                    >
                      <div class="issue-basis-title">依据{{ basisIndex + 1 }}</div>
                      <div v-if="basisMetaParts(basis).length" class="issue-basis-meta">
                        <span
                          v-for="(part, partIndex) in basisMetaParts(basis)"
                          :key="'basis_meta_' + index + '_' + basisIndex + '_' + partIndex"
                        >{{ part }}</span>
                      </div>
                      <div v-if="basis.quoteText" class="issue-basis-quote">{{ basis.quoteText }}</div>
                      <div v-if="basis.conflictDescription" class="issue-basis-conflict">
                        <span class="issue-basis-conflict-label">依据冲突：</span>{{ basis.conflictDescription }}
                      </div>
                    </div>
                  </el-collapse-item>
                </el-collapse>
              </div>
            </div>
          </div>
          <div v-else class="result-empty">暂无检测结果</div>
        </div>

        <div class="section-card">
          <div class="section-title">建议修改区</div>
          <el-input
            v-model="reviewForm.reviewOpinion"
            type="textarea"
            :rows="6"
            placeholder="请输入文字..."
            resize="none"
          />
          <div v-if="showDecisionActions" class="decision-actions">
            <el-button type="primary" size="small" @click="submitDecision('approved')" v-hasPermi="['audit:ai:review']">审核通过</el-button>
            <el-button size="small" @click="submitDecision('pending')" v-hasPermi="['audit:ai:review']">待修改</el-button>
            <el-button type="danger" size="small" @click="submitDecision('returned')" v-hasPermi="['audit:ai:review']">驳回</el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog title="处理日志" :visible.sync="stageLogOpen" width="680px" append-to-body>
      <div v-if="currentStageLog" class="stage-log-dialog">
        <div class="stage-log-title">{{ currentStageLog.stageName }}</div>
        <div v-if="currentStageLog.stageDetail" class="stage-log-block">
          <div class="stage-log-label">阶段详情</div>
          <pre>{{ currentStageLog.stageDetail }}</pre>
        </div>
        <div v-if="currentStageLog.errorMessage" class="stage-log-block error">
          <div class="stage-log-label">错误信息</div>
          <pre>{{ currentStageLog.errorMessage }}</pre>
        </div>
        <div v-if="currentStageLog.outputText" class="stage-log-block">
          <div class="stage-log-label">阶段输出</div>
          <pre>{{ currentStageLog.outputText }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAiReportPreview, getAiTask, reviewAiTask } from '@/api/audit/ai'
import { getReview } from '@/api/audit/review'

export default {
  name: 'AuditAiDetail',
  dicts: ['audit_ai_task_status'],
  data() {
    return {
      loading: true,
      previewLoading: false,
      previewInfo: null,
      previewError: '',
      detail: {
        findingList: [],
        flowStageList: []
      },
      reviewDetail: {
        stageList: [],
        issueList: [],
        versionList: []
      },
      reviewForm: {
        reviewOpinion: ''
      },
      currentPreviewPage: null,
      previewJumpSeq: 0,
      selectedIssueKey: '',
      stageLogOpen: false,
      currentStageLog: null,
      detailPollTimer: null,
      reviewDecisionSubmittedInCurrentView: false
    }
  },
  computed: {
    currentAiTaskId() {
      const aiTaskId = Number(this.$route.params.aiTaskId)
      return Number.isFinite(aiTaskId) && aiTaskId > 0 ? aiTaskId : null
    },
    previewReady() {
      return !!(this.previewInfo && this.previewInfo.previewFileUrl)
    },
    previewViewerSrc() {
      if (!this.previewReady) {
        return ''
      }
      return this.withPdfPageHash(this.toAbsoluteUrl(this.previewInfo.previewFileUrl), this.currentPreviewPage)
    },
    previewViewerKey() {
      return this.previewViewerSrc + ':' + this.previewJumpSeq
    },
    previewEmptyText() {
      if (this.previewLoading) {
        return '正在生成报告预览...'
      }
      if (this.previewError) {
        return this.previewError
      }
      if (!this.detail.reportFileUrl) {
        return '当前任务未绑定报告文件'
      }
      return '暂无可预览报告'
    },
    aiFlowStageList() {
      const missingStages = this.flowStageMissingContext()
      return this.compactFlowStageList([]).map(stage => {
        return this.buildMissingFlowStage(stage, missingStages[stage.stageCode])
      })
    },
    displayAiFlowStageList() {
      if (Array.isArray(this.detail.flowStageList) && this.detail.flowStageList.length) {
        const stages = this.detail.flowStageList.map(this.normalizeFlowStage)
        return this.compactFlowStageList(stages)
      }
      return this.aiFlowStageList
    },
    displayIssueList() {
      if (Array.isArray(this.detail.findingList) && this.detail.findingList.length) {
        return this.detail.findingList.map(item => ({
          issueId: item.findingId,
          issueType: item.findingType,
          issueTitle: this.formatFindingTitle(item),
          issueContent: item.findingContent,
          quoteText: item.quoteText,
          pageNo: item.pageNo,
          locationJson: item.locationJson
        }))
      }
      return []
    },
    canDownloadDetectionResult() {
      return this.detail.taskStatus === 'completed'
    },
    showDecisionActions() {
      return this.detail.reviewStatus !== 'approved' || this.reviewDecisionSubmittedInCurrentView
    }
  },
  created() {
    this.ensureRouteAndFetch()
  },
  activated() {
    this.ensureRouteAndFetch()
  },
  deactivated() {
    this.reviewDecisionSubmittedInCurrentView = false
    this.stopDetailPolling()
  },
  beforeDestroy() {
    this.stopDetailPolling()
  },
  watch: {
    '$route.params.aiTaskId'() {
      this.reviewDecisionSubmittedInCurrentView = false
      this.stopDetailPolling()
      this.ensureRouteAndFetch()
    }
  },
  methods: {
    displayValue(value) {
      return value === null || value === undefined || value === '' ? '--' : value
    },
    priorityLabel(priority) {
      if (priority === 'high') {
        return '高优先级'
      }
      if (priority === 'medium') {
        return '中优先级'
      }
      return '低优先级'
    },
    normalizeFlowStage(item, index) {
      const detailLines = this.splitStructuredText(item.stageDetail)
      const outputText = this.formatJsonText(item.outputJson)
      const errorMessage = item.errorMessage || ''
      return {
        stageKey: item.stageId || item.stageInstanceId || item.stageCode || index,
        stageCode: item.stageCode,
        stageName: item.stageName || item.stageCode || '执行阶段',
        status: this.normalizeBackendStageStatus(item.stageStatus),
        statusText: this.backendStageStatusLabel(item.stageStatus),
        startTime: item.startTime,
        endTime: item.endTime,
        durationMs: item.durationMs,
        agentName: item.agentName,
        timeText: this.stageTimeText(item.startTime, item.endTime, item.durationMs),
        summary: item.stageSummary || errorMessage || '--',
        lines: detailLines.length ? detailLines : this.stageFallbackLines(item, outputText, errorMessage),
        stageDetail: item.stageDetail,
        outputText,
        outputData: this.parseJsonValue(item.outputJson),
        errorMessage,
        hasLog: !!(item.stageDetail || outputText || errorMessage)
      }
    },
    compactFlowStageList(stages) {
      const sortedStages = [...stages].sort((left, right) => Number(left.sortNum || 0) - Number(right.sortNum || 0))
      const groups = [
        {
          stageCode: 'task_receive',
          stageName: '任务接收',
          codes: ['queued', 'input_validate']
        },
        {
          stageCode: 'content_parse',
          stageName: '内容解析',
          codes: ['file_parse', 'target_file_parse', 'basis_file_parse', 'text_split', 'target_text_split']
        },
        {
          stageCode: 'audit_analysis',
          stageName: '审核分析',
          codes: ['knowledge_retrieve', 'uploaded_basis_match', 'basis_pack_or_match', 'ai_audit', 'result_validate']
        },
        {
          stageCode: 'result_process',
          stageName: '结果处理',
          codes: ['result_save', 'callback']
        }
      ]
      const usedKeys = new Set()
      const compactList = groups.map(group => {
        const children = sortedStages.filter(stage => {
          const matched = group.codes.includes(stage.stageCode)
          if (matched) {
            usedKeys.add(stage.stageKey)
          }
          return matched
        })
        return this.buildCompactFlowStage(group, children)
      })
      const extraStages = sortedStages.filter(stage => !usedKeys.has(stage.stageKey))
      if (extraStages.length) {
        const auditStage = compactList.find(item => item.stageCode === 'audit_analysis')
        Object.assign(auditStage, this.buildCompactFlowStage(groups[2], auditStage.children.concat(extraStages)))
      }
      return compactList
    },
    buildCompactFlowStage(group, children) {
      const status = group.stageCode === 'result_process'
        ? this.compactResultStatus(children)
        : this.aggregateFlowStatus(children)
      return {
        stageKey: group.stageCode,
        stageCode: group.stageCode,
        stageName: group.stageName,
        status,
        statusText: group.stageCode === 'result_process' && status === this.reviewStageStatus(this.detail.reviewStatus)
          ? this.reviewStatusLabel(this.detail.reviewStatus)
          : this.flowStatusLabel(status),
        timeText: this.compactStageTimeText(children, group.stageCode),
        summary: this.compactStageSummary(group.stageCode, children, status),
        lines: this.compactStageLines(group.stageCode, children),
        stageDetail: this.compactStageDetail(children, group.stageCode),
        outputText: this.compactStageOutput(children),
        errorMessage: children.map(item => item.errorMessage).filter(Boolean).join('\n'),
        hasLog: children.length > 0 || group.stageCode === 'result_process',
        children
      }
    },
    aggregateFlowStatus(children) {
      if (!children.length) {
        return 'pending'
      }
      const statuses = children.map(item => item.status)
      if (statuses.includes('failed')) {
        return 'failed'
      }
      if (statuses.includes('running')) {
        return 'running'
      }
      if (statuses.includes('waiting')) {
        return 'waiting'
      }
      if (statuses.includes('paused')) {
        return 'paused'
      }
      return statuses.every(status => status === 'done') ? 'done' : 'pending'
    },
    compactResultStatus(children) {
      const workflowStatus = this.aggregateFlowStatus(children)
      if (workflowStatus !== 'done') {
        return workflowStatus
      }
      return this.reviewStageStatus(this.detail.reviewStatus)
    },
    compactStageTimeText(children, stageCode) {
      const start = this.boundaryStageTime(children, 'startTime', 'min')
      const end = this.boundaryStageTime(children, 'endTime', 'max')
      const duration = children.reduce((total, item) => total + Number(item.durationMs || 0), 0)
      if (start || end) {
        return this.stageTimeText(start, end, duration)
      }
      if (stageCode === 'result_process') {
        return this.parseTime(this.detail.updateTime, '{y}-{m}-{d} {h}:{i}:{s}') || '--'
      }
      return '--'
    },
    boundaryStageTime(children, field, direction) {
      const times = children
        .map(item => item[field])
        .filter(Boolean)
        .map(value => ({ value, time: new Date(value).getTime() }))
        .filter(item => Number.isFinite(item.time))
      if (!times.length) {
        return ''
      }
      times.sort((left, right) => direction === 'min' ? left.time - right.time : right.time - left.time)
      return times[0].value
    },
    compactStageSummary(stageCode, children, status) {
      if (status === 'failed') {
        return '处理失败，请查看处理日志'
      }
      if (stageCode === 'task_receive') {
        return status === 'done' ? '任务已接收并完成输入校验' : '等待任务接收'
      }
      if (stageCode === 'content_parse') {
        return status === 'done' ? '报告内容解析完成' : '报告内容解析处理中'
      }
      if (stageCode === 'audit_analysis') {
        const count = this.outputValue(children, ['ai_audit', 'result_validate'], ['finding_count', 'totalIssues', 'total_issues'])
        return count !== '' ? '审核分析完成，发现' + count + '个问题' : '审核分析完成'
      }
      if (stageCode === 'result_process') {
        return this.reviewStageSummary(this.detail.reviewStatus)
      }
      return '--'
    },
    compactStageLines(stageCode, children) {
      if (stageCode === 'task_receive') {
        return [
          '任务编号：' + this.displayValue(this.detail.taskNo),
          '报告文件：' + this.displayValue(this.detail.reportFileName),
          this.childSummaryLine(children, 'input_validate')
        ].filter(Boolean)
      }
      if (stageCode === 'content_parse') {
        return [
          this.outputLine(children, ['file_parse', 'target_file_parse'], '解析文本', ['char_count'], '字'),
          this.outputLine(children, ['file_parse', 'target_file_parse'], '识别段落', ['block_count'], '块'),
          this.outputLine(children, ['text_split', 'target_text_split'], '生成分片', ['chunk_count'], '个')
        ].filter(Boolean)
      }
      if (stageCode === 'audit_analysis') {
        return [
          this.outputLine(children, ['knowledge_retrieve', 'uploaded_basis_match', 'basis_pack_or_match'], '依据命中', ['reference_count', 'references_used_in_prompt', 'basis_chunks_used_in_prompt'], '条'),
          this.outputLine(children, ['ai_audit'], '模型调用', ['model_call_count'], '次', ['model_usage_summary']),
          this.outputLine(children, ['ai_audit', 'result_validate'], '发现问题', ['finding_count', 'totalIssues', 'total_issues'], '个')
        ].filter(Boolean)
      }
      return [
        this.outputLine(children, ['result_save'], '保存问题', ['issue_count'], '条'),
        this.childSummaryLine(children, 'callback'),
        '人工复核：' + this.reviewStatusLabel(this.detail.reviewStatus)
      ].filter(Boolean)
    },
    childSummaryLine(children, stageCode) {
      const child = children.find(item => item.stageCode === stageCode)
      return child ? child.summary : ''
    },
    outputLine(children, stageCodes, label, keys, unit, nestedPath) {
      const value = this.outputValue(children, stageCodes, keys, nestedPath)
      return value === '' ? '' : label + '：' + value + (unit || '')
    },
    outputValue(children, stageCodes, keys, nestedPath) {
      for (const child of children.filter(item => stageCodes.includes(item.stageCode))) {
        let output = child.outputData || {}
        for (const path of nestedPath || []) {
          output = output && output[path] ? output[path] : {}
        }
        for (const key of keys) {
          if (output[key] !== undefined && output[key] !== null && output[key] !== '') {
            return output[key]
          }
        }
      }
      return ''
    },
    compactStageDetail(children, stageCode) {
      const logs = children.map(item => {
        const lines = [
          item.stageName + '｜' + item.statusText + '｜' + item.timeText,
          item.summary,
          ...(item.lines || [])
        ].filter(Boolean)
        return lines.join('\n')
      })
      if (stageCode === 'result_process') {
        logs.push('人工复核｜' + this.reviewStatusLabel(this.detail.reviewStatus) + '｜' + (this.parseTime(this.detail.updateTime, '{y}-{m}-{d} {h}:{i}:{s}') || '--'))
        logs.push(this.reviewStageSummary(this.detail.reviewStatus))
      }
      return logs.join('\n\n')
    },
    compactStageOutput(children) {
      return children
        .filter(item => item.outputText)
        .map(item => '【' + item.stageName + '】\n' + item.outputText)
        .join('\n\n')
    },
    normalizeBackendStageStatus(stageStatus) {
      const map = {
        pending: 'pending',
        waiting: 'waiting',
        running: 'running',
        completed: 'done',
        success: 'done',
        failed: 'failed',
        skipped: 'paused',
        cancelled: 'paused',
        canceled: 'paused'
      }
      return map[String(stageStatus || '').toLowerCase()] || 'pending'
    },
    backendStageStatusLabel(stageStatus) {
      return this.flowStatusLabel(this.normalizeBackendStageStatus(stageStatus))
    },
    stageTimeText(startTime, endTime, durationMs) {
      const start = this.parseTime(startTime, '{y}-{m}-{d} {h}:{i}:{s}')
      const end = this.parseTime(endTime, '{y}-{m}-{d} {h}:{i}:{s}')
      const duration = this.durationText(durationMs)
      if (start && end) {
        return start + ' 至 ' + end + (duration ? '，耗时' + duration : '')
      }
      if (start) {
        return start + (duration ? '，耗时' + duration : '')
      }
      if (end) {
        return end + (duration ? '，耗时' + duration : '')
      }
      return duration || '--'
    },
    durationText(durationMs) {
      const value = Number(durationMs)
      if (!Number.isFinite(value) || value <= 0) {
        return ''
      }
      if (value < 1000) {
        return value + '毫秒'
      }
      if (value < 60000) {
        return Math.round(value / 1000) + '秒'
      }
      return Math.round(value / 60000) + '分钟'
    },
    stageFallbackLines(item, outputText, errorMessage) {
      const lines = []
      if (item.agentName) {
        lines.push('处理节点：' + item.agentName)
      }
      if (errorMessage) {
        lines.push('错误信息：' + errorMessage)
      }
      if (outputText) {
        lines.push('阶段输出：' + outputText)
      }
      return lines.length ? lines : ['暂无阶段详情']
    },
    formatJsonText(value) {
      if (!value) {
        return ''
      }
      try {
        const parsed = this.parseJsonValue(value)
        return JSON.stringify(parsed, null, 2)
      } catch (e) {
        return String(value)
      }
    },
    parseJsonValue(value) {
      if (!value) {
        return {}
      }
      if (typeof value === 'object') {
        return value
      }
      return JSON.parse(value)
    },
    openStageLog(item) {
      this.currentStageLog = item
      this.stageLogOpen = true
    },
    flowStageMissingContext() {
      const taskStatus = this.detail.taskStatus
      if (taskStatus === 'completed') {
        return {
          task_receive: {
            status: 'done',
            summary: '任务已接收',
            lines: ['未收到任务接收阶段日志。']
          },
          content_parse: {
            status: 'done',
            summary: '内容解析已完成',
            lines: ['未收到文件解析、文本切分等阶段日志。']
          },
          audit_analysis: {
            status: 'done',
            summary: '审核分析已完成',
            lines: ['未收到知识检索、AI审核等阶段日志。']
          },
          result_process: {
            status: 'done',
            summary: '结果处理已完成',
            lines: ['未收到结果保存、回调处理等阶段日志。']
          }
        }
      }
      if (taskStatus === 'failed') {
        return {
          task_receive: {
            status: 'done',
            summary: '任务已接收',
            lines: ['未收到任务接收阶段日志。']
          },
          content_parse: {
            status: 'unknown',
            summary: '阶段状态未知',
            lines: ['任务已失败，但未收到内容解析阶段日志，无法确认该阶段是否完成。']
          },
          audit_analysis: {
            status: 'unknown',
            summary: '阶段状态未知',
            lines: ['任务已失败，但未收到审核分析阶段日志，无法确认失败是否发生在该阶段。']
          },
          result_process: {
            status: 'unknown',
            summary: '阶段状态未知',
            lines: ['任务已失败，但未收到结果处理阶段日志，无法确认结果是否写入完成。']
          }
        }
      }
      if (taskStatus === 'executing') {
        return {
          task_receive: {
            status: 'done',
            summary: '任务已接收',
            lines: ['未收到任务接收阶段日志。']
          },
          content_parse: {
            status: 'waiting',
            summary: '等待阶段回调',
            lines: ['工作流执行中，等待内容解析阶段回传真实状态。']
          },
          audit_analysis: {
            status: 'waiting',
            summary: '等待阶段回调',
            lines: ['工作流执行中，等待审核分析阶段回传真实状态。']
          },
          result_process: {
            status: 'waiting',
            summary: '等待阶段回调',
            lines: ['工作流执行中，等待结果处理阶段回传真实状态。']
          }
        }
      }
      if (taskStatus === 'paused') {
        return {
          task_receive: {
            status: 'done',
            summary: '任务已接收',
            lines: ['未收到任务接收阶段日志。']
          },
          content_parse: {
            status: 'paused',
            summary: '任务已暂停',
            lines: ['未收到内容解析阶段日志，恢复执行后等待工作流回传真实状态。']
          },
          audit_analysis: {
            status: 'paused',
            summary: '任务已暂停',
            lines: ['未收到审核分析阶段日志，恢复执行后等待工作流回传真实状态。']
          },
          result_process: {
            status: 'paused',
            summary: '任务已暂停',
            lines: ['未收到结果处理阶段日志，恢复执行后等待工作流回传真实状态。']
          }
        }
      }
      return {
        task_receive: {
          status: this.detail.aiTaskId || this.detail.taskNo ? 'done' : 'pending',
          summary: this.detail.aiTaskId || this.detail.taskNo ? '任务已接收' : '等待任务接收',
          lines: ['未收到任务接收阶段日志。']
        },
        content_parse: {
          status: 'waiting',
          summary: '等待内容解析',
          lines: ['工作流尚未回传内容解析阶段状态。']
        },
        audit_analysis: {
          status: 'waiting',
          summary: '等待审核分析',
          lines: ['工作流尚未回传审核分析阶段状态。']
        },
        result_process: {
          status: 'waiting',
          summary: '等待结果处理',
          lines: ['工作流尚未回传结果处理阶段状态。']
        }
      }
    },
    buildMissingFlowStage(stage, context) {
      const stageContext = context || {
        status: 'unknown',
        summary: '阶段状态未知',
        lines: ['未收到该阶段日志。']
      }
      return {
        ...stage,
        status: stageContext.status,
        statusText: this.flowStatusLabel(stageContext.status),
        timeText: '--',
        summary: stageContext.summary,
        lines: stageContext.lines,
        children: [],
        outputText: '',
        errorMessage: '',
        stageDetail: '',
        hasLog: false
      }
    },
    reviewStageStatus(reviewStatus) {
      if (reviewStatus === 'approved') {
        return 'done'
      }
      if (reviewStatus === 'returned') {
        return 'failed'
      }
      if (reviewStatus === 'pending') {
        return 'paused'
      }
      return 'pending'
    },
    flowStatusLabel(status) {
      const map = {
        done: '已完成',
        running: '执行中',
        waiting: '等待中',
        paused: '已暂停',
        failed: '已失败',
        unknown: '状态未知',
        pending: '未开始'
      }
      return map[status] || '--'
    },
    flowStatusIcon(status) {
      if (status === 'done') {
        return 'el-icon-success'
      }
      if (status === 'running') {
        return 'el-icon-loading'
      }
      if (status === 'failed') {
        return 'el-icon-error'
      }
      if (status === 'paused') {
        return 'el-icon-warning'
      }
      if (status === 'unknown') {
        return 'el-icon-warning-outline'
      }
      return 'el-icon-time'
    },
    taskStatusLabel(taskStatus) {
      const map = {
        waiting: '等待中',
        executing: '执行中',
        paused: '已暂停',
        completed: '已完成',
        failed: '已失败'
      }
      return map[taskStatus] || '--'
    },
    reviewStatusLabel(reviewStatus) {
      if (reviewStatus === 'approved') {
        return '已通过'
      }
      if (reviewStatus === 'returned') {
        return '已驳回'
      }
      if (reviewStatus === 'pending') {
        return '待修改'
      }
      return '待复核'
    },
    reviewStageSummary(reviewStatus) {
      if (reviewStatus === 'approved') {
        return '人工复核已通过'
      }
      if (reviewStatus === 'returned') {
        return '人工复核已驳回'
      }
      if (reviewStatus === 'pending') {
        return '人工复核要求修改'
      }
      return '等待人工复核AI结果'
    },
    ensureRouteAndFetch() {
      if (!this.currentAiTaskId) {
        this.loading = false
        return
      }
      this.getDetail()
    },
    getDetail() {
      this.loading = true
      this.previewInfo = null
      this.previewError = ''
      this.currentPreviewPage = null
      this.previewJumpSeq = 0
      this.selectedIssueKey = ''
      this.reviewDetail = {
        stageList: [],
        issueList: [],
        versionList: []
      }
      getAiTask(this.currentAiTaskId).then(response => {
        this.detail = response.data || { findingList: [] }
        this.reviewForm.reviewOpinion = this.detail.reviewOpinion || ''
        this.syncDetailPolling()
        this.getReviewDetail().then(() => {
          this.loading = false
        })
        this.getPreview()
      }).catch(() => {
        this.loading = false
      })
    },
    refreshDetailForPolling() {
      if (!this.currentAiTaskId || this.loading) {
        return
      }
      getAiTask(this.currentAiTaskId).then(response => {
        this.detail = response.data || { findingList: [] }
        this.reviewForm.reviewOpinion = this.detail.reviewOpinion || ''
        this.syncDetailPolling()
      }).catch(() => {
        this.stopDetailPolling()
      })
    },
    syncDetailPolling() {
      if (this.shouldPollDetail()) {
        this.startDetailPolling()
      } else {
        this.stopDetailPolling()
      }
    },
    shouldPollDetail() {
      return this.detail.taskStatus === 'waiting' || this.detail.taskStatus === 'executing'
    },
    startDetailPolling() {
      if (this.detailPollTimer) {
        return
      }
      this.detailPollTimer = setInterval(() => {
        this.refreshDetailForPolling()
      }, 5000)
    },
    stopDetailPolling() {
      if (!this.detailPollTimer) {
        return
      }
      clearInterval(this.detailPollTimer)
      this.detailPollTimer = null
    },
    getReviewDetail() {
      if (!this.detail.reviewTaskId) {
        return Promise.resolve()
      }
      return getReview(this.detail.reviewTaskId, this.detail.reviewVersionId).then(response => {
        this.reviewDetail = response.data || {
          stageList: [],
          issueList: [],
          versionList: []
        }
      }).catch(() => {
        this.reviewDetail = {
          stageList: [],
          issueList: [],
          versionList: []
        }
      })
    },
    goBack() {
      this.$router.push('/audit-ai/queue')
    },
    getPreview() {
      if (!this.currentAiTaskId || !this.detail.reportFileUrl) {
        this.previewInfo = null
        this.previewError = ''
        this.currentPreviewPage = null
        return
      }
      this.previewLoading = true
      this.previewError = ''
      getAiReportPreview(this.currentAiTaskId).then(response => {
        this.previewInfo = response.data || null
        this.previewLoading = false
      }).catch(error => {
        this.previewInfo = null
        this.previewError = error && error.message ? error.message : '报告预览生成失败'
        this.previewLoading = false
      })
    },
    downloadReport() {
      if (!this.detail.reportFileUrl) {
        this.$message.warning('当前任务未绑定报告文件')
        return
      }
      window.open(encodeURI(this.detail.reportFileUrl))
    },
    downloadDetectionResultPdf() {
      if (!this.canDownloadDetectionResult) {
        this.$message.warning('AI审核分析完成后才可以下载检测结果')
        return
      }
      const aiTaskId = this.detail.aiTaskId || this.currentAiTaskId
      if (!aiTaskId) {
        this.$message.warning('AI任务参数缺失，无法下载检测结果')
        return
      }
      const taskNo = this.safeFileName(this.detail.taskNo || aiTaskId)
      this.download('audit/ai/' + aiTaskId + '/detectionResultPdf', {}, 'AI检测结果_' + taskNo + '.pdf')
    },
    handleIssueClick(item, index) {
      const page = this.resolveIssuePage(item)
      if (!page || !this.jumpToReportPage(page)) {
        this.$message.warning('暂无定位信息')
        return
      }
      this.selectedIssueKey = this.issueKey(item, index)
    },
    jumpToReportPage(page) {
      const pageNo = this.normalizePageNo(page)
      if (!pageNo || !this.previewReady) {
        return false
      }
      const totalPages = this.previewTotalPages()
      if (totalPages && pageNo > totalPages) {
        return false
      }
      this.currentPreviewPage = pageNo
      this.previewJumpSeq += 1
      return true
    },
    previewTotalPages() {
      const candidates = [
        this.previewInfo && this.previewInfo.totalPages,
        this.previewInfo && this.previewInfo.pageCount,
        this.previewInfo && this.previewInfo.pages
      ]
      for (const item of candidates) {
        const page = this.normalizePageNo(item)
        if (page) {
          return page
        }
      }
      return null
    },
    resolveIssuePage(item) {
      if (!item) {
        return null
      }
      const location = this.parseLocationJson(item.locationJson) || item.location || {}
      const candidates = [
        location.page,
        location.pageNo,
        location.page_no,
        item.page,
        item.pageNo,
        item.page_no
      ]
      for (const candidate of candidates) {
        const page = this.normalizePageNo(candidate)
        if (page) {
          return page
        }
      }
      return null
    },
    parseLocationJson(value) {
      if (!value) {
        return null
      }
      if (typeof value === 'object') {
        return value
      }
      try {
        const parsed = JSON.parse(value)
        return parsed && typeof parsed === 'object' ? parsed : null
      } catch (e) {
        return null
      }
    },
    normalizePageNo(value) {
      if (value === null || value === undefined || value === '') {
        return null
      }
      const page = Number(value)
      if (!Number.isInteger(page) || page < 1) {
        return null
      }
      return page
    },
    toAbsoluteUrl(url) {
      if (!url) {
        return ''
      }
      try {
        return new URL(url, window.location.origin).href
      } catch (e) {
        return url
      }
    },
    withPdfPageHash(url, page) {
      if (!url) {
        return ''
      }
      const pageNo = this.normalizePageNo(page)
      if (!pageNo) {
        return url
      }
      return url.split('#')[0] + '#page=' + pageNo
    },
    issueKey(item, index) {
      return String(item.issueId || item.issueKey || item.id || index)
    },
    splitStructuredText(text) {
      if (!text) {
        return []
      }
      return String(text)
        .replace(/\n/g, '；')
        .split('；')
        .map(item => item.trim())
        .filter(Boolean)
    },
    sanitizeIssueContent(text) {
      if (!text) {
        return ''
      }
      return String(text).replace(/位置：\{[^{}]*\}/g, (matched) => {
        const jsonText = matched.replace(/^位置：/, '')
        const location = this.parseLocationJson(jsonText)
        const displayText = this.formatLocationText(location)
        return displayText ? '位置：' + displayText : ''
      })
    },
    formatLocationText(location) {
      if (!location || typeof location !== 'object') {
        return ''
      }
      const pageNo = this.normalizePageNo(location.page || location.pageNo || location.page_no)
      const section = String(location.section || '').trim()
      if (pageNo && section) {
        return '第' + pageNo + '页，' + section
      }
      if (pageNo) {
        return '第' + pageNo + '页'
      }
      return section
    },
    formatFindingTitle(item) {
      const type = item.findingType || '其他'
      const title = item.findingTitle || 'AI发现问题'
      return type + ' - ' + title
    },
    issueLines(item) {
      const lines = this.splitStructuredText(this.sanitizeIssueContent(item.issueContent))
      if (lines.length) {
        return lines
      }
      return ['暂无问题描述']
    },
    issueQuote(item) {
      if (!item) {
        return ''
      }
      return String(item.quoteText || '').trim()
    },
    issueBasisReferences(item) {
      if (!item) {
        return []
      }
      const location = this.parseLocationJson(item.locationJson) || item.location || {}
      const rawBasis = location.basis_references ||
        location.basisReferences ||
        location.basis ||
        item.basisReferences ||
        item.basisReferenceList ||
        item.basis
      if (Array.isArray(rawBasis)) {
        return rawBasis.map(item => this.normalizeBasisReference(item)).filter(item => item.hasContent)
      }
      const normalized = this.normalizeBasisReference(rawBasis)
      return normalized.hasContent ? [normalized] : []
    },
    normalizeBasisReference(value) {
      const item = value && typeof value === 'object' ? value : {}
      const fileName = this.firstBasisValue(item, ['file_name', 'fileName', 'source', 'basis_file_name', 'basisFileName'])
      const versionNo = this.firstBasisValue(item, ['version_no', 'versionNo', 'version'])
      const pageNo = this.normalizePageNo(this.firstBasisValue(item, ['page', 'pageNo', 'page_no']))
      const sectionTitle = this.firstBasisValue(item, ['section', 'section_title', 'sectionTitle'])
      const ruleCode = this.firstBasisValue(item, ['rule_code', 'ruleCode'])
      const quoteText = this.firstBasisValue(item, ['quote', 'quote_text', 'basis_quote', 'basisQuote', 'content'])
      const conflictDescription = this.firstBasisValue(item, [
        'conflict_description',
        'conflictDescription',
        'conflict_note',
        'conflictNote',
        'basis_conflict',
        'basisConflict'
      ])
      return {
        fileName,
        versionNo,
        pageNo,
        sectionTitle,
        ruleCode,
        quoteText,
        conflictDescription,
        hasContent: !!(fileName || versionNo || pageNo || sectionTitle || ruleCode || quoteText || conflictDescription)
      }
    },
    firstBasisValue(item, keys) {
      for (const key of keys) {
        if (item[key] !== null && item[key] !== undefined && String(item[key]).trim() !== '') {
          return String(item[key]).trim()
        }
      }
      return ''
    },
    basisMetaParts(basis) {
      const parts = []
      if (basis.fileName) {
        parts.push('标准文件：' + basis.fileName)
      }
      if (basis.versionNo) {
        parts.push('版本：' + basis.versionNo)
      }
      if (basis.pageNo) {
        parts.push('页码：第' + basis.pageNo + '页')
      }
      if (basis.sectionTitle) {
        parts.push('章节：' + basis.sectionTitle)
      }
      if (basis.ruleCode) {
        parts.push('条款/规则：' + basis.ruleCode)
      }
      return parts
    },
    safeFileName(value) {
      return String(value || '未命名任务').replace(/[\\/:*?"<>|\s]+/g, '_')
    },
    submitDecision(reviewStatus) {
      const aiTaskId = this.detail.aiTaskId || this.currentAiTaskId
      if (!aiTaskId) {
        this.$message.warning('AI任务参数缺失，无法提交审核')
        return
      }
      const actionTextMap = {
        approved: '审核通过',
        pending: '设为待修改',
        returned: '驳回'
      }
      const actionText = actionTextMap[reviewStatus] || '提交审核'
      if (reviewStatus === 'returned') {
        reviewAiTask({
          aiTaskId,
          reviewStatus,
          reviewOpinion: this.reviewForm.reviewOpinion
        }).then(() => {
          this.$modal.msgSuccess(actionText + '成功')
          this.reviewDecisionSubmittedInCurrentView = true
          this.getDetail()
        }).catch(() => {})
        return
      }
      this.$modal.confirm('是否确认' + actionText + '当前任务？').then(() => {
        return reviewAiTask({
          aiTaskId,
          reviewStatus,
          reviewOpinion: this.reviewForm.reviewOpinion
        })
      }).then(() => {
        this.$modal.msgSuccess(actionText + '成功')
        this.reviewDecisionSubmittedInCurrentView = true
        this.getDetail()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped lang="scss">
.audit-ai-detail-page {
  background: #f4f6f8;
  padding-top: 10px;
}

.head-card,
.section-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 2px;
}

.head-card {
  padding: 14px 16px;
  margin-bottom: 12px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.back-icon {
  color: #409eff;
  font-size: 18px;
  cursor: pointer;
}

.review-detail-blocks {
  .info-table {
    th {
      width: 12%;
    }

    td {
      width: 38%;
    }
  }
}

.status-chip {
  display: inline-flex;
  align-items: center;
  padding: 7px 14px;
  border-radius: 4px;
  font-size: 13px;
  line-height: 1;
}

.status-chip.warning {
  color: #e6a23c;
  background: #fef4e8;
  border: 1px solid #fbe7c6;
}

.status-chip.success {
  color: #67c23a;
  background: #edf9e8;
  border: 1px solid #d8f0ca;
}

.priority-text {
  font-weight: 600;
}

.priority-high {
  color: #409eff;
}

.priority-medium {
  color: #41c3d9;
}

.priority-low {
  color: #ffb44c;
}

.detail-grid {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.preview-column {
  width: 39%;
  min-width: 520px;
  flex-shrink: 0;
}

.side-column {
  flex: 1;
  min-width: 0;
}

.section-card {
  padding: 14px 16px;
  margin-bottom: 12px;
}

.section-title {
  color: #303133;
  font-size: 16px;
  line-height: 1;
  font-weight: 600;
  margin-bottom: 14px;
}

.result-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  line-height: 28px;
}

.report-shell {
  height: calc(100vh - 230px);
  min-height: 680px;
  background: #f8fafc;
  border: 1px solid #ebeef5;
}

.preview-frame {
  width: 100%;
  height: 100%;
  display: block;
  border: 0;
}

.preview-empty {
  height: 100%;
  min-height: 260px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #909399;
  font-size: 14px;
  text-align: center;
  padding: 24px;
}

.preview-empty i {
  font-size: 42px;
  color: #c0c4cc;
}

.preview-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
  padding-bottom: 10px;
}

@media (min-width: 1481px) {
  .preview-column {
    align-self: stretch;
  }

  .preview-column .section-card {
    position: sticky;
    top: 14px;
  }
}

.info-table {
  width: 100%;
  border-collapse: collapse;

  th,
  td {
    border: 1px solid #ebeef5;
    color: #606266;
    font-size: 14px;
    padding: 16px 14px;
  }

  th {
    width: 20%;
    background: #fafbfc;
    color: #303133;
    font-weight: 600;
  }

  td {
    width: 30%;
  }
}

.stage-section {
  position: relative;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 22px;
  padding-top: 24px;
}

.stage-line {
  position: absolute;
  left: 12.5%;
  right: 12.5%;
  top: 40px;
  border-top: 2px solid #dcdfe6;
}

.stage-item {
  position: relative;
  z-index: 1;
}

.stage-node {
  width: 34px;
  height: 34px;
  margin: 0 auto 12px;
  border-radius: 50%;
  border: 2px solid #c0c4cc;
  background: #fff;
  color: #909399;
  font-size: 16px;
  line-height: 30px;
  text-align: center;
  font-weight: 600;
}

.stage-node.stage-done {
  border-color: #67c23a;
  color: #67c23a;
}

.stage-node.stage-running {
  border-color: #409eff;
  color: #409eff;
}

.stage-node.stage-waiting {
  border-color: #909399;
  color: #909399;
}

.stage-node.stage-paused {
  border-color: #e6a23c;
  color: #e6a23c;
}

.stage-node.stage-unknown {
  border-color: #e6a23c;
  color: #e6a23c;
}

.stage-node.stage-failed {
  border-color: #f56c6c;
  color: #f56c6c;
}

.stage-node.stage-pending {
  border-color: #c0c4cc;
  color: #c0c4cc;
}

.stage-name-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.stage-name {
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.stage-status-icon,
.stage-status-text,
.stage-log-empty,
.stage-log-btn {
  font-size: 13px;
}

.stage-status-done {
  color: #67c23a;
}

.stage-status-running {
  color: #409eff;
}

.stage-status-waiting {
  color: #909399;
}

.stage-status-paused {
  color: #e6a23c;
}

.stage-status-unknown {
  color: #e6a23c;
}

.stage-status-failed {
  color: #f56c6c;
}

.stage-status-pending,
.stage-log-empty {
  color: #c0c4cc;
}

.stage-time {
  margin: 10px 0 12px;
  color: #909399;
  font-size: 13px;
  text-align: center;
}

.stage-card {
  background: #f7f9fb;
  border: 1px solid #edf1f6;
  padding: 14px 14px 12px;
  min-height: 136px;
}

.stage-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
}

.stage-card-line {
  color: #606266;
  font-size: 12px;
  line-height: 1.9;
}

.stage-log-dialog {
  .stage-log-title {
    color: #303133;
    font-size: 15px;
    font-weight: 600;
    margin-bottom: 12px;
  }

  .stage-log-block {
    border: 1px solid #ebeef5;
    background: #f7f9fb;
    padding: 10px 12px;
    margin-bottom: 10px;
  }

  .stage-log-block.error {
    border-color: #fde2e2;
    background: #fff6f6;
  }

  .stage-log-label {
    color: #606266;
    font-size: 13px;
    font-weight: 600;
    margin-bottom: 8px;
  }

  pre {
    margin: 0;
    white-space: pre-wrap;
    word-break: break-word;
    color: #303133;
    font-size: 12px;
    line-height: 1.7;
    font-family: Consolas, Monaco, monospace;
  }
}

.issue-card {
  border: 1px solid #ebeef5;
  padding: 14px 16px 12px;
}

.issue-card-clickable {
  cursor: pointer;
  outline: none;
  transition: border-color .2s, box-shadow .2s, background-color .2s;
}

.issue-card-clickable:hover,
.issue-card-clickable:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, .12);
}

.issue-card-active {
  border-color: #409eff;
  background: #f5faff;
}

.issue-card + .issue-card {
  margin-top: 10px;
}

.issue-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #f56c6c;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
}

.issue-title span:first-of-type {
  flex: 1;
  min-width: 0;
}

.issue-page-tag {
  flex: none;
  color: #409eff;
  background: #ecf5ff;
  border: 1px solid #d9ecff;
  border-radius: 3px;
  padding: 2px 6px;
  font-size: 12px;
  font-weight: 400;
}

.issue-title.format {
  color: #f56c6c;
}

.issue-line {
  color: #606266;
  font-size: 13px;
  line-height: 1.9;
}

.issue-quote {
  margin-top: 10px;
  padding: 8px 10px;
  border-left: 3px solid #409eff;
  background: #f5faff;
}

.issue-quote-label {
  margin-bottom: 4px;
  color: #409eff;
  font-size: 12px;
  font-weight: 600;
}

.issue-quote-text {
  color: #303133;
  font-size: 13px;
  line-height: 1.8;
  word-break: break-word;
}

.issue-basis {
  margin-top: 10px;
}

.issue-basis ::v-deep .el-collapse {
  border-top: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
}

.issue-basis ::v-deep .el-collapse-item__header {
  height: 36px;
  line-height: 36px;
  color: #606266;
  font-size: 13px;
  font-weight: 600;
}

.issue-basis ::v-deep .el-collapse-item__content {
  padding-bottom: 8px;
}

.issue-basis-item {
  padding: 10px 0;
  border-top: 1px dashed #ebeef5;
}

.issue-basis-item:first-child {
  border-top: 0;
}

.issue-basis-title {
  color: #303133;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 6px;
}

.issue-basis-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 6px;
}

.issue-basis-meta span {
  color: #606266;
  background: #f7f9fb;
  border: 1px solid #edf1f6;
  border-radius: 3px;
  padding: 2px 6px;
  font-size: 12px;
  line-height: 1.5;
}

.issue-basis-quote {
  color: #303133;
  background: #fafbfc;
  border-left: 3px solid #67c23a;
  padding: 8px 10px;
  font-size: 13px;
  line-height: 1.8;
  word-break: break-word;
}

.issue-basis-conflict {
  margin-top: 8px;
  color: #f56c6c;
  background: #fef0f0;
  border-left: 3px solid #f56c6c;
  padding: 8px 10px;
  font-size: 13px;
  line-height: 1.8;
  word-break: break-word;
}

.issue-basis-conflict-label {
  font-weight: 600;
}

.result-empty {
  padding: 28px 0;
  color: #909399;
  font-size: 14px;
  text-align: center;
  border: 1px dashed #dcdfe6;
  background: #fafbfc;
}

.decision-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.section-card ::v-deep .el-textarea__inner {
  min-height: 168px !important;
  border-radius: 4px;
  resize: none;
}

@media (max-width: 1480px) {
  .stage-section {
    grid-template-columns: 1fr;
    gap: 14px;
    padding-top: 0;
  }

  .stage-line {
    display: none;
  }

  .detail-grid {
    flex-direction: column;
  }

  .preview-column,
  .side-column {
    width: 100%;
    min-width: 0;
  }

  .report-shell {
    height: 72vh;
    min-height: 520px;
  }
}
</style>
