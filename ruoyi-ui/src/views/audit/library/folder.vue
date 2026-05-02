<template>
  <div class="app-container audit-folder-page" v-loading="loading">
    <div class="library-panel">
      <div class="section-head section-head-top">
        <div class="section-title">{{ currentFolder ? currentFolder.folderName : '置顶文件库' }}</div>
        <el-button
          v-if="!currentFolder"
          type="primary"
          icon="el-icon-plus"
          size="small"
          @click="handleAdd"
          v-hasPermi="['audit:library:folder:add']"
        >
          新建文件库
        </el-button>
        <div v-else class="folder-head-actions">
          <el-button
            type="primary"
            icon="el-icon-plus"
            size="small"
            @click="handleAddFile"
            v-hasPermi="['audit:library:common:add']"
          >
            添加文件
          </el-button>
          <el-button
            icon="el-icon-back"
            size="small"
            @click="leaveFolder"
          >
            返回文件库
          </el-button>
        </div>
      </div>

      <div v-if="!currentFolder" class="folder-grid top-grid">
        <div
          v-for="item in topFolders"
          :key="item.folderId"
          class="folder-card"
          @click="enterFolder(item)"
        >
          <div class="folder-settings" @click.stop>
            <el-dropdown @command="handleCommand(item, $event)" trigger="click">
              <i class="el-icon-setting" />
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item
                  command="edit"
                  v-hasPermi="['audit:library:folder:edit']"
                >
                  编辑
                </el-dropdown-item>
                <el-dropdown-item
                  command="delete"
                  divided
                  v-hasPermi="['audit:library:folder:remove']"
                >
                  删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
          <div class="folder-graphic">
            <span class="folder-top" />
            <span class="folder-line" />
            <span class="folder-body" />
          </div>
          <div class="folder-caption">{{ item.folderName }}</div>
        </div>
        <el-empty v-if="!topFolders.length" description="暂无置顶文件库" :image-size="90" />
      </div>

      <div v-if="!currentFolder" class="section-head">
        <div class="section-title">全部文件库</div>
      </div>
      <div v-if="!currentFolder" class="folder-grid">
        <div
          v-for="item in normalFolders"
          :key="item.folderId"
          class="folder-card"
          @click="enterFolder(item)"
        >
          <div class="folder-settings" @click.stop>
            <el-dropdown @command="handleCommand(item, $event)" trigger="click">
              <i class="el-icon-setting" />
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item
                  command="edit"
                  v-hasPermi="['audit:library:folder:edit']"
                >
                  编辑
                </el-dropdown-item>
                <el-dropdown-item
                  command="delete"
                  divided
                  v-hasPermi="['audit:library:folder:remove']"
                >
                  删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
          <div class="folder-graphic">
            <span class="folder-top" />
            <span class="folder-line" />
            <span class="folder-body" />
          </div>
          <div class="folder-caption">{{ item.folderName }}</div>
        </div>
        <el-empty v-if="!normalFolders.length" description="暂无文件库" :image-size="90" />
      </div>

      <div v-if="currentFolder" class="folder-content">
        <div class="breadcrumb-row">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>
              <span class="breadcrumb-link" @click="leaveFolder">审核文件库</span>
            </el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentFolder.folderName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <el-table v-loading="resourceLoading" :data="folderResources" class="folder-resource-table">
          <el-table-column label="类型" width="110" align="center">
            <template slot-scope="scope">
              <el-tag size="small" :type="scope.row.resourceType === 'common' ? 'success' : 'primary'">
                {{ scope.row.resourceType === 'common' ? '常用文件' : '任务文件' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="文件名称" prop="displayName" min-width="220" show-overflow-tooltip />
          <el-table-column label="归属文件库" prop="folderName" min-width="150" show-overflow-tooltip />
          <el-table-column label="状态" min-width="130" align="center">
            <template slot-scope="scope">
              <el-tag
                v-if="scope.row.resourceType === 'common'"
                :class="['status-pill', scope.row.status]"
                disable-transitions
              >
                {{ scope.row.statusText || '--' }}
              </el-tag>
              <span v-else>{{ scope.row.statusText || '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="文件进度" align="left" min-width="250">
            <template slot-scope="scope">
              <div v-if="scope.row.resourceType === 'common'" class="progress-cell">
                <span :class="['progress-text', scope.row.status]">{{ scope.row.progressText || '--' }}</span>
                <div :class="['progress-bar', scope.row.status]">
                  <span :style="{ width: progressWidth(scope.row) }" />
                </div>
              </div>
              <span v-else>--</span>
            </template>
          </el-table-column>
          <el-table-column label="更新时间" min-width="170" align="center">
            <template slot-scope="scope">
              {{ parseTime(scope.row.displayTime) || '--' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="110" align="center">
            <template slot-scope="scope">
              <el-button type="text" size="mini" @click="openFile(scope.row.fileUrl)">预览文件</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty
          v-if="!resourceLoading && !folderResources.length"
          description="当前文件夹暂无文件"
          :image-size="110"
        />
      </div>
    </div>

    <el-dialog :title="title" :visible.sync="open" width="560px" append-to-body custom-class="folder-dialog">
      <el-form ref="form" :model="form" :rules="rules" label-width="86px" class="folder-form">
        <el-form-item label="名称" prop="folderName">
          <el-input v-model="form.folderName" placeholder="请输入" maxlength="100" />
        </el-form-item>
        <el-form-item label="简介" prop="intro">
          <el-input v-model="form.intro" type="textarea" :rows="4" placeholder="请输入" maxlength="500" />
        </el-form-item>
        <el-form-item label="可见范围" prop="visibleScope">
          <el-checkbox-group
            v-model="visibleScopeValues"
            class="visible-scope-group"
            @change="handleVisibleScopeChange"
          >
            <el-checkbox label="admin">仅超级管理员可见</el-checkbox>
            <el-checkbox label="all">全员可见</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="fileTitle" :visible.sync="fileOpen" width="720px" append-to-body custom-class="library-file-dialog">
      <el-form ref="fileForm" :model="fileForm" :rules="fileRules" label-width="96px">
        <el-form-item label="文档名称" prop="documentName">
          <el-input v-model="fileForm.documentName" placeholder="请输入文档名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="创建者" prop="creator">
          <el-input v-model="fileForm.creator" placeholder="请输入创建者" maxlength="64" />
        </el-form-item>
        <el-form-item label="上传文件" prop="fileUrl">
          <div class="drag-upload-panel">
            <FileUpload
              v-model="fileForm.fileUrl"
              :limit="1"
              :file-size="30"
              :dropzone="true"
              button-text="上传文件"
              drop-hint="拖放文件"
            />
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="fileOpen = false">取消</el-button>
        <el-button type="primary" @click="submitFileForm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import FileUpload from '@/components/FileUpload'
import {
  addLibraryFolder,
  addCommonResource,
  delLibraryFolder,
  listCommonResource,
  listLibraryFolders,
  listTaskResource,
  updateLibraryFolder
} from '@/api/audit/library'

export default {
  name: 'AuditLibraryFolder',
  components: { FileUpload },
  data() {
    return {
      loading: false,
      open: false,
      fileOpen: false,
      title: '',
      fileTitle: '',
      folderList: [],
      currentFolder: null,
      resourceLoading: false,
      folderResources: [],
      form: {
        folderId: undefined,
        folderName: '',
        intro: '',
        visibleScope: 'all',
        topFlag: '0'
      },
      fileForm: {
        documentName: '',
        creator: '',
        fileUrl: '',
        fileName: '',
        fileSize: '5MB'
      },
      visibleScopeValues: ['all'],
      rules: {
        folderName: [
          { required: true, message: '文件库名称不能为空', trigger: 'blur' }
        ],
        visibleScope: [
          { required: true, message: '可见范围不能为空', trigger: 'change' }
        ]
      },
      fileRules: {
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
  computed: {
    topFolders() {
      return this.folderList.filter(item => item.topFlag === '1')
    },
    normalFolders() {
      return this.folderList.filter(item => item.topFlag !== '1')
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listLibraryFolders().then(response => {
        this.folderList = response.data || []
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    enterFolder(folder) {
      this.currentFolder = folder
      this.getFolderResources(folder)
    },
    leaveFolder() {
      this.currentFolder = null
      this.folderResources = []
    },
    getFolderResources(folder) {
      if (!folder || !folder.folderId) {
        this.folderResources = []
        return
      }
      this.resourceLoading = true
      Promise.all([
        listCommonResource({ folderId: folder.folderId, pageNum: 1, pageSize: 999 }),
        listTaskResource({ folderId: folder.folderId, pageNum: 1, pageSize: 999 })
      ]).then(([commonResponse, taskResponse]) => {
        const commonRows = (commonResponse.rows || []).map(item => ({
          resourceType: 'common',
          resourceId: item.resourceId,
          displayName: item.documentName || item.fileName || '--',
          folderName: item.folderName || folder.folderName,
          status: item.storageStatus || 'processing',
          statusText: this.commonStatusLabel(item.storageStatus),
          progressText: item.progressText,
          displayTime: item.latestModifyTime || item.updateTime || item.createTime,
          fileUrl: item.fileUrl
        }))
        const taskRows = (taskResponse.rows || []).map(item => ({
          resourceType: 'task',
          resourceId: item.resourceId,
          displayName: item.fileName || '--',
          folderName: item.folderName || folder.folderName,
          statusText: this.taskStatusLabel(item.collectStatus),
          displayTime: item.archiveTime || item.updateTime || item.createTime,
          fileUrl: item.previewFileUrl
        }))
        this.folderResources = commonRows.concat(taskRows)
        this.resourceLoading = false
      }).catch(() => {
        this.folderResources = []
        this.resourceLoading = false
      })
    },
    commonStatusLabel(status) {
      if (status === 'processing') {
        return '入库中'
      }
      if (status === 'stored') {
        return '已入库'
      }
      if (status === 'failed') {
        return '入库失败'
      }
      return '入库中'
    },
    progressWidth(row) {
      if (row.status === 'stored') {
        return '100%'
      }
      if (row.status === 'failed') {
        return '70%'
      }
      return '30%'
    },
    taskStatusLabel(status) {
      if (status === 'archived') {
        return '已归集'
      }
      if (status === 'failed') {
        return '归集失败'
      }
      return status || '归集处理中'
    },
    openFile(url) {
      if (!url) {
        this.$modal.msgWarning('暂无可访问文件')
        return
      }
      window.open(encodeURI(url))
    },
    resetFileForm() {
      this.fileForm = {
        documentName: '',
        creator: this.$store.getters.name || 'admin',
        fileUrl: '',
        fileName: '',
        fileSize: '5MB'
      }
      this.resetForm('fileForm')
    },
    handleAddFile() {
      if (!this.currentFolder) {
        this.$modal.msgWarning('请先进入目标文件夹')
        return
      }
      this.resetFileForm()
      this.fileTitle = '添加文件'
      this.fileOpen = true
      this.$nextTick(() => {
        this.$refs.fileForm && this.$refs.fileForm.clearValidate()
      })
    },
    submitFileForm() {
      this.$refs.fileForm.validate(valid => {
        if (!valid) {
          return
        }
        const fileUrl = this.fileForm.fileUrl
        addCommonResource({
          documentName: this.fileForm.documentName,
          creator: this.fileForm.creator,
          fileUrl,
          fileName: this.getFileName(fileUrl),
          fileSize: this.fileForm.fileSize || '5MB',
          folderId: this.currentFolder.folderId,
          folderName: this.currentFolder.folderName,
          storageStatus: 'processing',
          progressText: '文本解析智能体解析中'
        }).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.fileOpen = false
          this.getFolderResources(this.currentFolder)
        })
      })
    },
    getFileName(url) {
      if (!url) {
        return ''
      }
      return url.substring(url.lastIndexOf('/') + 1)
    },
    handleVisibleScopeChange(values) {
      if (values.length > 1) {
        this.visibleScopeValues = [values[values.length - 1]]
      }
      this.form.visibleScope = this.visibleScopeValues[0] || ''
      if (this.$refs.form) {
        this.$refs.form.validateField('visibleScope')
      }
    },
    handleAdd() {
      this.reset()
      this.title = '新建文件库'
      this.open = true
      this.$nextTick(() => {
        this.$refs.form && this.$refs.form.clearValidate()
      })
    },
    handleCommand(row, command) {
      if (command === 'edit') {
        this.handleUpdate(row)
        return
      }
      if (command === 'delete') {
        this.handleDelete(row)
      }
    },
    handleUpdate(row) {
      this.reset()
      this.form = {
        folderId: row.folderId,
        folderName: row.folderName,
        intro: row.intro,
        visibleScope: row.visibleScope || 'all',
        topFlag: row.topFlag || '0'
      }
      this.visibleScopeValues = [this.form.visibleScope]
      this.title = '编辑文件库'
      this.open = true
      this.$nextTick(() => {
        this.$refs.form && this.$refs.form.clearValidate()
      })
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除文件库“' + row.folderName + '”？').then(() => {
        return delLibraryFolder(row.folderId)
      }).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      }).catch(() => {})
    },
    reset() {
      this.form = {
        folderId: undefined,
        folderName: '',
        intro: '',
        visibleScope: 'all',
        topFlag: '0'
      }
      this.visibleScopeValues = ['all']
      this.resetForm('form')
    },
    submitForm() {
      this.form.visibleScope = this.visibleScopeValues[0] || ''
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        const request = this.form.folderId ? updateLibraryFolder(this.form) : addLibraryFolder(this.form)
        request.then(() => {
          this.$modal.msgSuccess(this.form.folderId ? '修改成功' : '新增成功')
          this.open = false
          this.getList()
        })
      })
    }
  }
}
</script>

<style scoped lang="scss">
.audit-folder-page {
  min-height: calc(100vh - 96px);
  background: #f2f3f5;
}

.library-panel {
  min-height: calc(100vh - 145px);
  background: #fff;
  border-radius: 4px;
  padding: 16px 18px 28px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 2px 0 14px;
}

.section-head:not(.section-head-top) {
  margin-top: 26px;
}

.section-title {
  font-size: 24px;
  line-height: 34px;
  font-weight: 700;
  color: #2f3440;
}

.folder-head-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.folder-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 22px;
}

.top-grid {
  min-height: 212px;
}

.folder-card {
  position: relative;
  width: 186px;
  height: 184px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.folder-card:hover {
  border-color: #d6e0ef;
  box-shadow: 0 7px 18px rgba(34, 93, 173, 0.08);
}

.folder-settings {
  position: absolute;
  top: 13px;
  right: 11px;
  color: #adb3bf;
  cursor: pointer;
  font-size: 18px;
}

.folder-graphic {
  position: relative;
  width: 104px;
  height: 78px;
  margin: 46px auto 0;
}

.folder-top {
  position: absolute;
  top: -10px;
  left: 9px;
  width: 40px;
  height: 13px;
  border-radius: 6px 6px 0 0;
  background: linear-gradient(180deg, #ffe060 0%, #f4c400 100%);
  border: 1px solid rgba(228, 167, 0, 0.32);
  border-bottom: none;
}

.folder-line {
  position: absolute;
  top: 2px;
  left: 8px;
  right: 8px;
  height: 5px;
  border-radius: 3px;
  background: rgba(255, 251, 220, 0.62);
  z-index: 2;
}

.folder-body {
  display: block;
  position: absolute;
  inset: 0;
  border-radius: 9px;
  background: linear-gradient(180deg, #ffd638 0%, #f2be01 100%);
  border: 1px solid rgba(223, 160, 0, 0.28);
  box-shadow: inset 0 -5px 0 rgba(255, 170, 0, 0.18);
}

.folder-caption {
  margin-top: 30px;
  padding: 0 12px;
  text-align: center;
  color: #5d6472;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.folder-content {
  margin-top: 8px;
}

.breadcrumb-row {
  display: flex;
  align-items: center;
  min-height: 38px;
  margin-bottom: 12px;
}

.breadcrumb-link {
  color: #409eff;
  cursor: pointer;
}

.folder-resource-table {
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.folder-resource-table ::v-deep th.el-table__cell {
  background: #f6f8fb;
  color: #606266;
  font-weight: 600;
}

.folder-resource-table ::v-deep .el-table__cell {
  padding-top: 10px;
  padding-bottom: 10px;
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

.visible-scope-group .el-checkbox {
  margin-right: 48px;
}

::v-deep .folder-dialog {
  border-radius: 10px;
  overflow: hidden;
}

::v-deep .folder-dialog .el-dialog__header {
  padding: 20px 22px 16px;
  border-bottom: 1px solid #ebeef3;
}

::v-deep .folder-dialog .el-dialog__title {
  color: #303642;
  font-size: 22px;
  font-weight: 600;
}

::v-deep .folder-dialog .el-dialog__body {
  padding: 18px 22px 12px;
}

::v-deep .folder-dialog .el-dialog__footer {
  border-top: 1px solid #ebeef3;
  padding: 12px 22px 14px;
}

::v-deep .folder-dialog .el-textarea__inner {
  min-height: 92px !important;
  resize: none;
}

::v-deep .folder-dialog .el-checkbox__label {
  font-size: 14px;
  color: #434a57;
}

::v-deep .folder-dialog .el-input__inner,
::v-deep .folder-dialog .el-textarea__inner {
  font-size: 14px;
}

::v-deep .folder-dialog .el-button {
  min-width: 96px;
}

@media (max-width: 1600px) {
  .section-title {
    font-size: 20px;
    line-height: 30px;
  }
}
</style>
