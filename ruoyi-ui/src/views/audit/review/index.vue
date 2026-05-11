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
      <el-form-item label="审核状态" prop="reviewStatus">
        <el-select v-model="queryParams.reviewStatus" clearable placeholder="请选择审核状态" style="width: 220px">
          <el-option
            v-for="dict in dict.type.audit_review_status"
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
          size="small"
          type="danger"
          plain
          @click="handleDelete()"
          :disabled="multiple"
          v-hasPermi="['audit:review:remove']"
        >批量删除</el-button>
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

    <el-table
      v-loading="loading"
      :data="reviewList"
      class="review-table"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="任务编号" align="center" prop="taskNo" min-width="150" />
      <el-table-column label="产品名称" align="center" prop="productName" min-width="120" />
      <el-table-column label="送检单位" align="center" prop="deliveryUnit" min-width="130" />
      <el-table-column label="上传时间" align="center" prop="submitTime" min-width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.submitTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="发起人" align="center" prop="sponsor" width="110" />
      <el-table-column label="优先级" align="center" prop="priority" width="110">
        <template slot-scope="scope">
          <span :class="['priority-text', 'priority-' + scope.row.priority]">{{ priorityLabel(scope.row.priority) }}</span>
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

    <el-dialog
      title="从审核文件库选择"
      :visible.sync="libraryOpen"
      width="880px"
      append-to-body
      class="library-picker-dialog"
    >
      <div class="library-picker" v-loading="libraryLoading">
        <div class="picker-head">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>
              <span
                :class="{ 'breadcrumb-link': currentLibraryFolder }"
                @click="leaveLibraryFolder"
              >审核文件库</span>
            </el-breadcrumb-item>
            <el-breadcrumb-item v-for="(item, index) in libraryFolderPath" :key="item.folderId">
              <span
                :class="{ 'breadcrumb-link': index < libraryFolderPath.length - 1 }"
                @click="navigateLibraryFolder(index)"
              >{{ item.folderName }}</span>
            </el-breadcrumb-item>
          </el-breadcrumb>
          <div class="selected-count">已选择 {{ selectedLibraryFileList.length }} 个文件</div>
        </div>

        <div class="library-search-bar">
          <el-input
            v-model.trim="libraryKeyword"
            clearable
            prefix-icon="el-icon-search"
            :placeholder="currentLibraryFolder ? '请输入文件名称搜索' : '请输入文件名称全局搜索'"
          />
        </div>

        <div v-if="!currentLibraryFolder && !libraryKeyword && libraryFolders.length" class="picker-folder-grid picker-child-folder-grid">
          <div
            v-for="item in libraryFolders"
            :key="item.folderId"
            class="picker-folder-card"
            @click="enterLibraryFolder(item)"
          >
            <div class="picker-folder-icon">
              <span class="folder-top" />
              <span class="folder-line" />
              <span class="folder-body" />
            </div>
            <div class="picker-folder-name">{{ item.folderName }}</div>
            <div class="picker-folder-count">{{ item.fileCount || 0 }} 个项目</div>
          </div>
        </div>

        <div>
          <div v-if="currentLibraryFolder && !libraryKeyword && libraryChildFolders.length" class="picker-folder-grid picker-child-folder-grid">
            <div
              v-for="item in libraryChildFolders"
              :key="item.folderId"
              class="picker-folder-card"
              @click="enterLibraryFolder(item)"
            >
              <div class="picker-folder-icon">
                <span class="folder-top" />
                <span class="folder-line" />
                <span class="folder-body" />
              </div>
              <div class="picker-folder-name">{{ item.folderName }}</div>
              <div class="picker-folder-count">{{ item.fileCount || 0 }} 个项目</div>
            </div>
          </div>
          <el-table
            ref="libraryResourceTable"
            v-loading="resourceLoading"
            :data="filteredLibraryResources"
            row-key="pickerKey"
            class="library-resource-table"
            @select="handleLibrarySelect"
            @select-all="handleLibrarySelectAll"
          >
            <el-table-column type="selection" width="55" align="center" :selectable="isLibraryFileSelectable" />
            <el-table-column label="文件名称" prop="displayName" min-width="200" show-overflow-tooltip />
            <el-table-column label="文件大小" prop="fileSize" width="90" align="center">
              <template slot-scope="scope">{{ scope.row.fileSize || '--' }}</template>
            </el-table-column>
            <el-table-column label="创建者" prop="creatorName" width="100" align="center">
              <template slot-scope="scope">{{ scope.row.creatorName || '--' }}</template>
            </el-table-column>
            <el-table-column label="更新时间" width="150" align="center">
              <template slot-scope="scope">{{ parseTime(scope.row.displayTime) || '--' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template slot-scope="scope">
                <el-button type="text" size="mini" :disabled="!scope.row.fileUrl" @click="openLibraryFile(scope.row.fileUrl)">预览</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty
            v-if="!resourceLoading && !filteredLibraryResources.length && (libraryKeyword || (!libraryFolders.length && !libraryChildFolders.length))"
            :description="libraryKeyword ? '未找到匹配文件' : '当前目录暂无文件'"
            :image-size="100"
          />
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="libraryOpen = false">取消</el-button>
        <el-button type="primary" @click="confirmLibrarySelection">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import FileUpload from '@/components/FileUpload'
import { ensureAiTaskByReviewTask } from '@/api/audit/ai'
import { listCommonResource, listLibraryFolders, listTaskResource } from '@/api/audit/library'
import {
  addReview,
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
      libraryOpen: false,
      libraryLoading: false,
      resourceLoading: false,
      title: '',
      historyList: [],
      libraryFolders: [],
      libraryChildFolders: [],
      libraryFolderPath: [],
      currentLibraryFolder: null,
      libraryResources: [],
      libraryKeyword: '',
      libraryGlobalLoaded: false,
      selectedLibraryFiles: {},
      ids: [],
      multiple: true,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        keyword: undefined,
        reviewStatus: undefined
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
        mainReportUrls: []
      }
    }
  },
  computed: {
    selectedLibraryFileList() {
      return Object.values(this.selectedLibraryFiles)
    },
    filteredLibraryResources() {
      const keyword = this.libraryKeyword.trim().toLowerCase()
      if (!keyword) {
        return this.libraryResources
      }
      return this.libraryResources.filter(item => {
        const name = String(item.displayName || item.fileName || '').toLowerCase()
        return name.includes(keyword)
      })
    }
  },
  watch: {
    libraryKeyword() {
      if (!this.currentLibraryFolder && this.libraryKeyword && !this.libraryGlobalLoaded && !this.resourceLoading) {
        this.getLibraryResources()
      }
      this.$nextTick(this.syncLibraryTableSelection)
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
        reviewStatus: 'reviewing',
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
      ensureAiTaskByReviewTask(row.taskId).then(response => {
        const aiTask = response.data || {}
        if (!aiTask.aiTaskId) {
          this.$modal.msgWarning('未获取到对应AI任务')
          return
        }
        this.$router.push({
          path: '/audit-ai/detail/' + aiTask.aiTaskId
        })
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
      ensureAiTaskByReviewTask(row.taskId, row.versionId).then(response => {
        const aiTask = response.data || {}
        if (!aiTask.aiTaskId) {
          this.$modal.msgWarning('未获取到对应AI任务')
          return
        }
        this.historyOpen = false
        this.$router.push({
          path: '/audit-ai/detail/' + aiTask.aiTaskId
        })
      })
    },
    handleSelectFromLibrary() {
      this.libraryOpen = true
      this.currentLibraryFolder = null
      this.libraryResources = []
      this.libraryChildFolders = []
      this.libraryFolderPath = []
      this.libraryKeyword = ''
      this.libraryGlobalLoaded = false
      this.selectedLibraryFiles = {}
      this.getLibraryFolders()
      this.getLibraryResources(null, true)
    },
    getLibraryFolders() {
      this.libraryLoading = true
      listLibraryFolders({ parentId: 0 }).then(response => {
        this.libraryFolders = response.data || []
        this.libraryLoading = false
      }).catch(() => {
        this.libraryFolders = []
        this.libraryLoading = false
      })
    },
    enterLibraryFolder(folder) {
      this.currentLibraryFolder = folder
      this.libraryFolderPath = this.libraryFolderPath.concat(folder)
      this.libraryKeyword = ''
      this.libraryGlobalLoaded = false
      this.libraryChildFolders = []
      this.getLibraryChildFolders(folder)
      this.getLibraryResources(folder)
    },
    leaveLibraryFolder() {
      if (!this.currentLibraryFolder) {
        return
      }
      this.currentLibraryFolder = null
      this.libraryResources = []
      this.libraryChildFolders = []
      this.libraryFolderPath = []
      this.libraryKeyword = ''
      this.libraryGlobalLoaded = false
      this.getLibraryResources(null, true)
    },
    navigateLibraryFolder(index) {
      if (index < 0 || index >= this.libraryFolderPath.length - 1) {
        return
      }
      const nextPath = this.libraryFolderPath.slice(0, index + 1)
      const folder = nextPath[nextPath.length - 1]
      this.libraryFolderPath = nextPath
      this.currentLibraryFolder = folder
      this.libraryKeyword = ''
      this.libraryGlobalLoaded = false
      this.libraryChildFolders = []
      this.getLibraryChildFolders(folder)
      this.getLibraryResources(folder)
    },
    getLibraryChildFolders(folder) {
      if (!folder || !folder.folderId) {
        this.libraryChildFolders = []
        return
      }
      listLibraryFolders({ parentId: folder.folderId }).then(response => {
        this.libraryChildFolders = response.data || []
      }).catch(() => {
        this.libraryChildFolders = []
      })
    },
    getLibraryResources(folder, rootOnly) {
      const queryParams = { pageNum: 1, pageSize: 999 }
      if (folder && folder.folderId) {
        queryParams.folderId = folder.folderId
      } else if (rootOnly) {
        queryParams.folderId = 0
      }
      this.resourceLoading = true
      Promise.all([
        listCommonResource(queryParams),
        listTaskResource(queryParams)
      ]).then(([commonResponse, taskResponse]) => {
        const commonRows = (commonResponse.rows || []).map(item => ({
          pickerKey: 'common_' + item.resourceId,
          resourceType: 'common',
          resourceId: item.resourceId,
          displayName: item.documentName || item.fileName || this.getFileNameFromUrl(item.fileUrl),
          fileName: item.fileName || item.documentName || this.getFileNameFromUrl(item.fileUrl),
          folderName: item.folderName,
          fileUrl: item.fileUrl,
          fileSize: item.fileSize,
          creatorName: item.creator || item.createBy,
          displayTime: item.latestModifyTime || item.updateTime || item.createTime
        }))
        const taskRows = (taskResponse.rows || []).map(item => ({
          pickerKey: 'task_' + item.resourceId,
          resourceType: 'task',
          resourceId: item.resourceId,
          displayName: item.fileName || this.getFileNameFromUrl(item.previewFileUrl),
          fileName: item.fileName || this.getFileNameFromUrl(item.previewFileUrl),
          folderName: item.folderName,
          fileUrl: item.previewFileUrl,
          fileSize: '',
          creatorName: item.creator || item.createBy,
          displayTime: item.archiveTime || item.updateTime || item.createTime
        }))
        this.libraryResources = commonRows.concat(taskRows)
        this.libraryGlobalLoaded = !(folder && folder.folderId)
        this.resourceLoading = false
        this.$nextTick(this.syncLibraryTableSelection)
      }).catch(() => {
        this.libraryResources = []
        this.libraryGlobalLoaded = !(folder && folder.folderId)
        this.resourceLoading = false
      })
    },
    handleLibrarySelect(selection, row) {
      if (!row || !row.fileUrl) {
        return
      }
      if (selection.some(item => item.pickerKey === row.pickerKey)) {
        this.$set(this.selectedLibraryFiles, row.pickerKey, row)
      } else {
        this.$delete(this.selectedLibraryFiles, row.pickerKey)
      }
    },
    handleLibrarySelectAll(selection) {
      const selectionKeys = selection.map(item => item.pickerKey)
      this.filteredLibraryResources.forEach(row => {
        if (!row.fileUrl) {
          return
        }
        if (selectionKeys.includes(row.pickerKey)) {
          this.$set(this.selectedLibraryFiles, row.pickerKey, row)
        } else {
          this.$delete(this.selectedLibraryFiles, row.pickerKey)
        }
      })
    },
    syncLibraryTableSelection() {
      if (!this.$refs.libraryResourceTable) {
        return
      }
      this.$refs.libraryResourceTable.clearSelection()
      this.filteredLibraryResources.forEach(row => {
        if (this.selectedLibraryFiles[row.pickerKey]) {
          this.$refs.libraryResourceTable.toggleRowSelection(row, true)
        }
      })
    },
    isLibraryFileSelectable(row) {
      return !!(row && row.fileUrl)
    },
    confirmLibrarySelection() {
      const selectedUrls = this.selectedLibraryFileList.map(item => item.fileUrl).filter(Boolean)
      if (!selectedUrls.length) {
        this.$message.warning('请选择文件')
        return
      }
      const currentUrls = this.splitFileUrls(this.form.basisFileUrls)
      const mergedUrls = Array.from(new Set(currentUrls.concat(selectedUrls)))
      if (mergedUrls.length > 8) {
        this.$message.warning('依据文件数量不能超过 8 个')
        return
      }
      this.form.basisFileUrls = mergedUrls.join(',')
      this.libraryOpen = false
    },
    splitFileUrls(fileUrls) {
      if (!fileUrls) {
        return []
      }
      return String(fileUrls).split(',').map(item => item.trim()).filter(Boolean)
    },
    getFileNameFromUrl(url) {
      if (!url) {
        return '--'
      }
      const cleanUrl = String(url).split('?')[0]
      return decodeURIComponent(cleanUrl.substring(cleanUrl.lastIndexOf('/') + 1)) || cleanUrl
    },
    commonStatusLabel(status) {
      if (status === 'stored') {
        return '已入库'
      }
      if (status === 'failed') {
        return '入库失败'
      }
      return '入库中'
    },
    taskStatusLabel(status) {
      if (status === 'archived') {
        return '已归档'
      }
      if (status === 'pending') {
        return '待采集'
      }
      return '已采集'
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
    openLibraryFile(fileUrl) {
      if (fileUrl) {
        window.open(encodeURI(fileUrl))
      }
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.taskId)
      this.multiple = !selection.length
    },
    handleDelete(row) {
      const taskIds = row ? [row.taskId] : this.ids
      if (!taskIds.length) {
        this.$message.warning('请先选择任务')
        return
      }
      const message = row ? '确认要删除任务编号为“' + row.taskNo + '”的数据项吗？' : '是否确认删除所选审核任务？'
      this.$modal.confirm(message).then(() => {
        return delReview(taskIds.join(','))
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

.library-picker {
  min-height: 420px;
}

.library-picker-dialog ::v-deep .el-dialog__body {
  padding: 16px 20px 8px !important;
}

.picker-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 12px 14px;
  margin-bottom: 14px;
}

.breadcrumb-link {
  color: #409eff;
  cursor: pointer;
}

.selected-count {
  color: #606266;
  font-size: 13px;
}

.picker-folder-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 14px;
}

.picker-child-folder-grid {
  margin-bottom: 14px;
}

.picker-folder-card {
  height: 150px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.picker-folder-card:hover {
  border-color: #c6e2ff;
  background: #f5faff;
}

.picker-folder-icon {
  position: relative;
  width: 78px;
  height: 58px;
  margin-bottom: 12px;
}

.folder-top {
  position: absolute;
  left: 6px;
  top: 8px;
  width: 28px;
  height: 11px;
  background: #f4c45f;
  border-radius: 4px 4px 0 0;
}

.folder-line {
  position: absolute;
  left: 2px;
  right: 2px;
  top: 18px;
  height: 9px;
  background: #ffd773;
  border-radius: 4px 4px 0 0;
}

.folder-body {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 38px;
  background: #f6c453;
  border-radius: 4px;
  box-shadow: inset 0 -6px 0 rgba(0, 0, 0, 0.04);
}

.picker-folder-name {
  max-width: 120px;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.picker-folder-count {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
}

.library-search-bar {
  margin-bottom: 12px;
  width: 320px;
}

.library-resource-table {
  border: 1px solid #ebeef5;
}

.library-resource-table ::v-deep th.el-table__cell {
  background: #f5f7fa;
  color: #606266;
  font-weight: 500;
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
