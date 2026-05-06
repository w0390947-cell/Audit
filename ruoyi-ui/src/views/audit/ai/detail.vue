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
        <div class="section-title">审核基础信息</div>
        <table class="info-table">
          <tbody>
            <tr>
              <th>任务编号</th>
              <td>{{ reviewDetail.taskNo || 'SF-16542598454' }}</td>
              <th>产品名称</th>
              <td>{{ reviewDetail.productName || '产品名称1' }}</td>
            </tr>
            <tr>
              <th>送检单位</th>
              <td>{{ reviewDetail.deliveryUnit || '送检单位1' }}</td>
              <th>上传时间</th>
              <td>{{ parseTime(reviewDetail.submitTime, '{y}-{m}-{d} {h}:{i}:{s}') || '2025-12-28 06:28:34' }}</td>
            </tr>
            <tr>
              <th>AI分析次数</th>
              <td>{{ reviewDetail.aiAnalysisCount || 3 }}</td>
              <th>任务状态</th>
              <td>
                <dict-tag v-if="reviewDetail.taskStatus" :options="dict.type.audit_review_task_status" :value="reviewDetail.taskStatus" />
                <span v-else class="status-chip warning">已上传</span>
              </td>
            </tr>
            <tr>
              <th>发起人</th>
              <td>{{ reviewDetail.sponsor || '发起人1' }}</td>
              <th>审核进度</th>
              <td>
                <dict-tag v-if="reviewDetail.reviewStatus" :options="dict.type.audit_review_status" :value="reviewDetail.reviewStatus" />
                <span v-else class="status-chip success">审核通过</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="section-card">
        <div class="section-title">流转状态</div>
        <div class="stage-section">
          <div class="stage-line" />
          <div v-for="(item, index) in displayStageList" :key="item.stageId || index" class="stage-item">
            <div class="stage-node">{{ index + 1 }}</div>
            <div class="stage-name-row">
              <span class="stage-name">{{ item.stageName || defaultStageName(index, item) }}</span>
              <i class="el-icon-success stage-status-icon" />
              <span class="stage-status-text">已完成</span>
            </div>
            <div class="stage-time">{{ stageTimeText(item, index) }}</div>
            <div class="stage-card">
              <div class="stage-card-head">
                <span>{{ item.stageSummary || defaultStageSummary(item) }}</span>
                <el-button type="text" class="stage-log-btn">处理日志</el-button>
              </div>
              <div v-for="(line, lineIndex) in stageLines(item)" :key="'stage_' + index + '_' + lineIndex" class="stage-card-line">
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
  dicts: ['audit_review_task_status', 'audit_review_status'],
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
    displayStageList() {
      if (Array.isArray(this.reviewDetail.stageList) && this.reviewDetail.stageList.length) {
        return this.reviewDetail.stageList
      }
      return this.defaultStageList()
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
    defaultStageList() {
      return [
        {
          stageCode: 'upload',
          stageName: '报告上传',
          stageTime: '2024-06-01 14:35:00'
        },
        {
          stageCode: 'parse',
          stageName: '报告解析',
          stageTime: '2024-06-01 14:37:00'
        },
        {
          stageCode: 'detect',
          stageName: '报告检测',
          stageTime: '2024-06-01 14:39:00'
        }
      ]
    },
    formatFindingTitle(item) {
      const type = item.findingType || '其他'
      const title = item.findingTitle || 'AI发现问题'
      return '识别异常类型：' + type + ' - ' + title
    },
    defaultStageName(index, item) {
      if (item && item.stageCode === 'upload') {
        return '报告上传'
      }
      if (item && item.stageCode === 'parse') {
        return '报告解析'
      }
      if (item && item.stageCode === 'detect') {
        return '报告检测'
      }
      return ['报告上传', '报告解析', '报告检测'][index] || '--'
    },
    defaultStageSummary(item) {
      const stageCode = item && item.stageCode
      if (stageCode === 'upload') {
        return '对应智能体-文件校验智能体'
      }
      if (stageCode === 'parse') {
        return '对应智能体-预处理智能体'
      }
      return '对应智能体：比对智能体 + 检测结果智能体'
    },
    stageLines(item) {
      const lines = this.splitStructuredText(item.stageDetail)
      if (lines.length > 1) {
        return lines
      }
      if (item.stageCode === 'upload') {
        return [
          '① 格式校验：检测文件为 PDF，符合要求',
          '② 大小校验：文件大小 2.3MB，<5MB 限制',
          '③ 存储校验：已成功存入 MinIO，路径：xxx'
        ]
      }
      if (item.stageCode === 'parse') {
        return [
          '① 格式转换：已将 PDF 转为 JSON 结构化格式',
          '② 字段提取：提取 12 个核心字段（防爆等级、检测日期等）',
          '③ 摘要生成：已生成报告摘要，字数 120 字'
        ]
      }
      return [
        '① 比对智能体：已完成报告与依据文件的 10 个字段比对，发现 2 处不一致',
        '② 检测结果智能体：“识别异常类型：数据错误 + 格式不规范，生成 4 条整改建议”'
      ]
    },
    issueLines(item) {
      const lines = this.splitStructuredText(item.issueContent)
      if (lines.length) {
        return lines
      }
      return ['暂无问题描述']
    },
    stageTimeText(item, index) {
      const current = this.toDate(item.stageTime)
      if (!current) {
        return '--'
      }
      const nextStage = this.displayStageList[index + 1]
      const next = this.toDate(nextStage && nextStage.stageTime) || new Date(current.getTime() + 60 * 1000)
      return this.parseTime(current, '{y}-{m}-{d} {h}:{i}') + '-' + this.parseTime(next, '{h}:{i}')
    },
    toDate(value) {
      if (!value) {
        return null
      }
      const date = new Date(value)
      return Number.isNaN(date.getTime()) ? null : date
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
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 22px;
  padding-top: 24px;
}

.stage-line {
  position: absolute;
  left: 16%;
  right: 16%;
  top: 40px;
  border-top: 2px solid #4a96e9;
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
  border: 2px solid #4a96e9;
  background: #fff;
  color: #4a96e9;
  font-size: 16px;
  line-height: 30px;
  text-align: center;
  font-weight: 600;
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
.stage-status-text {
  color: #4a96e9;
  font-size: 13px;
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

.stage-log-btn {
  padding: 0;
  font-size: 13px;
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
