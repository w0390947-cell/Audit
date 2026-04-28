<template>
  <div class="app-container audit-review-page">
    <el-form ref="queryForm" :model="queryParams" :inline="true" size="small" class="query-form">
      <el-form-item prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          clearable
          placeholder="请输入关键搜索词"
          style="width: 260px"
          @keyup.enter.native="handleQuery"
        >
          <i slot="prefix" class="el-input__icon el-icon-search" />
        </el-input>
      </el-form-item>
      <el-form-item label="任务状态" prop="taskStatus">
        <el-select v-model="queryParams.taskStatus" clearable placeholder="请选择任务状态" style="width: 220px">
          <el-option
            v-for="dict in dict.type.audit_review_task_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="选择日期">
        <el-date-picker
          v-model="dateRange"
          clearable
          end-placeholder="结束日期"
          range-separator="至"
          start-placeholder="开始日期"
          style="width: 260px"
          type="daterange"
          value-format="yyyy-MM-dd"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="toolbar-row">
      <div />
      <div class="toolbar-actions">
        <el-button
          plain
          icon="el-icon-download"
          size="small"
          @click="handleExport"
          v-hasPermi="['audit:review:export']"
        >导出数据</el-button>
        <el-button
          type="primary"
          icon="el-icon-plus"
          size="small"
          @click="handleAdd"
          v-hasPermi="['audit:review:add']"
        >新增</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="reviewList" class="review-table">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="任务编号" align="center" prop="taskNo" min-width="150" />
      <el-table-column label="产品名称" align="center" prop="productName" min-width="120" />
      <el-table-column label="送检单位" align="center" prop="deliveryUnit" min-width="130" />
      <el-table-column label="上传时间" align="center" prop="submitTime" min-width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.submitTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="AI分析次数" align="center" prop="aiAnalysisCount" width="110" />
      <el-table-column label="发起人" align="center" prop="sponsor" width="110" />
      <el-table-column label="任务状态" align="center" prop="taskStatus" width="110">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.audit_review_task_status" :value="scope.row.taskStatus" />
        </template>
      </el-table-column>
      <el-table-column label="审核状态" align="center" prop="reviewStatus" width="110">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.audit_review_status" :value="scope.row.reviewStatus" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" min-width="250" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            @click="handleHistory(scope.row)"
            v-hasPermi="['audit:review:history']"
          >版本追溯</el-button>
          <el-button
            size="mini"
            type="text"
            @click="handleDetail(scope.row)"
            v-hasPermi="['audit:review:detail']"
          >详情</el-button>
          <el-button
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['audit:review:edit']"
          >编辑</el-button>
          <el-button
            size="mini"
            type="text"
            @click="handleToggleStatus(scope.row)"
            v-hasPermi="['audit:review:changeStatus']"
          >{{ scope.row.processFlag === '1' ? '恢复' : '暂停' }}</el-button>
          <el-button
            size="mini"
            type="text"
            class="delete-btn"
            @click="handleDelete(scope.row)"
            v-hasPermi="['audit:review:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :limit.sync="queryParams.pageSize"
      :page.sync="queryParams.pageNum"
      :total="total"
      @pagination="getList"
    />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body class="task-dialog">
      <el-form ref="form" :model="form" :rules="rules" label-position="top" class="task-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产品名称" prop="productName">
              <el-input v-model="form.productName" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="送检单位" prop="deliveryUnit">
              <el-input v-model="form.deliveryUnit" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级选择" prop="priority">
              <el-select v-model="form.priority" placeholder="请选择" style="width: 100%">
                <el-option
                  v-for="dict in dict.type.audit_review_priority"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经办人选择" prop="handlerName">
              <el-select v-model="form.handlerName" clearable placeholder="请选择" style="width: 100%">
                <el-option
                  v-for="item in operatorOptions"
                  :key="item.userId"
                  :label="item.nickName || item.userName"
                  :value="item.nickName || item.userName"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="文件上传区" prop="mainReportUrls">
          <div class="upload-panel">
            <FileUpload
              v-model="form.mainReportUrls"
              :limit="2"
              :file-size="20"
              :is-show-tip="false"
              :dropzone="true"
              button-text="上传文件"
              drop-hint="拖放文件"
            />
          </div>
        </el-form-item>

        <el-form-item label="依据文件子区" prop="basisFileUrls">
          <div class="upload-sub-head">
            <span />
            <el-button type="text" class="library-link" @click="handleSelectFromLibrary">从已有文件库选择</el-button>
          </div>
          <div class="upload-panel">
            <FileUpload
              v-model="form.basisFileUrls"
              :limit="8"
              :file-size="20"
              :is-show-tip="false"
              :dropzone="true"
              button-text="上传文件"
              drop-hint="拖放文件"
            />
          </div>
        </el-form-item>

        <el-form-item label="上传报告子区" prop="appendixFileUrls">
          <div class="upload-panel">
            <FileUpload
              v-model="form.appendixFileUrls"
              :limit="8"
              :file-size="20"
              :is-show-tip="false"
              :dropzone="true"
              button-text="上传文件"
              drop-hint="拖放文件"
            />
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="历史版本" :visible.sync="historyOpen" width="780px" append-to-body>
      <div v-loading="historyLoading" class="history-list">
        <div v-for="item in historyList" :key="item.versionId" class="history-card">
          <div class="history-head">
            <div>
              <span class="history-version">{{ item.versionNo }}版本</span>
              <span v-if="item.currentFlag === '1'" class="current-tag">当前版本</span>
            </div>
          </div>
          <div class="history-file">
            <i class="el-icon-document file-icon" />
            <span class="file-name">{{ item.reportFileName }}</span>
            <span class="history-time">上传时间：{{ parseTime(item.submitTime) }}</span>
          </div>
          <div class="history-footer">
            <span>检测状态：{{ selectDictLabel(dict.type.audit_review_task_status, item.detectStatus) }}</span>
            <el-button type="text" @click="jumpVersionDetail(item)">点击跳转至该版本详情</el-button>
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
import FileUpload from '@/components/FileUpload'
import {
  addReview,
  changeReviewProcessFlag,
  delReview,
  getReview,
  listReview,
  listReviewOperators,
  listReviewVersions,
  updateReview
} from '@/api/audit/review'

export default {
  name: 'AuditReview',
  components: { FileUpload },
  dicts: ['audit_review_priority', 'audit_review_task_status', 'audit_review_status'],
  data() {
    return {
      loading: true,
      total: 0,
      reviewList: [],
      dateRange: [],
      operatorOptions: [],
      open: false,
      historyOpen: false,
      historyLoading: false,
      title: '',
      historyList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        keyword: undefined,
        taskStatus: undefined
      },
      form: {},
      rules: {
        productName: [
          { required: true, message: '产品名称不能为空', trigger: 'blur' }
        ],
        deliveryUnit: [
          { required: true, message: '送检单位不能为空', trigger: 'blur' }
        ],
        priority: [
          { required: true, message: '优先级不能为空', trigger: 'change' }
        ],
        mainReportUrls: [
          { required: true, message: '请上传主报告文件', trigger: 'change' }
        ],
        appendixFileUrls: [
          { required: true, message: '请上传报告子区文件', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getOperators()
  },
  methods: {
    getList() {
      this.loading = true
      listReview(this.addDateRange({ ...this.queryParams }, this.dateRange, 'SubmitTime')).then(response => {
        this.reviewList = response.rows
        this.total = response.total
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    getOperators() {
      listReviewOperators().then(response => {
        this.operatorOptions = response.data || []
      })
    },
    reset() {
      this.form = {
        taskId: undefined,
        taskNo: undefined,
        productName: undefined,
        deliveryUnit: undefined,
        sponsor: undefined,
        handlerName: undefined,
        priority: 'medium',
        mainReportUrls: '',
        basisFileUrls: '',
        appendixFileUrls: '',
        reviewStatus: 'pending',
        taskStatus: 'uploaded',
        processFlag: '0',
        remark: ''
      }
      this.resetForm('form')
    },
    cancel() {
      this.open = false
      this.reset()
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.dateRange = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleAdd() {
      this.reset()
      this.title = '新增'
      this.open = true
    },
    handleUpdate(row) {
      this.reset()
      getReview(row.taskId).then(response => {
        const data = response.data || {}
        this.form = {
          taskId: data.taskId,
          taskNo: data.taskNo,
          productName: data.productName,
          deliveryUnit: data.deliveryUnit,
          sponsor: data.sponsor,
          handlerName: data.handlerName,
          priority: data.priority || 'medium',
          mainReportUrls: data.mainReportUrls || '',
          basisFileUrls: data.basisFileUrls || '',
          appendixFileUrls: data.appendixFileUrls || '',
          reviewStatus: data.reviewStatus,
          taskStatus: data.taskStatus,
          processFlag: data.processFlag,
          remark: data.remark
        }
        this.title = '编辑'
        this.open = true
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        const request = this.form.taskId ? updateReview(this.form) : addReview(this.form)
        request.then(() => {
          this.$modal.msgSuccess(this.form.taskId ? '修改成功' : '新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDetail(row) {
      this.$router.push({
        path: '/audit/review/detail/' + row.taskId
      })
    },
    handleHistory(row) {
      this.historyLoading = true
      this.historyOpen = true
      listReviewVersions(row.taskId).then(response => {
        this.historyList = response.data || []
        this.historyLoading = false
      }).catch(() => {
        this.historyLoading = false
      })
    },
    jumpVersionDetail(row) {
      this.historyOpen = false
      this.$router.push({
        path: '/audit/review/detail/' + row.taskId,
        query: { versionId: row.versionId }
      })
    },
    handleSelectFromLibrary() {
      this.$message.info('请在审核文件库中选择目标文件后回填到当前任务')
    },
    handleToggleStatus(row) {
      const nextFlag = row.processFlag === '1' ? '0' : '1'
      const actionText = nextFlag === '1' ? '暂停' : '恢复'
      this.$modal.confirm('确认要' + actionText + '任务编号为“' + row.taskNo + '”的数据项吗？').then(() => {
        return changeReviewProcessFlag({
          taskId: row.taskId,
          processFlag: nextFlag
        })
      }).then(() => {
        this.$modal.msgSuccess(actionText + '成功')
        this.getList()
      }).catch(() => {})
    },
    handleDelete(row) {
      this.$modal.confirm('确认要删除任务编号为“' + row.taskNo + '”的数据项吗？').then(() => {
        return delReview(row.taskId)
      }).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      }).catch(() => {})
    },
    handleExport() {
      this.download('audit/review/export', {
        ...this.queryParams,
        params: {
          beginSubmitTime: this.dateRange && this.dateRange.length ? this.dateRange[0] : undefined,
          endSubmitTime: this.dateRange && this.dateRange.length ? this.dateRange[1] : undefined
        }
      }, `审核列表_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style scoped lang="scss">
.audit-review-page {
  background: #f5f7fa;
}

.query-form,
.toolbar-row,
.review-table,
.pagination-container {
  background: #fff;
}

.query-form {
  padding: 18px 20px 0;
  margin-bottom: 14px;
  border-radius: 4px;
}

.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px 10px;
  margin-bottom: 0;
  border-radius: 4px 4px 0 0;
}

.toolbar-actions .el-button + .el-button {
  margin-left: 10px;
}

.review-table {
  padding: 0 20px 20px;
}

.review-table ::v-deep .el-table__header-wrapper th {
  background: #f5f7fa;
  color: #606266;
  font-weight: 500;
}

.review-table ::v-deep .el-table__cell {
  padding-top: 10px;
  padding-bottom: 10px;
}

.review-table ::v-deep .el-button--text {
  color: #409eff;
}

.delete-btn {
  color: #f56c6c;
}

.task-form {
  padding-top: 8px;
}

.task-dialog ::v-deep .el-dialog {
  margin-top: 4vh !important;
}

.task-dialog ::v-deep .el-dialog__body {
  max-height: calc(100vh - 190px);
  overflow-y: auto;
  padding: 10px 20px 0 !important;
}

.task-dialog ::v-deep .el-dialog__footer {
  padding: 14px 20px 18px;
}

.task-form ::v-deep .el-form-item__label {
  padding-bottom: 6px;
  line-height: 1.3;
  color: #303133;
}

.task-form ::v-deep .el-form-item {
  margin-bottom: 16px;
}

.upload-sub-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.library-link {
  padding: 0;
  font-size: 13px;
}

.upload-panel {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px 12px 4px;
  background: #fff;
}

.upload-panel ::v-deep .upload-file-uploader {
  margin-bottom: 10px;
}

.upload-panel ::v-deep .upload-file-uploader .el-upload,
.upload-panel ::v-deep .upload-file-uploader .el-upload-dragger {
  width: 100%;
  min-height: 134px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  background: #fafcff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  box-sizing: border-box;
}

.upload-panel ::v-deep .upload-file-uploader .el-upload-dragger {
  padding: 0;
}

.upload-panel ::v-deep .upload-file-uploader .el-upload:hover,
.upload-panel ::v-deep .upload-file-uploader .el-upload-dragger:hover {
  border-color: #c7dbff;
  background: #f7faff;
}

.upload-panel ::v-deep .upload-file-uploader .el-button {
  border: none;
  background: transparent;
  color: #409eff;
  font-size: 16px;
  font-weight: 500;
  padding: 0;
  height: auto;
}

.upload-panel ::v-deep .upload-file-uploader .el-upload__tip {
  display: none;
}

.upload-panel ::v-deep .upload-file-list .el-upload-list__item {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 10px;
  padding: 10px 12px;
  line-height: 1.5;
}

.upload-panel ::v-deep .upload-file-list .el-icon-document {
  color: #409eff;
}

.upload-panel ::v-deep .upload-file-list .el-link--danger {
  color: #f56c6c;
}

.history-list {
  max-height: 520px;
  overflow-y: auto;
}

.history-card {
  border: 1px dashed #dfe6ec;
  border-radius: 6px;
  padding: 18px 18px 16px;
}

.history-card + .history-card {
  margin-top: 14px;
}

.history-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.history-version {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin-right: 8px;
}

.current-tag {
  color: #409eff;
  font-size: 12px;
}

.history-file {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 10px;
}

.file-icon {
  font-size: 20px;
  color: #409eff;
}

.file-name {
  flex: 1;
  color: #303133;
}

.history-time {
  color: #909399;
}

.history-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #606266;
}
</style>
