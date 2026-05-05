<template>
  <div class="app-container audit-library-page">
    <el-form ref="queryForm" :model="queryParams" :inline="true" size="small" class="query-form">
      <el-form-item prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          clearable
          placeholder="请输入关键搜索词"
          style="width: 300px"
          @keyup.enter.native="handleQuery"
        >
          <i slot="prefix" class="el-input__icon el-icon-search" />
        </el-input>
      </el-form-item>
      <el-form-item label="文件入库状态" prop="storageStatus">
        <el-select v-model="queryParams.storageStatus" clearable placeholder="请选择文件入库状态" style="width: 240px">
          <el-option
            v-for="dict in dict.type.audit_file_storage_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="选择日期">
        <el-date-picker
          v-model="queryDate"
          clearable
          placeholder="请选择日期"
          style="width: 230px"
          type="date"
          value-format="yyyy-MM-dd"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="table-card">
      <div class="toolbar-row">
        <div />
        <div class="toolbar-actions">
          <el-button
            type="primary"
            size="small"
            icon="el-icon-plus"
            @click="handleAdd"
            v-hasPermi="['audit:library:common:add']"
          >
            添加文件
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="resourceList"
        class="library-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="52" align="center" />
        <el-table-column label="文档名称" align="left" prop="documentName" min-width="140" />
        <el-table-column label="归属文件库" align="left" prop="folderName" min-width="130">
          <template slot-scope="scope">
            {{ scope.row.folderName || '文件库一' }}
          </template>
        </el-table-column>
        <el-table-column label="文件入库状态" align="center" prop="storageStatus" width="120">
          <template slot-scope="scope">
            <el-tag :class="['status-pill', scope.row.storageStatus]" disable-transitions>
              {{ storageStatusLabel(scope.row.storageStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="文件进度" align="left" min-width="250">
          <template slot-scope="scope">
            <div class="progress-cell">
              <span :class="['progress-text', scope.row.storageStatus]">{{ scope.row.progressText || '--' }}</span>
              <div :class="['progress-bar', scope.row.storageStatus]">
                <span :style="{ width: progressWidth(scope.row) }" />
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="创建者" align="center" prop="creator" width="120" />
        <el-table-column label="最新修改时间" align="center" prop="latestModifyTime" width="160">
          <template slot-scope="scope">
            {{ parseTime(scope.row.latestModifyTime) }}
          </template>
        </el-table-column>
        <el-table-column label="文件大小" align="center" prop="fileSize" width="90" />
        <el-table-column label="操作" align="left" min-width="330" class-name="small-padding">
          <template slot-scope="scope">
            <el-button size="mini" type="text" @click="openFile(scope.row.fileUrl)">预览</el-button>
            <el-button
              size="mini"
              type="text"
              @click="handleUpdate(scope.row)"
              v-hasPermi="['audit:library:common:edit']"
            >
              编辑
            </el-button>
            <el-button size="mini" type="text" @click="openFile(scope.row.fileUrl)">下载</el-button>
            <el-button
              size="mini"
              type="text"
              class="delete-btn"
              @click="handleDelete(scope.row)"
              v-hasPermi="['audit:library:common:remove']"
            >
              删除
            </el-button>
            <el-button
              size="mini"
              type="text"
              @click="handleAssignFolder(scope.row)"
              v-hasPermi="['audit:library:common:assignFolder']"
            >
              归类文件库
            </el-button>
            <el-button
              v-if="scope.row.storageStatus !== 'processing'"
              size="mini"
              type="text"
              @click="handleVersion(scope.row)"
            >
              历史版本
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />
    </div>

    <el-dialog :title="title" :visible.sync="open" width="720px" append-to-body custom-class="library-file-dialog">
      <el-form ref="form" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="文档名称" prop="documentName">
          <el-input v-model="form.documentName" placeholder="请输入文档名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="创建者" prop="creator">
          <el-input v-model="form.creator" placeholder="请输入创建者" maxlength="64" />
        </el-form-item>
        <el-form-item label="上传文件" prop="fileUrl">
          <div class="drag-upload-panel">
            <FileUpload
              v-model="form.fileUrl"
              :limit="1"
              :file-size="30"
              :dropzone="true"
              button-text="上传文件"
              drop-hint="拖放文件"
              @upload-success="handleFileUploadSuccess"
            />
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog
      title="选择归属文件库"
      :visible.sync="assignOpen"
      width="620px"
      append-to-body
      custom-class="assign-folder-dialog"
    >
      <div class="folder-picker-grid">
        <div
          v-for="item in folderOptions"
          :key="item.folderId"
          :class="['folder-picker-card', { active: selectedFolderId === item.folderId }]"
          @click="selectedFolderId = item.folderId"
        >
          <div class="picker-icon">
            <span class="folder-top" />
            <span class="folder-line" />
            <span class="folder-body" />
          </div>
          <div class="picker-name">{{ item.folderName }}</div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="assignOpen = false">取消</el-button>
        <el-button type="primary" @click="submitAssignFolder">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="历史版本" :visible.sync="versionOpen" width="720px" append-to-body>
      <div class="version-list">
        <div v-for="item in versionList" :key="item.versionId" class="version-card">
          <div class="version-head">
            <div>
              <span class="version-title">{{ item.versionNo }}</span>
              <span v-if="item.versionNo === currentVersionNo" class="version-tag">当前版本</span>
            </div>
            <span class="version-time">上传时间：{{ parseTime(item.createTime) }}</span>
          </div>
          <div class="version-file">
            <i class="el-icon-document" />
            <span>{{ item.fileName }}</span>
            <el-button type="text" @click="openFile(item.fileUrl)">文件预览</el-button>
          </div>
          <div class="version-meta">
            <span>创建者：{{ item.creator || '--' }}</span>
            <span>文件大小：{{ item.fileSize || '--' }}</span>
          </div>
        </div>
        <el-empty v-if="!versionList.length" description="暂无历史版本" :image-size="90" />
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="versionOpen = false">取消</el-button>
        <el-button type="primary" @click="versionOpen = false">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import FileUpload from '@/components/FileUpload'
import {
  addCommonResource,
  assignCommonResourceFolder,
  delCommonResource,
  getCommonResource,
  listCommonResource,
  listLibraryFolderOptions,
  updateCommonResource
} from '@/api/audit/library'

export default {
  name: 'AuditLibraryCommon',
  components: { FileUpload },
  dicts: ['audit_file_storage_status'],
  data() {
    return {
      loading: false,
      total: 0,
      open: false,
      assignOpen: false,
      versionOpen: false,
      title: '',
      queryDate: undefined,
      resourceList: [],
      folderOptions: [],
      selectedIds: [],
      versionList: [],
      currentVersionNo: '',
      currentRow: null,
      selectedFolderId: undefined,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        keyword: undefined,
        storageStatus: undefined
      },
      form: {
        resourceId: undefined,
        documentName: '',
        creator: '',
        fileUrl: '',
        fileName: '',
        fileSize: '',
        folderId: undefined,
        folderName: ''
      },
      rules: {
        documentName: [
          { required: true, message: '文档名称不能为空', trigger: 'blur' }
        ],
        creator: [
          { required: true, message: '创建者不能为空', trigger: 'blur' }
        ],
        fileUrl: [
          { required: true, message: '请上传文件', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getFolderOptions()
  },
  methods: {
    formatDateRange() {
      if (!this.queryDate) {
        return []
      }
      return [this.queryDate, this.queryDate]
    },
    storageStatusLabel(status) {
      if (status === 'stored') {
        return '已入库'
      }
      if (status === 'failed') {
        return '入库失败'
      }
      return '入库中'
    },
    getList() {
      this.loading = true
      listCommonResource(this.addDateRange({ ...this.queryParams }, this.formatDateRange(), 'LatestModifyTime')).then(response => {
        this.resourceList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    getFolderOptions() {
      listLibraryFolderOptions().then(response => {
        this.folderOptions = response.data || []
      })
    },
    progressWidth(row) {
      if (row.storageStatus === 'stored') {
        return '100%'
      }
      if (row.storageStatus === 'failed') {
        return '70%'
      }
      return '30%'
    },
    handleSelectionChange(selection) {
      this.selectedIds = selection.map(item => item.resourceId)
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.queryDate = undefined
      this.resetForm('queryForm')
      this.handleQuery()
    },
    reset() {
      this.form = {
        resourceId: undefined,
        documentName: '',
        creator: this.$store.getters.name || 'admin',
        fileUrl: '',
        fileName: '',
        fileSize: '',
        folderId: undefined,
        folderName: ''
      }
      this.resetForm('form')
    },
    handleAdd() {
      this.reset()
      this.title = '添加文件'
      this.open = true
      this.$nextTick(() => {
        this.$refs.form && this.$refs.form.clearValidate()
      })
    },
    handleUpdate(row) {
      getCommonResource(row.resourceId).then(response => {
        const data = response.data || {}
        this.reset()
        this.form = {
          resourceId: data.resourceId,
          documentName: data.documentName,
          creator: data.creator,
          fileUrl: data.fileUrl,
          fileName: data.fileName,
          fileSize: data.fileSize || '',
          folderId: data.folderId,
          folderName: data.folderName || ''
        }
        this.title = '编辑文件'
        this.open = true
        this.$nextTick(() => {
          this.$refs.form && this.$refs.form.clearValidate()
        })
      })
    },
    handleFileUploadSuccess(payload) {
      this.form.fileSize = payload.fileSize || ''
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        const fileUrl = this.form.fileUrl
        const data = {
          resourceId: this.form.resourceId,
          documentName: this.form.documentName,
          creator: this.form.creator,
          fileUrl,
          fileName: this.getFileName(fileUrl),
          fileSize: this.form.fileSize || '',
          folderId: this.form.folderId,
          folderName: this.form.folderName,
          storageStatus: this.form.resourceId ? undefined : 'processing',
          progressText: this.form.resourceId ? '文本解析智能体解析成功' : '文本解析智能体解析中'
        }
        const request = this.form.resourceId ? updateCommonResource(data) : addCommonResource(data)
        request.then(() => {
          this.$modal.msgSuccess(this.form.resourceId ? '修改成功' : '新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除文档“' + row.documentName + '”？').then(() => {
        return delCommonResource(row.resourceId)
      }).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      }).catch(() => {})
    },
    handleAssignFolder(row) {
      this.currentRow = row
      this.selectedFolderId = row.folderId
      this.assignOpen = true
    },
    submitAssignFolder() {
      if (!this.selectedFolderId) {
        this.$modal.msgWarning('请选择归属文件库')
        return
      }
      const folder = this.folderOptions.find(item => item.folderId === this.selectedFolderId)
      assignCommonResourceFolder({
        resourceId: this.currentRow.resourceId,
        folderId: folder.folderId,
        folderName: folder.folderName
      }).then(() => {
        this.$modal.msgSuccess('归类成功')
        this.assignOpen = false
        this.getList()
      })
    },
    handleVersion(row) {
      getCommonResource(row.resourceId).then(response => {
        const data = response.data || {}
        this.versionList = data.versionList || []
        this.currentVersionNo = data.currentVersionNo || ''
        this.versionOpen = true
      })
    },
    handleExport() {
      this.download(
        'audit/library/common/export',
        this.addDateRange({ ...this.queryParams }, this.formatDateRange(), 'LatestModifyTime'),
        '常用文件资源_' + this.parseTime(new Date(), '{y}{m}{d}{h}{i}{s}') + '.xlsx'
      )
    },
    openFile(url) {
      if (!url) {
        this.$modal.msgWarning('暂无可访问文件')
        return
      }
      window.open(encodeURI(url))
    },
    getFileName(url) {
      if (!url) {
        return ''
      }
      return url.substring(url.lastIndexOf('/') + 1)
    }
  }
}
</script>

<style scoped lang="scss">
.audit-library-page {
  min-height: calc(100vh - 96px);
  background: #f2f3f5;
}

.query-form,
.table-card {
  padding: 14px 16px;
  margin-bottom: 14px;
  background: #fff;
  border-radius: 4px;
}

.query-form {
  ::v-deep .el-form-item {
    margin-right: 14px;
    margin-bottom: 8px;
  }
}

.table-card {
  padding-bottom: 10px;
}

.toolbar-row {
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar-actions {
  display: flex;
  gap: 10px;
}

::v-deep .library-table th.el-table__cell {
  background: #f2f4f8;
  color: #6b7280;
  font-weight: 600;
}

::v-deep .library-table .el-table__cell {
  padding-top: 10px;
  padding-bottom: 10px;
}

::v-deep .library-table .cell {
  line-height: 26px;
}

::v-deep .library-table .el-button--text {
  color: #4b92e8;
  font-size: 13px;
  padding: 0;
  margin-right: 12px;
}

::v-deep .library-table .el-button--text:last-child {
  margin-right: 0;
}

.progress-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.progress-bar {
  width: 210px;
  height: 8px;
  border-radius: 999px;
  overflow: hidden;
  background: #edf1f6;
}

.progress-bar span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: #2f88ec;
}

.progress-bar.failed span {
  background: #f56c6c;
}

.progress-bar.stored span {
  background: #2f88ec;
}

.progress-text {
  color: #6b7380;
  line-height: 1.4;
}

.progress-text.failed {
  color: #ef6a67;
}

.progress-text.processing {
  color: #5f6472;
}

.progress-text.stored {
  color: #5f6472;
}

.status-pill {
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  padding: 0 10px;
  border: 1px solid transparent;
}

.status-pill.processing {
  background: #eaf8eb;
  border-color: #d4f0d8;
  color: #58bf74;
}

.status-pill.stored {
  background: #eaf3ff;
  border-color: #dbe9ff;
  color: #5c96ec;
}

.status-pill.failed {
  background: #fdeeed;
  border-color: #fadad7;
  color: #ef6d68;
}

.delete-btn {
  color: #f56c6c !important;
}

::v-deep .library-file-dialog .el-dialog__body {
  max-height: calc(100vh - 190px);
  overflow-y: auto;
  padding: 12px 20px 4px !important;
}

::v-deep .library-file-dialog .el-dialog__footer {
  padding: 14px 20px 18px;
}

.drag-upload-panel {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px 12px 4px;
  background: #fff;
}

.drag-upload-panel ::v-deep .upload-file-uploader {
  margin-bottom: 10px;
}

.drag-upload-panel ::v-deep .upload-file-uploader .el-upload,
.drag-upload-panel ::v-deep .upload-file-uploader .el-upload-dragger {
  width: 100%;
  min-height: 140px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  background: #fafcff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  box-sizing: border-box;
}

.drag-upload-panel ::v-deep .upload-file-uploader .el-upload-dragger {
  padding: 0;
}

.drag-upload-panel ::v-deep .upload-file-uploader .el-upload:hover,
.drag-upload-panel ::v-deep .upload-file-uploader .el-upload-dragger:hover {
  border-color: #c7dbff;
  background: #f7faff;
}

.drag-upload-panel ::v-deep .upload-file-list .el-upload-list__item {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 10px;
  padding: 10px 12px;
  line-height: 1.5;
}

.folder-picker-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.folder-picker-card {
  padding: 16px 10px 12px;
  text-align: center;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.folder-picker-card.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.picker-icon {
  position: relative;
  width: 66px;
  height: 50px;
  margin: 2px auto 10px;
}

.picker-icon .folder-top {
  position: absolute;
  top: -7px;
  left: 6px;
  width: 28px;
  height: 10px;
  border-radius: 5px 5px 0 0;
  background: linear-gradient(180deg, #ffe060 0%, #f4c400 100%);
  border: 1px solid rgba(228, 167, 0, 0.32);
  border-bottom: none;
}

.picker-icon .folder-line {
  position: absolute;
  top: 1px;
  left: 5px;
  right: 5px;
  height: 4px;
  border-radius: 3px;
  background: rgba(255, 251, 220, 0.62);
  z-index: 2;
}

.picker-icon .folder-body {
  display: block;
  position: absolute;
  inset: 0;
  border-radius: 7px;
  background: linear-gradient(180deg, #ffd638 0%, #f2be01 100%);
  border: 1px solid rgba(223, 160, 0, 0.28);
}

.picker-name {
  color: #303133;
  word-break: break-all;
}

::v-deep .assign-folder-dialog {
  border-radius: 10px;
  overflow: hidden;
}

::v-deep .assign-folder-dialog .el-dialog__header {
  border-bottom: 1px solid #ebeef3;
}

::v-deep .assign-folder-dialog .el-dialog__footer {
  border-top: 1px solid #ebeef3;
}

.version-card {
  margin-bottom: 16px;
  padding: 16px 18px;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
}

.version-head,
.version-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.version-head {
  margin-bottom: 14px;
}

.version-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.version-tag {
  margin-left: 8px;
  font-size: 12px;
  color: #409eff;
}

.version-time,
.version-meta {
  font-size: 13px;
  color: #909399;
}

.version-file {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  padding: 12px 14px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  color: #303133;
}

.version-file .el-icon-document {
  color: #409eff;
  font-size: 18px;
}
</style>
