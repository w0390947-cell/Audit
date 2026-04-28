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
          <div class="report-shell">
            <div class="report-paper">
              <div class="report-title">煤科院煤炭产品质量检测报告</div>
              <div class="report-meta">报告编号：{{ detail.taskNo || 'MKY-JC-20260129001' }}</div>
              <div class="report-meta">检测单位：XX 煤炭科学研究院有限公司 检测中心</div>
              <div class="report-meta">委托单位：{{ detail.deliveryUnit || 'XX 煤炭开采有限公司' }}</div>

              <div class="report-tip blue-tip">内容缺失：委托单位联系人及联系方式未填写</div>

              <div class="report-heading">一、检测概况</div>
              <p class="report-text">1.1 检测目的：受委托单位委托，对其送检的商品煤样品进行工业分析、全硫、发热量等指标检测，验证样品质量是否符合 GB/T 5751-2014 及委托方约定标准。</p>
              <p class="report-text">1.2 检测样品：送检样品为烟煤，样品数量 2kg，样品编号 YM-20260129001，采集日期 2026 年 01 月 27 日，送检日期 2026 年 01 月 28 日。</p>

              <div class="report-tip blue-tip">内容缺失：样品采集地点、采样方式未填写</div>

              <p class="report-text">1.3 检测依据：GB/T 212-2008《煤的工业分析方法》、GB/T 213-2021《煤的发热量测定方法》、GB/T 214-2007《煤中全硫的测定方法》。</p>
              <p class="report-text">1.4 检测仪器：电子天平（精度 0.1mg）、马弗炉、量热仪、定硫仪，所有仪器均在检定有效期内，检定证书编号详见附件。</p>

              <div class="report-heading">二、检测项目及结果</div>
              <div class="report-tip yellow-tip">格式错误：检测项目及结果未按规范设置表格标题，表格缺少“检测结果判定”</div>

              <table class="report-table">
                <thead>
                  <tr>
                    <th>检测项目</th>
                    <th>单位</th>
                    <th>检测结果</th>
                    <th>标准要求（委托方约定+国标）</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>水分（Mad）</td>
                    <td>%</td>
                    <td>8.7</td>
                    <td>≤8.0</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="preview-actions">
            <el-button type="primary" size="small" @click="changePage(-1)" :disabled="currentPage <= 1">上一页</el-button>
            <el-button type="primary" size="small" @click="changePage(1)">下一页</el-button>
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
import { getAiTask, reviewAiTask } from '@/api/audit/ai'

export default {
  name: 'AuditAiDetail',
  dicts: ['audit_review_status'],
  data() {
    return {
      loading: true,
      currentPage: 1,
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
      this.currentPage = 1
      getAiTask(this.currentAiTaskId).then(response => {
        this.detail = response.data || { findingList: [] }
        this.reviewForm.reviewOpinion = this.detail.reviewOpinion || ''
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    goBack() {
      this.$router.push('/audit-ai/queue')
    },
    changePage(step) {
      const nextPage = this.currentPage + step
      if (nextPage < 1) {
        return
      }
      this.currentPage = nextPage
    },
    downloadReport() {
      if (!this.detail.reportFileUrl) {
        this.$message.warning('当前任务未绑定报告文件')
        return
      }
      window.open(encodeURI(this.detail.reportFileUrl))
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
  padding: 8px 0 0;
}

.report-paper {
  width: 72%;
  min-width: 420px;
  margin: 0 auto;
  background: #fff;
  min-height: 860px;
}

.report-title {
  color: #1f2d3d;
  font-size: 18px;
  font-weight: 700;
  text-align: center;
  margin: 54px 0 30px;
}

.report-meta {
  color: #444;
  font-size: 12px;
  line-height: 2.1;
}

.report-heading {
  color: #303133;
  font-size: 16px;
  font-weight: 700;
  margin: 16px 0 8px;
  text-align: center;
}

.report-text {
  color: #4c5564;
  font-size: 12px;
  line-height: 2.1;
  margin: 0;
}

.report-tip {
  margin: 12px 0;
  padding: 9px 12px;
  border-radius: 2px;
  font-size: 12px;
  line-height: 1.5;
}

.blue-tip {
  color: #67758a;
  background: #dfeaf8;
  border: 1px solid #d5e3f6;
}

.yellow-tip {
  color: #b8a54b;
  background: #fff7cf;
  border: 1px solid #faefad;
}

.report-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;

  th,
  td {
    border: 1px solid #e8edf3;
    text-align: center;
    color: #4c5564;
    font-size: 12px;
    padding: 8px 6px;
  }

  th {
    background: #fafbfd;
    color: #384150;
    font-weight: 600;
  }
}

.preview-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 18px;
  padding: 0 46px 10px;
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

  .report-paper {
    width: 100%;
    min-width: 0;
  }
}
</style>
