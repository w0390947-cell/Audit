<template>
  <div class="app-container review-detail-page" v-loading="loading">
    <div class="head-card">
      <div class="page-title">
        <i class="el-icon-back back-icon" @click="goBack" />
        <span>详情</span>
      </div>
      <div v-if="currentTaskId" class="page-actions">
        <span class="version-label">当前版本：{{ detail.currentVersionNo || 'v2.0' }}</span>
        <el-button type="primary" size="small" @click="historyOpen = true">查看历史版本</el-button>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title">审核基础信息</div>
      <table class="info-table">
        <tbody>
          <tr>
            <th>任务编号</th>
            <td>{{ detail.taskNo || 'SF-16542598454' }}</td>
            <th>产品名称</th>
            <td>{{ detail.productName || '产品名称1' }}</td>
          </tr>
          <tr>
            <th>送检单位</th>
            <td>{{ detail.deliveryUnit || '送检单位1' }}</td>
            <th>上传时间</th>
            <td>{{ parseTime(detail.submitTime, '{y}-{m}-{d} {h}:{i}:{s}') || '2025-12-28 06:28:34' }}</td>
          </tr>
          <tr>
            <th>AI分析次数</th>
            <td>{{ detail.aiAnalysisCount || 3 }}</td>
            <th>任务状态</th>
            <td>
              <dict-tag v-if="detail.taskStatus" :options="dict.type.audit_review_task_status" :value="detail.taskStatus" />
              <span v-else class="status-chip warning">已上传</span>
            </td>
          </tr>
          <tr>
            <th>发起人</th>
            <td>{{ detail.sponsor || '发起人1' }}</td>
            <th>审核进度</th>
            <td>
              <dict-tag v-if="detail.reviewStatus" :options="dict.type.audit_review_status" :value="detail.reviewStatus" />
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

    <div class="section-card">
      <div class="section-title">检测结果</div>
      <div>
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
    </div>

    <el-dialog title="历史版本" :visible.sync="historyOpen" width="760px" append-to-body class="history-dialog">
      <div class="history-list">
        <div v-for="(item, versionIndex) in displayVersionList" :key="item.versionId || versionIndex" class="history-card">
          <div class="history-head">
            <div>
              <span class="history-version">{{ item.versionNo }}版本</span>
              <span v-if="item.currentFlag === '1' || versionIndex === 0" class="current-tag">当前版本</span>
            </div>
          </div>
          <div class="history-file-row">
            <div class="history-file-main">
              <i class="el-icon-document file-icon" />
              <span class="file-name">{{ item.reportFileName || '--' }}</span>
            </div>
            <span class="history-time">上传时间：{{ parseTime(item.submitTime, '{y}-{m}-{d} {h}:{i}:{s}') || '--' }}</span>
          </div>
          <div class="history-footer">
            <span>检测状态：{{ historyDetectText(item) }}</span>
            <el-button type="text" @click="switchVersion(item)">点击跳转至该版本详情</el-button>
          </div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="historyOpen = false">取消</el-button>
        <el-button type="primary" @click="historyOpen = false">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getReview } from '@/api/audit/review'

export default {
  name: 'AuditReviewDetail',
  dicts: ['audit_review_task_status', 'audit_review_status'],
  data() {
    return {
      loading: true,
      historyOpen: false,
      detail: {
        stageList: [],
        issueList: [],
        versionList: []
      }
    }
  },
  computed: {
    currentTaskId() {
      const taskId = Number(this.$route.params.taskId)
      return Number.isFinite(taskId) && taskId > 0 ? taskId : null
    },
    displayStageList() {
      if (Array.isArray(this.detail.stageList) && this.detail.stageList.length) {
        return this.detail.stageList
      }
      return this.defaultStageList()
    },
    displayIssueList() {
      if (Array.isArray(this.detail.issueList) && this.detail.issueList.length) {
        return this.detail.issueList
      }
      return this.defaultIssueList()
    },
    displayVersionList() {
      if (Array.isArray(this.detail.versionList) && this.detail.versionList.length) {
        return this.detail.versionList
      }
      return this.defaultVersionList()
    }
  },
  created() {
    this.ensureRouteAndFetch()
  },
  activated() {
    this.ensureRouteAndFetch()
  },
  watch: {
    '$route.params.taskId'() {
      this.ensureRouteAndFetch()
    },
    '$route.query.versionId'() {
      this.ensureRouteAndFetch()
    }
  },
  methods: {
    ensureRouteAndFetch() {
      if (!this.currentTaskId) {
        this.loading = false
        return
      }
      this.getDetail()
    },
    getDetail() {
      this.loading = true
      getReview(this.currentTaskId, this.$route.query.versionId).then(response => {
        this.detail = response.data || {
          stageList: [],
          issueList: [],
          versionList: []
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    goBack() {
      this.$router.push('/audit/review')
    },
    switchVersion(row) {
      if (!row.versionId) {
        this.$message.warning('当前版本暂无可切换的版本标识')
        return
      }
      this.historyOpen = false
      this.$router.replace({
        path: '/audit/review/detail/' + this.currentTaskId,
        query: { versionId: row.versionId }
      })
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
    defaultIssueList() {
      return [
        {
          issueType: '数据错误',
          issueTitle: '识别异常类型：数据错误'
        },
        {
          issueType: '格式不规范',
          issueTitle: '识别异常类型：格式不规范'
        }
      ]
    },
    defaultVersionList() {
      return [
        {
          versionNo: 'v2.0',
          currentFlag: '1',
          reportFileName: '“防爆电机检验报告 V2.0.pdf”',
          submitTime: '2025-12-28 06:28:34',
          detectStatus: 'parsed'
        },
        {
          versionNo: 'v1.0',
          currentFlag: '0',
          reportFileName: '“防爆电机检验报告 V1.0.pdf”',
          submitTime: '2025-12-28 06:28:34',
          detectStatus: 'parsed'
        }
      ]
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
      if (lines.length > 1) {
        return lines
      }
      if (item.issueType === '数据错误' || (item.issueTitle || '').indexOf('数据错误') > -1) {
        return [
          '① ' + (item.issueContent || '报告第 3 页表 3-1 中“防爆等级”填写为 “Exd II BT4”，与依据文件要求不一致'),
          '② 报告第 7 页“额定电流”填写为“50A”，与辅助材料（检测记录）中记录的“60A”存在偏差'
        ]
      }
      return [
        '① ' + (item.issueContent || '报告第 5 页“检测日期”格式为 “2024/06/01”，未按审核模板要求的 “YYYY-MM-DD” 标准格式填写'),
        '② 报告第 10 页“检测人员签字”栏位未填写，且缺少审核人员签字确认'
      ]
    },
    historyDetectText(item) {
      if (item.detectStatus) {
        return this.selectDictLabel(this.dict.type.audit_review_task_status, item.detectStatus) || '已解析'
      }
      return '已解析'
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
    }
  }
}
</script>

<style scoped lang="scss">
.review-detail-page {
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
  display: flex;
  align-items: center;
  justify-content: space-between;
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

.page-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.version-label {
  color: #606266;
  font-size: 14px;
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

.info-table {
  width: 100%;
  border-collapse: collapse;

  th,
  td {
    border: 1px solid #ebeef5;
    padding: 16px 14px;
    color: #606266;
    font-size: 14px;
  }

  th {
    width: 12%;
    background: #fafbfc;
    color: #303133;
    font-weight: 600;
  }

  td {
    width: 38%;
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

.history-list {
  max-height: 520px;
  overflow-y: auto;
  padding-right: 2px;
}

.history-card {
  border: 1px dashed #dfe6ec;
  border-radius: 4px;
  padding: 14px;
}

.history-card + .history-card {
  margin-top: 12px;
}

.history-head {
  margin-bottom: 10px;
}

.history-version {
  color: #303133;
  font-size: 15px;
  font-weight: 600;
  margin-right: 8px;
}

.current-tag {
  color: #409eff;
  font-size: 12px;
}

.history-file-row {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.history-file-main {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex: 1;
}

.file-icon {
  color: #409eff;
  font-size: 18px;
}

.file-name {
  color: #303133;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-time {
  color: #909399;
  font-size: 12px;
  white-space: nowrap;
}

.history-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #606266;
  font-size: 13px;
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
}
</style>
