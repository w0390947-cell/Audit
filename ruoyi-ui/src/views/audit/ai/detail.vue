<template>
  <div class="app-container audit-ai-detail-page" v-loading="loading">
    <div class="head-card">
      <div class="page-title">
        <i class="el-icon-back back-icon" @click="goBack" />
        <span>详情</span>
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
          <div class="section-title">关联信息区</div>
          <table class="info-table">
            <tbody>
              <tr>
                <th>任务编号</th>
                <td>{{ detail.taskNo || '--' }}</td>
                <th>产品名称</th>
                <td>{{ detail.productName || '--' }}</td>
              </tr>
              <tr>
                <th>送检单位</th>
                <td>{{ detail.deliveryUnit || '--' }}</td>
                <th>提交人</th>
                <td>{{ detail.submitter || '--' }}</td>
              </tr>
              <tr>
                <th>AI分析次数</th>
                <td>{{ detail.aiAnalysisCount || 0 }}</td>
                <th>任务审核状态</th>
                <td>
                  <dict-tag :options="dict.type.audit_review_status" :value="detail.reviewStatus" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="section-card">
          <div class="section-title">报告预览区</div>
          <div class="file-card">
            <div class="file-main">
              <i class="el-icon-document file-icon" />
              <div class="file-text">
                <div class="file-name">{{ detail.reportFileName || '煤科院煤炭产品质量检测报告.pdf' }}</div>
                <div class="file-meta">2.4 MB　创建时间：{{ reportTimeText }}</div>
              </div>
            </div>
            <el-button type="text" class="download-btn" @click="downloadReport">
              <i class="el-icon-upload2" />
              <span>下载</span>
            </el-button>
          </div>
        </div>

        <div class="section-card">
          <div class="section-title">AI关键发现区</div>
          <div v-for="(row, index) in findingRows" :key="'finding_' + index" class="finding-row">
            {{ row }}
          </div>
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

export default {
  name: 'AuditAiDetail',
  dicts: ['audit_review_status'],
  data() {
    return {
      loading: true,
      previewLoading: false,
      previewInfo: null,
      previewError: '',
      detail: {
        findingList: []
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
    reportTimeText() {
      return this.parseTime(this.detail.submitTime, '{y}-{m}-{d}-{h}:{i}') || '2025-06-17-12:28'
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
    findingRows() {
      if (Array.isArray(this.detail.findingList) && this.detail.findingList.length) {
        return this.detail.findingList.map(item => item.findingContent || item.findingTitle || '--')
      }
      if (this.detail.aiSummary) {
        return [this.detail.aiSummary, this.detail.aiSummary]
      }
      return [
        '本次报告经 AI 核查，发现存在内容缺失、格式错误两类问题，具体汇总如下，对应报告中相关标注位置，便于整改完善：',
        '本次报告经 AI 核查，发现存在内容缺失、格式错误两类问题，具体汇总如下，对应报告中相关标注位置，便于整改完善：'
      ]
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
      getAiTask(this.currentAiTaskId).then(response => {
        this.detail = response.data || { findingList: [] }
        this.reviewForm.reviewOpinion = this.detail.reviewOpinion || ''
        this.loading = false
        this.getPreview()
      }).catch(() => {
        this.loading = false
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

.file-card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 12px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.file-main {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  flex: 1;
}

.file-icon {
  color: #409eff;
  font-size: 22px;
}

.file-text {
  min-width: 0;
}

.file-name {
  color: #303133;
  font-size: 14px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-meta {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
}

.download-btn {
  padding: 0;
  color: #409eff;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.finding-row {
  background: #f7f9fb;
  border: 1px solid #f0f2f5;
  color: #606266;
  font-size: 14px;
  line-height: 1.8;
  padding: 14px 16px;
}

.finding-row + .finding-row {
  margin-top: 12px;
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
