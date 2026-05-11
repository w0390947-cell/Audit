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
          <div v-for="(item, index) in aiFlowStageList" :key="item.stageCode || index" class="stage-item">
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
                <span class="stage-log-empty">暂无日志</span>
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
          <div class="section-title">检测结果</div>
          <div v-if="displayIssueList.length">
            <div v-for="(item, index) in displayIssueList" :key="item.issueId || index" class="issue-card">
              <div class="issue-title" :class="{ 'format': index > 0 }">
                <i class="el-icon-warning" />
                <span>{{ item.issueTitle || '识别异常类型' }}</span>
              </div>
              <div v-for="(line, lineIndex) in issueLines(item)" :key="'issue_' + index + '_' + lineIndex" class="issue-line">
                {{ line }}
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
          <div class="decision-actions">
            <el-button type="primary" size="small" @click="submitDecision('approved')" v-hasPermi="['audit:ai:review']">审核通过</el-button>
            <el-button size="small" @click="submitDecision('pending')" v-hasPermi="['audit:ai:review']">待修改</el-button>
          </div>
        </div>
      </div>
    </div>
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
        findingList: []
      },
      reviewDetail: {
        stageList: [],
        issueList: [],
        versionList: []
      },
      reviewForm: {
        reviewOpinion: ''
      }
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
      return this.toAbsoluteUrl(this.previewInfo.previewFileUrl)
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
      return [
        this.buildQueueStage(),
        this.buildAnalysisStage(),
        this.buildResultStage(),
        this.buildReviewStage()
      ]
    },
    displayIssueList() {
      if (Array.isArray(this.detail.findingList) && this.detail.findingList.length) {
        return this.detail.findingList.map(item => ({
          issueId: item.findingId,
          issueType: item.findingType,
          issueTitle: this.formatFindingTitle(item),
          issueContent: item.findingContent
        }))
      }
      if (Array.isArray(this.reviewDetail.issueList) && this.reviewDetail.issueList.length) {
        return this.reviewDetail.issueList
      }
      return []
    }
  },
  created() {
    this.ensureRouteAndFetch()
  },
  activated() {
    this.ensureRouteAndFetch()
  },
  watch: {
    '$route.params.aiTaskId'() {
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
    buildQueueStage() {
      const done = !!(this.detail.aiTaskId || this.detail.taskNo)
      return {
        stageCode: 'queued',
        stageName: '任务入队',
        status: done ? 'done' : 'pending',
        statusText: done ? '已完成' : '未开始',
        timeText: this.parseTime(this.detail.submitTime, '{y}-{m}-{d} {h}:{i}:{s}') || '--',
        summary: done ? 'AI任务已创建并进入处理队列' : '等待创建AI任务',
        lines: [
          '提交人：' + this.displayValue(this.detail.submitter),
          '优先级：' + (this.detail.priority ? this.priorityLabel(this.detail.priority) : '--'),
          '任务状态：' + this.taskStatusLabel(this.detail.taskStatus)
        ]
      }
    },
    buildAnalysisStage() {
      const taskStatus = this.detail.taskStatus
      const stageStatus = this.analysisStageStatus(taskStatus)
      const percent = this.detail.progressPercent === null || this.detail.progressPercent === undefined
        ? 0
        : this.detail.progressPercent
      return {
        stageCode: 'analysis',
        stageName: 'AI分析执行',
        status: stageStatus,
        statusText: this.flowStatusLabel(stageStatus),
        timeText: this.displayValue(this.detail.estimatedDuration),
        summary: this.detail.progressText || this.analysisStageSummary(taskStatus),
        lines: [
          '执行进度：' + percent + '%',
          '执行说明：' + this.displayValue(this.detail.progressText),
          '预计执行时间：' + this.displayValue(this.detail.estimatedDuration)
        ]
      }
    },
    buildResultStage() {
      const findingCount = Array.isArray(this.detail.findingList) ? this.detail.findingList.length : 0
      const taskStatus = this.detail.taskStatus
      const stageStatus = this.resultStageStatus(taskStatus, findingCount)
      return {
        stageCode: 'result',
        stageName: '结果生成',
        status: stageStatus,
        statusText: this.flowStatusLabel(stageStatus),
        timeText: this.parseTime(this.detail.updateTime, '{y}-{m}-{d} {h}:{i}:{s}') || '--',
        summary: this.resultStageSummary(taskStatus, findingCount),
        lines: [
          '检测结果数量：' + findingCount,
          'AI摘要：' + this.displayValue(this.detail.aiSummary)
        ]
      }
    },
    buildReviewStage() {
      const stageStatus = this.reviewStageStatus(this.detail.reviewStatus)
      return {
        stageCode: 'review',
        stageName: '人工复核',
        status: stageStatus,
        statusText: this.reviewStatusLabel(this.detail.reviewStatus),
        timeText: this.parseTime(this.detail.updateTime, '{y}-{m}-{d} {h}:{i}:{s}') || '--',
        summary: this.reviewStageSummary(this.detail.reviewStatus),
        lines: [
          '复核人：' + this.displayValue(this.detail.reviewer),
          '复核意见：' + this.displayValue(this.detail.reviewOpinion)
        ]
      }
    },
    analysisStageStatus(taskStatus) {
      if (taskStatus === 'completed') {
        return 'done'
      }
      if (taskStatus === 'executing') {
        return 'running'
      }
      if (taskStatus === 'waiting') {
        return 'waiting'
      }
      if (taskStatus === 'paused') {
        return 'paused'
      }
      if (taskStatus === 'failed') {
        return 'failed'
      }
      return 'pending'
    },
    resultStageStatus(taskStatus, findingCount) {
      if (findingCount > 0 || taskStatus === 'completed') {
        return 'done'
      }
      if (taskStatus === 'failed') {
        return 'failed'
      }
      if (taskStatus === 'executing') {
        return 'waiting'
      }
      return 'pending'
    },
    reviewStageStatus(reviewStatus) {
      if (reviewStatus === 'approved') {
        return 'done'
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
    analysisStageSummary(taskStatus) {
      const map = {
        waiting: 'AI任务正在等待调度',
        executing: 'AI任务正在执行分析',
        paused: 'AI任务已暂停',
        completed: 'AI分析已完成',
        failed: 'AI分析执行失败'
      }
      return map[taskStatus] || 'AI分析尚未开始'
    },
    resultStageSummary(taskStatus, findingCount) {
      if (findingCount > 0) {
        return '已生成' + findingCount + '条检测结果'
      }
      if (taskStatus === 'completed') {
        return 'AI分析已完成，未发现异常或暂无结构化问题'
      }
      if (taskStatus === 'failed') {
        return 'AI分析失败，未生成检测结果'
      }
      if (taskStatus === 'executing') {
        return '等待AI分析完成后生成结果'
      }
      return '检测结果尚未生成'
    },
    reviewStatusLabel(reviewStatus) {
      if (reviewStatus === 'approved') {
        return '已通过'
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
      this.reviewDetail = {
        stageList: [],
        issueList: [],
        versionList: []
      }
      getAiTask(this.currentAiTaskId).then(response => {
        this.detail = response.data || { findingList: [] }
        this.reviewForm.reviewOpinion = this.detail.reviewOpinion || ''
        this.getReviewDetail().then(() => {
          this.loading = false
        })
        this.getPreview()
      }).catch(() => {
        this.loading = false
      })
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
    formatFindingTitle(item) {
      const type = item.findingType || '其他'
      const title = item.findingTitle || 'AI发现问题'
      return '识别异常类型：' + type + ' - ' + title
    },
    issueLines(item) {
      const lines = this.splitStructuredText(item.issueContent)
      if (lines.length) {
        return lines
      }
      return ['暂无问题描述']
    },
    submitDecision(reviewStatus) {
      const aiTaskId = this.detail.aiTaskId || this.currentAiTaskId
      if (!aiTaskId) {
        this.$message.warning('AI任务参数缺失，无法提交审核')
        return
      }
      const actionText = reviewStatus === 'approved' ? '审核通过' : '设为待修改'
      this.$modal.confirm('是否确认' + actionText + '当前任务？').then(() => {
        return reviewAiTask({
          aiTaskId,
          reviewStatus,
          reviewOpinion: this.reviewForm.reviewOpinion
        })
      }).then(() => {
        this.$modal.msgSuccess(actionText + '成功')
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
.stage-log-empty {
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

.issue-card {
  border: 1px solid #ebeef5;
  padding: 14px 16px 12px;
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

.issue-title.format {
  color: #f56c6c;
}

.issue-line {
  color: #606266;
  font-size: 13px;
  line-height: 1.9;
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
