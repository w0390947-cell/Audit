<template>
  <div class="app-container audit-asset-detail-page" v-loading="loading">
    <div class="head-card">
      <div class="page-title">
        <i class="el-icon-back back-icon" @click="goBack" />
        <span>详情</span>
      </div>
      <div class="head-actions">
        <el-button type="primary" size="small" icon="el-icon-view" @click="openPreview(detail.reportFileUrl, detail.reportFileName)">报告预览</el-button>
        <el-button type="primary" size="small" icon="el-icon-download" @click="downloadOpen = true">报告下载</el-button>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title">文件基础信息区</div>
      <table class="base-table">
        <tbody>
          <tr>
            <th>任务编号</th>
            <td>{{ detail.taskNo || 'SF-16542' }}</td>
            <th>产品名称</th>
            <td>{{ detail.productName || '产品名称1' }}</td>
          </tr>
          <tr>
            <th>送检单位</th>
            <td>{{ detail.deliveryUnit || '送检单位1' }}</td>
            <th>提交人</th>
            <td>{{ detail.submitter || '提交人1' }}</td>
          </tr>
          <tr>
            <th>AI分析次数</th>
            <td>{{ detail.aiAnalysisCount || 3 }}</td>
            <th>任务审核状态</th>
            <td>
              <dict-tag v-if="detail.reviewStatus" :options="dict.type.audit_asset_review_status" :value="detail.reviewStatus" />
              <span v-else class="status-chip success">审核通过</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="detail-layout">
      <div class="section-card panel-card ai-panel">
        <div class="panel-head">
          <span class="section-title">AI审核区</span>
          <el-button type="primary" size="mini" @click="versionOpen = true">AI历史分析版本</el-button>
        </div>

        <div class="inner-card">
          <div class="sub-title">队列与调度记录</div>
          <div class="version-banner">
            <span>{{ currentVersionLabel }}</span>
            <span>{{ currentWordText }}</span>
          </div>

          <div class="timeline">
            <div v-for="(item, index) in currentSteps" :key="'step_' + index" class="timeline-row">
              <div class="timeline-axis">
                <div class="timeline-index">{{ index + 1 }}</div>
                <div v-if="index < currentSteps.length - 1" class="timeline-line" />
              </div>
              <div class="timeline-content">
                <div class="timeline-head">
                  <span class="timeline-title">{{ item.stepTitle }}</span>
                  <span class="timeline-time">{{ parseTime(item.stepTime, '{y}-{m}-{d} {h}:{i}:{s}') || item.stepTime || '--' }}</span>
                </div>
                <div class="timeline-text">{{ item.stepContent }}</div>
              </div>
            </div>
          </div>
        </div>

        <el-button class="reupload-btn" type="primary" @click="handleReuploadOpen" v-hasPermi="['audit:asset:reupload']">
          报告重新上传
        </el-button>
      </div>

      <div class="section-card panel-card manual-panel">
        <div class="panel-head">
          <span class="section-title">人工审核区</span>
          <el-button type="primary" size="mini" @click="recordOpen = true">修改与重提记录</el-button>
        </div>

        <div class="review-block">
          <div class="review-label">AI生成观点</div>
          <div class="review-box">{{ aiOpinionText }}</div>
        </div>

        <div class="review-user">审核人：{{ detail.reviewer || '审核人1' }}</div>

        <div class="review-block">
          <div class="review-label">最终审核意见</div>
          <div class="review-box final-box">{{ finalOpinionText }}</div>
        </div>
      </div>
    </div>

    <el-dialog title="报告在线预览" :visible.sync="previewOpen" width="960px" append-to-body class="preview-dialog">
      <div class="preview-toolbar">
        <el-button type="primary" size="small" icon="el-icon-download" @click="openFile(previewFileUrl)">报告下载</el-button>
        <span class="preview-file-name">{{ previewFileName }}</span>
      </div>
      <div class="preview-viewer">
        <object
          v-if="previewMode === 'pdf'"
          :data="previewViewerSrc"
          type="application/pdf"
          class="preview-frame"
        >
          <iframe :src="previewViewerSrc" class="preview-frame" frameborder="0" />
        </object>
        <iframe
          v-else-if="previewMode === 'iframe' || previewMode === 'office'"
          :src="previewViewerSrc"
          class="preview-frame"
          frameborder="0"
        />
        <img
          v-else-if="previewMode === 'image'"
          :src="previewViewerSrc"
          class="preview-image"
          alt="preview"
        />
        <div v-else class="preview-empty">
          <i class="el-icon-document" />
          <span>{{ previewEmptyText }}</span>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="previewOpen = false">取消</el-button>
        <el-button type="primary" @click="previewOpen = false">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="报告下载" :visible.sync="downloadOpen" width="420px" append-to-body class="download-dialog">
      <div class="download-form">
        <div class="download-label">下载格式</div>
        <div class="download-options">
          <el-checkbox v-model="downloadForm.pdf">下载PDF</el-checkbox>
          <el-checkbox v-model="downloadForm.word">下载WORD</el-checkbox>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="downloadOpen = false">取消</el-button>
        <el-button type="primary" @click="submitDownload">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="AI历史分析版本" :visible.sync="versionOpen" width="840px" append-to-body class="version-dialog">
      <div class="version-count">AI分析次数 {{ detail.aiAnalysisCount || displayVersionList.length }}次</div>
      <div class="version-list">
        <div v-for="(item, versionIndex) in displayVersionList" :key="'version_' + versionIndex" class="version-card">
          <div class="version-card-head">
            <span class="version-name">{{ item.versionNo || ('版本' + (displayVersionList.length - versionIndex)) }}</span>
            <span class="version-word">{{ item.wordCountText || '单次AI共计审核1w字' }}</span>
          </div>

          <div v-for="(step, stepIndex) in versionStepList(item, versionIndex)" :key="'version_step_' + versionIndex + '_' + stepIndex" class="version-step">
            <div class="version-step-axis">
              <div class="version-step-index">{{ stepIndex + 1 }}</div>
              <div v-if="stepIndex < versionStepList(item, versionIndex).length - 1" class="version-step-line" />
            </div>
            <div class="version-step-body">
              <div class="version-step-head">
                <span class="version-step-title">{{ step.stepTitle }}</span>
                <span class="version-step-time">{{ parseTime(step.stepTime, '{y}-{m}-{d} {h}:{i}:{s}') || step.stepTime || '--' }}</span>
              </div>
              <div class="version-step-text">{{ step.stepContent }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog title="修改与重提记录" :visible.sync="recordOpen" width="860px" append-to-body class="record-dialog">
      <div class="record-list">
        <div v-for="(item, index) in displayRecordList" :key="'record_' + index" class="record-card">
          <div class="record-header">
            <span class="record-index">{{ toCircleNo(index + 1) }}</span>
            <span class="record-version">{{ item.versionNo || '--' }}</span>
          </div>

          <div class="record-frame">
            <div class="record-meta">
              <span>提交人：{{ item.submitter || '提交人1' }}</span>
              <span>提交时间：{{ parseTime(item.submitTime, '{y}-{m}-{d} {h}:{i}:{s}') || item.submitTime || '--' }}</span>
            </div>
            <div class="record-label">提交文件：</div>
            <div class="record-file">
              <div class="record-file-main">
                <i class="el-icon-document file-icon" />
                <span class="record-file-name">“{{ item.fileName || '--' }}”</span>
              </div>
              <el-button type="text" @click="openPreview(item.fileUrl, item.fileName)">文件预览</el-button>
            </div>

            <div class="record-label">修改内容关联：</div>
            <div class="record-images">
              <template v-for="(image, imageIndex) in resolveRecordImages(item)">
                <el-image
                  v-if="!image.placeholder"
                  :key="'record_img_' + index + '_' + imageIndex"
                  :src="image.url"
                  :preview-src-list="splitImages(item.imageUrls)"
                  fit="cover"
                  class="record-image"
                />
                <div v-else :key="'record_ph_' + index + '_' + imageIndex" class="record-image placeholder">
                  <div class="placeholder-paper" />
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="recordOpen = false">取消</el-button>
        <el-button type="primary" @click="recordOpen = false">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="报告重新上传" :visible.sync="reuploadOpen" width="760px" append-to-body class="reupload-dialog">
      <el-form ref="reuploadForm" :model="reuploadForm" :rules="reuploadRules" label-width="100px">
        <el-form-item label="提交人" prop="submitter">
          <el-input v-model="reuploadForm.submitter" placeholder="请输入提交人" />
        </el-form-item>
        <el-form-item label="提交文件" prop="fileUrl">
          <FileUpload v-model="reuploadForm.fileUrl" :limit="1" :file-size="30" />
        </el-form-item>
        <el-form-item label="修改关联图" prop="imageUrls">
          <FileUpload v-model="reuploadForm.imageUrls" :limit="6" :file-size="10" :file-type="['png', 'jpg', 'jpeg']" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="reuploadOpen = false">取消</el-button>
        <el-button type="primary" @click="submitReupload">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import FileUpload from '@/components/FileUpload'
import { getAsset, reuploadAsset } from '@/api/audit/asset'

export default {
  name: 'AuditAssetDetail',
  components: { FileUpload },
  dicts: ['audit_asset_review_status'],
  data() {
    return {
      loading: true,
      previewOpen: false,
      downloadOpen: false,
      versionOpen: false,
      recordOpen: false,
      reuploadOpen: false,
      previewTarget: {
        url: '',
        name: ''
      },
      detail: {
        versionList: [],
        resubmitRecordList: []
      },
      downloadForm: {
        pdf: true,
        word: false
      },
      reuploadForm: {
        assetId: undefined,
        submitter: '',
        fileUrl: '',
        fileName: '',
        imageUrls: ''
      },
      reuploadRules: {
        submitter: [
          { required: true, message: '提交人不能为空', trigger: 'blur' }
        ],
        fileUrl: [
          { required: true, message: '请上传提交文件', trigger: 'change' }
        ]
      }
    }
  },
  computed: {
    currentAssetId() {
      const assetId = Number(this.$route.params.assetId)
      return Number.isFinite(assetId) && assetId > 0 ? assetId : null
    },
    displayVersionList() {
      if (Array.isArray(this.detail.versionList) && this.detail.versionList.length) {
        return this.detail.versionList
      }
      return [
        {
          versionNo: '版本3',
          wordCountText: '单次AI共计审核1w字',
          stepList: this.defaultSteps('pass')
        },
        {
          versionNo: '版本2',
          wordCountText: '单次AI共计审核1w字',
          stepList: this.defaultSteps('modify')
        },
        {
          versionNo: '版本1',
          wordCountText: '单次AI共计审核1w字',
          stepList: this.defaultSteps('modify')
        }
      ]
    },
    currentVersion() {
      return this.detail.currentVersion || this.displayVersionList[0] || {}
    },
    currentSteps() {
      return this.versionStepList(this.currentVersion, 0)
    },
    currentVersionLabel() {
      return this.currentVersion.versionNo || this.detail.currentVersionNo || '版本3'
    },
    currentWordText() {
      return this.currentVersion.wordCountText || '单次AI共计审核1w字'
    },
    displayRecordList() {
      if (Array.isArray(this.detail.resubmitRecordList) && this.detail.resubmitRecordList.length) {
        return this.detail.resubmitRecordList
      }
      return [
        {
          versionNo: 'v1.0版本',
          submitter: '提交人1',
          submitTime: '2025-06-28 15:38:24',
          fileName: '防爆电机检验报告 V1.0.pdf',
          fileUrl: this.detail.reportFileUrl || '',
          imageUrls: ''
        },
        {
          versionNo: 'v2.0版本',
          submitter: '提交人1',
          submitTime: '2025-06-28 15:38:24',
          fileName: '防爆电机检验报告 V2.0.pdf',
          fileUrl: this.detail.reportFileUrl || '',
          imageUrls: ''
        }
      ]
    },
    aiOpinionText() {
      return this.detail.aiOpinion || '本次报告经AI核查，发现在内容缺失、格式错误两类问题，具体汇总如下，对应报告中相关标注位置，便于整改完善：'
    },
    finalOpinionText() {
      return this.detail.finalOpinion || '格式正确，给予审核通过'
    },
    previewFileUrl() {
      return this.normalizeFileUrl(this.previewTarget.url || this.detail.reportFileUrl || '')
    },
    previewFileName() {
      return this.previewTarget.name || this.detail.reportFileName || this.getFileName(this.previewTarget.url || this.detail.reportFileUrl) || '未命名文件'
    },
    previewFileExt() {
      return this.getFileExt(this.previewFileName, this.previewFileUrl)
    },
    previewMode() {
      if (!this.previewFileUrl) {
        return 'empty'
      }
      if (this.previewFileExt === 'pdf') {
        return 'pdf'
      }
      if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(this.previewFileExt)) {
        return 'image'
      }
      if (['pdf', 'txt', 'html', 'htm'].includes(this.previewFileExt)) {
        return 'iframe'
      }
      if (['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'].includes(this.previewFileExt)) {
        return 'office'
      }
      return 'unsupported'
    },
    previewViewerSrc() {
      if (!this.previewFileUrl) {
        return ''
      }
      if (['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'].includes(this.previewFileExt)) {
        return 'https://view.officeapps.live.com/op/embed.aspx?src=' + encodeURIComponent(this.toAbsoluteUrl(this.previewFileUrl))
      }
      return this.toAbsoluteUrl(this.previewFileUrl)
    },
    previewEmptyText() {
      if (!this.previewFileUrl) {
        return '当前记录未绑定可预览文件'
      }
      return '当前文件类型暂不支持内嵌预览，请点击下载查看'
    }
  },
  created() {
    this.ensureRouteAndFetch()
  },
  activated() {
    this.ensureRouteAndFetch()
  },
  watch: {
    '$route.params.assetId'() {
      this.ensureRouteAndFetch()
    }
  },
  methods: {
    ensureRouteAndFetch() {
      if (!this.currentAssetId) {
        this.loading = false
        return
      }
      this.getDetail()
    },
    normalizeDetail(data) {
      const detail = data || {}
      return Object.assign({
        versionList: [],
        resubmitRecordList: []
      }, detail)
    },
    getDetail() {
      this.loading = true
      getAsset(this.currentAssetId).then(response => {
        this.detail = this.normalizeDetail(response.data)
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    goBack() {
      this.$router.push('/audit-asset/list')
    },
    defaultSteps(type) {
      const step3Title = type === 'pass' ? 'AI审核初步通过' : '待修改'
      const step3Content = type === 'pass' ? 'AI已初步通过审核' : 'AI检测出问题1,2,3'
      return [
        {
          stepTitle: '排队等待AI分析',
          stepTime: '2025-06-24 15:29:23',
          stepContent: '当前队列共计30个，该条位于队伍第三位'
        },
        {
          stepTitle: 'AI正在解析审核',
          stepTime: '2025-06-24 15:30:23',
          stepContent: 'AI解析进度100%，已全部解析完成'
        },
        {
          stepTitle: step3Title,
          stepTime: '2025-06-24 15:32:23',
          stepContent: step3Content
        }
      ]
    },
    versionStepList(item, versionIndex) {
      if (item && Array.isArray(item.stepList) && item.stepList.length) {
        return item.stepList
      }
      return this.defaultSteps(versionIndex === 0 ? 'pass' : 'modify')
    },
    openFile(url) {
      if (!url) {
        this.$modal.msgWarning('暂无可访问文件')
        return
      }
      window.open(encodeURI(url))
    },
    openPreview(url, fileName) {
      if (!url) {
        this.$modal.msgWarning('当前记录未绑定可预览文件')
        return
      }
      this.previewTarget = {
        url,
        name: fileName || this.getFileName(url)
      }
      this.previewOpen = true
    },
    submitDownload() {
      if (!this.downloadForm.pdf && !this.downloadForm.word) {
        this.$modal.msgWarning('请至少选择一种下载格式')
        return
      }
      const downloadUrl = this.previewTarget.url || this.detail.reportFileUrl
      if (!downloadUrl) {
        this.$modal.msgWarning('当前记录未绑定报告文件')
        return
      }
      this.openFile(downloadUrl)
      this.downloadOpen = false
    },
    splitImages(imageUrls) {
      if (!imageUrls) {
        return []
      }
      return String(imageUrls).split(',').map(item => item.trim()).filter(Boolean)
    },
    resolveRecordImages(record) {
      const list = this.splitImages(record.imageUrls)
      if (list.length) {
        return list.map(url => ({ url, placeholder: false }))
      }
      return [
        { placeholder: true },
        { placeholder: true }
      ]
    },
    handleReuploadOpen() {
      const assetId = this.detail.assetId || this.currentAssetId
      if (!assetId) {
        this.$modal.msgWarning('审核资源参数缺失，无法重新上传')
        return
      }
      this.reuploadForm = {
        assetId,
        submitter: this.detail.submitter || '',
        fileUrl: '',
        fileName: '',
        imageUrls: ''
      }
      this.resetForm('reuploadForm')
      this.reuploadOpen = true
    },
    submitReupload() {
      const assetId = this.reuploadForm.assetId || this.currentAssetId
      if (!assetId) {
        this.$modal.msgWarning('审核资源参数缺失，无法提交')
        return
      }
      this.$refs.reuploadForm.validate(valid => {
        if (!valid) {
          return
        }
        const fileUrl = this.reuploadForm.fileUrl
        reuploadAsset({
          assetId,
          submitter: this.reuploadForm.submitter,
          fileUrl,
          fileName: this.getFileName(fileUrl),
          imageUrls: this.reuploadForm.imageUrls
        }).then(() => {
          this.$modal.msgSuccess('重新上传成功')
          this.reuploadOpen = false
          this.getDetail()
        })
      })
    },
    getFileName(url) {
      if (!url) {
        return ''
      }
      const cleanUrl = String(url).split('?')[0]
      return decodeURIComponent(cleanUrl.substring(cleanUrl.lastIndexOf('/') + 1))
    },
    getFileExt(fileName, url) {
      const source = fileName || this.getFileName(url) || ''
      const cleanName = String(source).split('?')[0]
      const dotIndex = cleanName.lastIndexOf('.')
      if (dotIndex < 0) {
        return ''
      }
      return cleanName.substring(dotIndex + 1).toLowerCase()
    },
    normalizeFileUrl(url) {
      if (!url) {
        return ''
      }
      return String(url).trim()
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
    toCircleNo(index) {
      const circleList = ['①', '②', '③', '④', '⑤', '⑥', '⑦', '⑧', '⑨', '⑩']
      return circleList[index - 1] || index + '.'
    }
  }
}
</script>

<style scoped lang="scss">
.audit-asset-detail-page {
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

.head-actions .el-button + .el-button {
  margin-left: 10px;
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
}

.base-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 14px;

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

.status-chip {
  display: inline-flex;
  align-items: center;
  padding: 7px 14px;
  border-radius: 4px;
  font-size: 13px;
  line-height: 1;
}

.status-chip.success {
  color: #67c23a;
  background: #edf9e8;
  border: 1px solid #d8f0ca;
}

.detail-layout {
  display: flex;
  gap: 14px;
  align-items: stretch;
}

.panel-card {
  width: calc(50% - 7px);
  min-height: 654px;
  display: flex;
  flex-direction: column;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.inner-card {
  flex: 1;
  border: 1px solid #ebeef5;
  padding: 16px 16px 10px;
}

.sub-title {
  color: #303133;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 14px;
}

.version-banner {
  background: #f5f7fa;
  border: 1px solid #f0f3f7;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #606266;
  font-size: 13px;
  margin-bottom: 14px;
}

.timeline {
  padding: 4px 4px 0;
}

.timeline-row {
  display: flex;
  gap: 14px;
}

.timeline-row + .timeline-row {
  margin-top: 22px;
}

.timeline-axis {
  width: 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.timeline-index {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #fff;
  border: 2px solid #4a96e9;
  color: #4a96e9;
  font-size: 16px;
  line-height: 24px;
  text-align: center;
  font-weight: 600;
}

.timeline-line {
  width: 2px;
  flex: 1;
  min-height: 78px;
  margin-top: 4px;
  background: #c9ddf7;
}

.timeline-content {
  flex: 1;
  min-width: 0;
}

.timeline-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.timeline-title {
  color: #303133;
  font-size: 15px;
  font-weight: 600;
}

.timeline-time {
  color: #909399;
  font-size: 12px;
  white-space: nowrap;
}

.timeline-text {
  color: #606266;
  font-size: 13px;
  line-height: 1.85;
  margin-top: 8px;
}

.reupload-btn {
  width: 72%;
  min-width: 320px;
  height: 46px;
  margin-top: 18px;
  border-radius: 2px;
}

.review-block + .review-block {
  margin-top: 28px;
}

.review-label {
  color: #303133;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 14px;
}

.review-box {
  background: #f7f9fb;
  border: 1px solid #f0f2f5;
  color: #606266;
  font-size: 14px;
  line-height: 1.9;
  padding: 18px 16px;
  min-height: 88px;
}

.final-box {
  min-height: 72px;
}

.review-user {
  margin: 22px 0 26px;
  color: #303133;
  font-size: 15px;
  font-weight: 600;
}

.preview-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 14px;
}

.preview-file-name {
  color: #606266;
  font-size: 13px;
  word-break: break-all;
}

.preview-viewer {
  height: 760px;
  border: 1px solid #ebeef5;
  background: #fff;
  overflow: hidden;
  display: flex;
  align-items: stretch;
  justify-content: center;
}

.preview-frame {
  width: 100%;
  height: 100%;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  margin: auto;
}

.preview-empty {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #909399;
  font-size: 14px;
}

.preview-empty i {
  font-size: 42px;
  color: #c0c4cc;
}

.download-label {
  color: #303133;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 16px;
}

.download-options {
  display: flex;
  gap: 80px;
  padding: 8px 0 20px;
}

.version-count {
  color: #409eff;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}

.version-list {
  max-height: 560px;
  overflow-y: auto;
  padding-right: 2px;
}

.version-card {
  border-top: 1px solid #f0f2f5;
  padding-top: 14px;
}

.version-card + .version-card {
  margin-top: 16px;
}

.version-card-head {
  background: #f5f7fa;
  padding: 14px 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #606266;
  font-size: 13px;
  margin-bottom: 16px;
}

.version-name {
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.version-step {
  display: flex;
  gap: 14px;
}

.version-step + .version-step {
  margin-top: 18px;
}

.version-step-axis {
  width: 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.version-step-index {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 2px solid #4a96e9;
  color: #4a96e9;
  font-size: 16px;
  line-height: 24px;
  text-align: center;
  font-weight: 600;
}

.version-step-line {
  width: 2px;
  flex: 1;
  min-height: 62px;
  margin-top: 4px;
  background: #c9ddf7;
}

.version-step-body {
  flex: 1;
}

.version-step-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.version-step-title {
  color: #303133;
  font-size: 15px;
  font-weight: 600;
}

.version-step-time {
  color: #909399;
  font-size: 12px;
}

.version-step-text {
  margin-top: 8px;
  color: #606266;
  font-size: 13px;
  line-height: 1.8;
}

.record-list {
  max-height: 640px;
  overflow-y: auto;
  padding-right: 2px;
}

.record-card + .record-card {
  margin-top: 18px;
}

.record-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.record-index {
  color: #303133;
  font-size: 20px;
  line-height: 1;
}

.record-version {
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.record-frame {
  border: 1px dashed #dfe6ec;
  padding: 14px 16px 16px;
}

.record-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  color: #606266;
  font-size: 14px;
  margin-bottom: 12px;
}

.record-label {
  color: #303133;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
}

.record-file {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 12px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.record-file-main {
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

.record-file-name {
  color: #303133;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.record-images {
  display: flex;
  gap: 14px;
}

.record-image {
  width: 74px;
  height: 90px;
  border: 1px solid #ebeef5;
  background: #fff;
}

.record-image.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fffdf6;
}

.placeholder-paper {
  width: 46px;
  height: 62px;
  border: 1px solid #e6e9ef;
  background:
    linear-gradient(180deg, #ffffff 0%, #f8fafc 100%),
    #fff;
  box-shadow: inset 0 0 0 1px #f3f5f8;
  position: relative;
}

.placeholder-paper::before,
.placeholder-paper::after {
  content: '';
  position: absolute;
  left: 8px;
  right: 8px;
  height: 3px;
  background: #d9e2ef;
}

.placeholder-paper::before {
  top: 16px;
}

.placeholder-paper::after {
  top: 28px;
}

.reupload-dialog ::v-deep .el-upload--picture-card,
.reupload-dialog ::v-deep .el-upload-list__item {
  border-radius: 2px;
}

@media (max-width: 1480px) {
  .detail-layout {
    flex-direction: column;
  }

  .panel-card {
    width: 100%;
    min-height: 0;
  }

  .reupload-btn {
    width: 100%;
    min-width: 0;
  }

  .record-meta,
  .record-file,
  .version-card-head,
  .version-step-head,
  .timeline-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
