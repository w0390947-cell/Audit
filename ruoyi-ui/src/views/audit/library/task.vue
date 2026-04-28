<template>
  <div class="app-container audit-task-page">
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
      <el-form-item label="归属文件库" prop="folderId">
        <el-select v-model="queryParams.folderId" clearable placeholder="请选择归属文件库" style="width: 240px">
          <el-option
            v-for="item in folderOptions"
            :key="item.folderId"
            :label="item.folderName"
            :value="item.folderId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="文件采集状态" prop="collectStatus">
        <el-select v-model="queryParams.collectStatus" clearable placeholder="请选择文件采集状态" style="width: 240px">
          <el-option
            v-for="dict in dict.type.audit_task_collect_status"
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
          range-separator="至"
          start-placeholder="请选择日期"
          end-placeholder="请选择日期"
          style="width: 290px"
          type="daterange"
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
        <el-button
          plain
          size="small"
          icon="el-icon-download"
          @click="handleExport"
          v-hasPermi="['audit:library:task:export']"
        >
          批量导出
        </el-button>
      </div>

      <el-table v-loading="loading" :data="resourceList" class="task-table">
        <el-table-column type="selection" width="52" align="center" />
        <el-table-column label="文件编号" align="left" prop="fileNo" min-width="130" />
        <el-table-column label="文件名称" align="left" prop="fileName" min-width="130" />
        <el-table-column label="归档时间" align="center" prop="archiveTime" width="180">
          <template slot-scope="scope">
            {{ parseTime(scope.row.archiveTime) }}
          </template>
        </el-table-column>
        <el-table-column label="归属文件库" align="left" prop="folderName" min-width="150">
          <template slot-scope="scope">
            {{ scope.row.folderName || '未归属文件库' }}
          </template>
        </el-table-column>
        <el-table-column label="文件采集状态" align="center" prop="collectStatus" width="130">
          <template slot-scope="scope">
            <el-tag :class="['status-pill', scope.row.collectStatus]" disable-transitions>
              {{ collectStatusLabel(scope.row.collectStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="left" min-width="230" class-name="small-padding">
          <template slot-scope="scope">
            <el-button size="mini" type="text" @click="openFile(scope.row.previewFileUrl)">预览文件</el-button>
            <el-button
              size="mini"
              type="text"
              @click="handleReupload(scope.row)"
              v-hasPermi="['audit:library:task:edit']"
            >
              重新上传
            </el-button>
            <el-button
              size="mini"
              type="text"
              class="delete-btn"
              @click="handleDelete(scope.row)"
              v-hasPermi="['audit:library:task:remove']"
            >
              删除
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

    <el-dialog title="任务文件重新上传" :visible.sync="open" width="720px" append-to-body custom-class="task-upload-dialog">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="文件编号">
          <span>{{ form.fileNo }}</span>
        </el-form-item>
        <el-form-item label="文件名称" prop="fileName">
          <el-input v-model="form.fileName" placeholder="请输入文件名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="上传文件" prop="previewFileUrl">
          <div class="drag-upload-panel">
            <FileUpload
              v-model="form.previewFileUrl"
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
        <el-button @click="open = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import FileUpload from '@/components/FileUpload'
import {
  delTaskResource,
  listLibraryFolderOptions,
  listTaskResource,
  reuploadTaskResource
} from '@/api/audit/library'

export default {
  name: 'AuditLibraryTask',
  components: { FileUpload },
  dicts: ['audit_task_collect_status'],
  data() {
    return {
      loading: false,
      total: 0,
      open: false,
      dateRange: [],
      resourceList: [],
      folderOptions: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        keyword: undefined,
        folderId: undefined,
        collectStatus: undefined
      },
      form: {
        resourceId: undefined,
        fileNo: '',
        fileName: '',
        folderId: undefined,
        folderName: '',
        previewFileUrl: ''
      },
      rules: {
        fileName: [
          { required: true, message: '文件名称不能为空', trigger: 'blur' }
        ],
        previewFileUrl: [
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
    collectStatusLabel(status) {
      if (status === 'archived') {
        return '已归集'
      }
      if (status === 'failed') {
        return '归集失败'
      }
      return '归集处理中'
    },
    getList() {
      this.loading = true
      listTaskResource(this.addDateRange({ ...this.queryParams }, this.dateRange, 'ArchiveTime')).then(response => {
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
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.dateRange = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleReupload(row) {
      this.form = {
        resourceId: row.resourceId,
        fileNo: row.fileNo,
        fileName: row.fileName,
        folderId: row.folderId,
        folderName: row.folderName,
        previewFileUrl: row.previewFileUrl || ''
      }
      this.open = true
      this.$nextTick(() => {
        this.$refs.form && this.$refs.form.clearValidate()
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        const fileUrl = this.form.previewFileUrl
        reuploadTaskResource({
          resourceId: this.form.resourceId,
          fileNo: this.form.fileNo,
          fileName: this.form.fileName || this.getFileName(fileUrl),
          folderId: this.form.folderId,
          folderName: this.form.folderName,
          previewFileUrl: fileUrl
        }).then(() => {
          this.$modal.msgSuccess('重新上传成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除任务文件“' + row.fileName + '”？').then(() => {
        return delTaskResource(row.resourceId)
      }).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      }).catch(() => {})
    },
    handleExport() {
      this.download(
        'audit/library/task/export',
        this.addDateRange({ ...this.queryParams }, this.dateRange, 'ArchiveTime'),
        '任务文件资源_' + this.parseTime(new Date(), '{y}{m}{d}{h}{i}{s}') + '.xlsx'
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
.audit-task-page {
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

.toolbar-row {
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

::v-deep .task-table th.el-table__cell {
  background: #f2f4f8;
  color: #6b7280;
  font-weight: 600;
}

::v-deep .task-table .el-table__cell {
  padding-top: 10px;
  padding-bottom: 10px;
}

::v-deep .task-table .el-button--text {
  color: #4b92e8;
  font-size: 13px;
  padding: 0;
  margin-right: 12px;
}

::v-deep .task-table .el-button--text:last-child {
  margin-right: 0;
}

.status-pill {
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  padding: 0 10px;
  border: 1px solid transparent;
}

.status-pill.processing {
  background: #eaf3ff;
  border-color: #dbe9ff;
  color: #5c96ec;
}

.status-pill.archived {
  background: #eaf8eb;
  border-color: #d4f0d8;
  color: #58bf74;
}

.status-pill.failed {
  background: #fdeeed;
  border-color: #fadad7;
  color: #ef6d68;
}

.delete-btn {
  color: #f56c6c !important;
}

::v-deep .task-upload-dialog .el-dialog__body {
  max-height: calc(100vh - 190px);
  overflow-y: auto;
  padding: 12px 20px 4px !important;
}

::v-deep .task-upload-dialog .el-dialog__footer {
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
</style>
