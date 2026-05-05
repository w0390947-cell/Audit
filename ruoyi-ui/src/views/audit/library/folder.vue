<template>
  <div class="app-container audit-folder-page">
    <el-form ref="queryForm" :model="queryParams" :inline="true" size="small" class="query-form">
      <el-form-item prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          clearable
          placeholder="请输入文件夹或文件名称"
          style="width: 300px"
          @keyup.enter.native="handleQuery"
        >
          <i slot="prefix" class="el-input__icon el-icon-search" />
        </el-input>
      </el-form-item>
      <el-form-item label="文件入库状态" prop="storageStatus">
        <el-select v-model="queryParams.storageStatus" clearable placeholder="请选择文件入库状态" style="width: 220px">
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
          v-model="dateRange"
          clearable
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
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

    <div class="file-manager" v-loading="loading">
      <div class="manager-toolbar">
        <div class="path-area">
          <div class="page-title">{{ searchMode ? '搜索结果' : '审核文件库' }}</div>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>
              <span class="breadcrumb-link" @click="goRoot">全部文件</span>
            </el-breadcrumb-item>
            <el-breadcrumb-item v-if="searchMode">搜索结果</el-breadcrumb-item>
            <el-breadcrumb-item v-for="(item, index) in folderPath" v-else :key="item.folderId">
              <span
                v-if="index < folderPath.length - 1"
                class="breadcrumb-link"
                @click="switchFolderPath(index)"
              >{{ item.folderName }}</span>
              <span v-else>{{ item.folderName }}</span>
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="toolbar-actions">
          <el-radio-group v-model="viewMode" size="small" class="view-switch">
            <el-radio-button label="table">
              <i class="el-icon-s-grid" />
              表格视图
            </el-radio-button>
            <el-radio-button label="grid">
              <i class="el-icon-menu" />
              网格视图
            </el-radio-button>
          </el-radio-group>
          <el-button
            icon="el-icon-folder-add"
            size="small"
            @click="handleAddFolder"
            v-hasPermi="['audit:library:folder:add']"
          >
            新建文件夹
          </el-button>
          <el-button
            type="primary"
            icon="el-icon-upload2"
            size="small"
            @click="handleAddFile"
            v-hasPermi="['audit:library:common:add']"
          >
            上传文件
          </el-button>
          <el-button
            v-if="searchMode || currentFolder"
            icon="el-icon-back"
            size="small"
            @click="goBack"
          >
            返回
          </el-button>
        </div>
      </div>

      <div v-if="searchMode" class="search-summary">共 {{ managerItems.length }} 条结果</div>

      <el-table
        v-if="managerItems.length && viewMode === 'table'"
        :data="managerItems"
        class="manager-table"
        @row-dblclick="handleOpenItem"
      >
        <el-table-column label="名称" prop="displayName" min-width="260" show-overflow-tooltip>
          <template slot-scope="scope">
            <div class="table-name-cell" @click="handleOpenItem(scope.row)">
              <div v-if="scope.row.resourceType === 'folder'" class="table-folder-icon">
                <span class="folder-tab" />
                <span class="folder-body" />
              </div>
              <div v-else :class="['file-type-icon', 'file-type-icon--table', getFileTypeClass(scope.row)]">
                <span>{{ getFileTypeLabel(scope.row) }}</span>
              </div>
              <span>{{ scope.row.displayName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="130" align="center">
          <template slot-scope="scope">
            <dict-tag
              v-if="scope.row.resourceType === 'common'"
              :options="dict.type.audit_file_storage_status"
              :value="scope.row.status"
            />
            <span v-else>{{ scope.row.resourceType === 'task' ? scope.row.metaText : '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="进度" min-width="220" align="left">
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
        <el-table-column label="文件大小" width="110" align="center">
          <template slot-scope="scope">
            {{ scope.row.resourceType === 'common' ? (scope.row.fileSize || '--') : '--' }}
          </template>
        </el-table-column>
        <el-table-column label="创建者" width="120" align="center">
          <template slot-scope="scope">
            {{ getCreator(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="170" align="center">
          <template slot-scope="scope">
            {{ parseTime(scope.row.displayTime) || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="left" class-name="small-padding">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="text"
              @click="handleOpenItem(scope.row)"
            >
              {{ scope.row.resourceType === 'folder' ? '打开' : '预览' }}
            </el-button>
            <el-button
              v-if="scope.row.resourceType === 'folder'"
              size="mini"
              type="text"
              @click="handleRename(scope.row)"
              v-hasPermi="['audit:library:folder:edit']"
            >
              编辑
            </el-button>
            <el-button
              v-if="scope.row.resourceType === 'common'"
              size="mini"
              type="text"
              @click="handleRename(scope.row)"
              v-hasPermi="['audit:library:common:edit']"
            >
              编辑
            </el-button>
            <el-button
              v-if="scope.row.resourceType !== 'folder'"
              size="mini"
              type="text"
              @click="handleDownload(scope.row)"
            >
              下载
            </el-button>
            <el-button size="mini" type="text" @click="handleMove(scope.row)">移动到</el-button>
            <el-button
              v-if="scope.row.resourceType === 'common'"
              size="mini"
              type="text"
              @click="handleVersion(scope.row)"
            >
              历史版本
            </el-button>
            <el-button size="mini" type="text" class="delete-btn" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="managerItems.length && viewMode === 'grid'" class="manager-grid">
        <div
          v-for="item in managerItems"
          :key="item.itemKey"
          :class="['manager-item', item.resourceType]"
          @dblclick="handleOpenItem(item)"
        >
          <div class="item-menu" @click.stop>
            <el-dropdown trigger="click" @command="handleItemCommand(item, $event)">
              <i class="el-icon-more" />
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item v-if="item.resourceType === 'folder'" command="open">打开</el-dropdown-item>
                <el-dropdown-item v-if="item.resourceType !== 'folder'" command="preview">预览</el-dropdown-item>
                <el-dropdown-item v-if="item.resourceType !== 'folder'" command="download">下载</el-dropdown-item>
                <el-dropdown-item v-if="item.resourceType === 'folder'" command="rename" v-hasPermi="['audit:library:folder:edit']">编辑</el-dropdown-item>
                <el-dropdown-item v-if="item.resourceType === 'common'" command="rename" v-hasPermi="['audit:library:common:edit']">编辑</el-dropdown-item>
                <el-dropdown-item command="move">移动到</el-dropdown-item>
                <el-dropdown-item v-if="item.resourceType === 'common'" command="version">历史版本</el-dropdown-item>
                <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
          <div class="item-icon" @click="handleOpenItem(item)">
            <div v-if="item.resourceType === 'folder'" class="folder-icon">
              <span class="folder-tab" />
              <span class="folder-body" />
            </div>
            <div v-else :class="['file-type-icon', 'file-type-icon--grid', getFileTypeClass(item)]">
              <span>{{ getFileTypeLabel(item) }}</span>
            </div>
          </div>
          <div class="item-name" :title="item.displayName">{{ item.displayName }}</div>
          <div v-if="item.resourceType === 'folder'" class="item-meta">
            <span>{{ item.metaText }}</span>
          </div>
        </div>
      </div>
      <el-empty v-if="!managerItems.length" :description="searchMode ? '暂无匹配结果' : '当前目录暂无内容'" :image-size="110" />
    </div>

    <el-dialog :title="folderTitle" :visible.sync="folderOpen" width="520px" append-to-body>
      <el-form ref="folderForm" :model="folderForm" :rules="folderRules" label-width="86px">
        <el-form-item label="名称" prop="folderName">
          <el-input v-model="folderForm.folderName" placeholder="请输入文件夹名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="简介" prop="intro">
          <el-input v-model="folderForm.intro" type="textarea" :rows="4" placeholder="请输入简介" maxlength="500" />
        </el-form-item>
        <el-form-item label="可见范围" prop="visibleScope">
          <el-radio-group v-model="folderForm.visibleScope">
            <el-radio label="all">全员可见</el-radio>
            <el-radio label="admin">仅超级管理员可见</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="folderOpen = false">取消</el-button>
        <el-button type="primary" @click="submitFolderForm">确定</el-button>
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
              @upload-success="handleFileUploadSuccess"
            />
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="fileOpen = false">取消</el-button>
        <el-button type="primary" @click="submitFileForm">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="移动到" :visible.sync="moveOpen" width="560px" append-to-body>
      <el-form label-width="86px">
        <el-form-item label="目标目录">
          <el-select v-model="targetFolderId" filterable placeholder="请选择目标目录" style="width: 100%">
            <el-option :value="0" label="全部文件" />
            <el-option
              v-for="item in moveFolderOptions"
              :key="item.folderId"
              :label="item.pathName"
              :value="item.folderId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="moveOpen = false">取消</el-button>
        <el-button type="primary" @click="submitMove">确定</el-button>
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
            <span class="version-time">{{ parseTime(item.createTime) }}</span>
          </div>
          <div class="version-file">
            <span :class="['file-type-icon', 'file-type-icon--version', getFileTypeClass(item)]">
              <span>{{ getFileTypeLabel(item) }}</span>
            </span>
            <span>{{ item.fileName }}</span>
            <el-button type="text" @click="openFile(item.fileUrl)">预览</el-button>
          </div>
          <div class="version-meta">
            <span>创建者：{{ item.creator || '--' }}</span>
            <span>文件大小：{{ item.fileSize || '--' }}</span>
          </div>
        </div>
        <el-empty v-if="!versionList.length" description="暂无历史版本" :image-size="90" />
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="versionOpen = false">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import FileUpload from '@/components/FileUpload'
import { saveAs } from 'file-saver'
import {
  addLibraryFolder,
  addCommonResource,
  assignCommonResourceFolder,
  assignTaskResourceFolder,
  delCommonResource,
  delLibraryFolder,
  delTaskResource,
  getCommonResource,
  listCommonResource,
  listLibraryFolderOptions,
  listLibraryFolders,
  listTaskResource,
  updateCommonResource,
  updateLibraryFolder
} from '@/api/audit/library'

export default {
  name: 'AuditLibraryFolder',
  components: { FileUpload },
  dicts: ['audit_file_storage_status'],
  data() {
    return {
      loading: false,
      searchMode: false,
      viewMode: 'table',
      currentFolder: null,
      folderPath: [],
      folderItems: [],
      fileItems: [],
      allFolderList: [],
      folderOpen: false,
      fileOpen: false,
      moveOpen: false,
      versionOpen: false,
      folderTitle: '',
      fileTitle: '',
      movingItem: null,
      targetFolderId: 0,
      dateRange: [],
      queryParams: {
        keyword: '',
        storageStatus: undefined
      },
      folderForm: {
        folderId: undefined,
        parentId: 0,
        folderName: '',
        intro: '',
        visibleScope: 'all',
        topFlag: '0'
      },
      fileForm: {
        resourceId: undefined,
        documentName: '',
        creator: '',
        fileUrl: '',
        fileName: '',
        fileSize: '',
        folderId: 0,
        folderName: ''
      },
      versionList: [],
      currentVersionNo: '',
      folderRules: {
        folderName: [{ required: true, message: '文件夹名称不能为空', trigger: 'blur' }],
        visibleScope: [{ required: true, message: '可见范围不能为空', trigger: 'change' }]
      },
      fileRules: {
        documentName: [{ required: true, message: '文档名称不能为空', trigger: 'blur' }],
        creator: [{ required: true, message: '创建者不能为空', trigger: 'blur' }],
        fileUrl: [{ required: true, message: '请上传文件', trigger: 'change' }]
      }
    }
  },
  computed: {
    managerItems() {
      return this.folderItems.concat(this.fileItems)
    },
    currentFolderId() {
      return this.currentFolder ? this.currentFolder.folderId : 0
    },
    currentFolderName() {
      return this.currentFolder ? this.currentFolder.folderName : ''
    },
    moveFolderOptions() {
      const disabledIds = this.movingItem && this.movingItem.resourceType === 'folder'
        ? this.getDescendantIds(this.movingItem.folderId).concat([this.movingItem.folderId])
        : []
      return this.allFolderList
        .filter(item => !disabledIds.includes(item.folderId))
        .map(item => ({
          ...item,
          pathName: this.buildFolderPath(item).map(path => path.folderName).join(' / ')
        }))
    }
  },
  created() {
    this.refreshAll()
  },
  methods: {
    refreshAll() {
      this.getFolderOptions().then(() => {
        this.loadDirectory()
      })
    },
    getFolderOptions() {
      return listLibraryFolderOptions().then(response => {
        this.allFolderList = response.data || []
      })
    },
    loadDirectory() {
      this.loading = true
      const folderId = this.currentFolderId
      Promise.all([
        listLibraryFolders({ parentId: folderId }),
        listCommonResource({ folderId, pageNum: 1, pageSize: 999 }),
        listTaskResource({ folderId, pageNum: 1, pageSize: 999 })
      ]).then(([folderResponse, commonResponse, taskResponse]) => {
        this.folderItems = (folderResponse.data || [])
          .filter(item => this.isFolderInCurrentDirectory(item, folderId))
          .map(item => this.normalizeFolder(item))
        this.fileItems = (commonResponse.rows || []).map(item => this.normalizeCommonResource(item))
          .concat((taskResponse.rows || []).map(item => this.normalizeTaskResource(item)))
        this.loading = false
      }).catch(() => {
        this.folderItems = []
        this.fileItems = []
        this.loading = false
      })
    },
    handleQuery() {
      const keyword = (this.queryParams.keyword || '').trim()
      const hasResourceFilters = !!this.queryParams.storageStatus || (this.dateRange && this.dateRange.length)
      const folderRows = hasResourceFilters ? [] : this.allFolderList
        .filter(item => !keyword || (item.folderName || '').toLowerCase().includes(keyword.toLowerCase()))
        .map(item => this.normalizeFolder(item))
      const commonQuery = this.addDateRange({
        keyword,
        storageStatus: this.queryParams.storageStatus,
        pageNum: 1,
        pageSize: 999
      }, this.dateRange, 'LatestModifyTime')
      const taskQuery = this.addDateRange({
        keyword,
        pageNum: 1,
        pageSize: 999
      }, this.dateRange, 'ArchiveTime')
      this.loading = true
      Promise.all([
        listCommonResource(commonQuery),
        this.queryParams.storageStatus ? Promise.resolve({ rows: [] }) : listTaskResource(taskQuery)
      ]).then(([commonResponse, taskResponse]) => {
        this.searchMode = true
        this.folderPath = []
        this.currentFolder = null
        this.folderItems = folderRows
        this.fileItems = (commonResponse.rows || []).map(item => this.normalizeCommonResource(item))
          .concat((taskResponse.rows || []).map(item => this.normalizeTaskResource(item)))
        this.loading = false
      }).catch(() => {
        this.searchMode = true
        this.folderItems = folderRows
        this.fileItems = []
        this.loading = false
      })
    },
    resetQuery() {
      this.queryParams.keyword = ''
      this.queryParams.storageStatus = undefined
      this.dateRange = []
      this.resetForm('queryForm')
      this.goRoot()
    },
    normalizeFolder(item) {
      return {
        ...item,
        itemKey: 'folder_' + item.folderId,
        resourceType: 'folder',
        displayName: item.folderName,
        displayTime: item.updateTime || item.createTime,
        metaText: (item.fileCount || 0) + ' 项'
      }
    },
    isFolderInCurrentDirectory(item, folderId) {
      const parentId = item.parentId === undefined || item.parentId === null ? 0 : Number(item.parentId)
      return parentId === Number(folderId || 0)
    },
    normalizeCommonResource(item) {
      return {
        ...item,
        itemKey: 'common_' + item.resourceId,
        resourceType: 'common',
        displayName: item.documentName || item.fileName || '--',
        displayTime: item.latestModifyTime || item.updateTime || item.createTime,
        status: item.storageStatus || 'processing',
        fileUrl: item.fileUrl,
        metaText: item.fileSize || '文件'
      }
    },
    normalizeTaskResource(item) {
      return {
        ...item,
        itemKey: 'task_' + item.resourceId,
        resourceType: 'task',
        displayName: item.fileName || '--',
        displayTime: item.archiveTime || item.updateTime || item.createTime,
        fileUrl: item.previewFileUrl,
        metaText: this.taskStatusLabel(item.collectStatus)
      }
    },
    handleOpenItem(item) {
      if (item.resourceType === 'folder') {
        this.enterFolder(item)
      } else {
        this.openFile(item.fileUrl)
      }
    },
    handleItemCommand(item, command) {
      if (command === 'open') this.enterFolder(item)
      if (command === 'preview') this.openFile(item.fileUrl)
      if (command === 'download') this.handleDownload(item)
      if (command === 'rename') this.handleRename(item)
      if (command === 'move') this.handleMove(item)
      if (command === 'version') this.handleVersion(item)
      if (command === 'delete') this.handleDelete(item)
    },
    enterFolder(folder) {
      this.searchMode = false
      const realFolder = this.allFolderList.find(item => item.folderId === folder.folderId) || folder
      this.currentFolder = realFolder
      this.folderPath = this.buildFolderPath(realFolder)
      this.loadDirectory()
    },
    switchFolderPath(index) {
      this.enterFolder(this.folderPath[index])
    },
    goRoot() {
      this.searchMode = false
      this.currentFolder = null
      this.folderPath = []
      this.loadDirectory()
    },
    goBack() {
      if (this.searchMode) {
        this.goRoot()
        return
      }
      if (this.folderPath.length <= 1) {
        this.goRoot()
        return
      }
      this.enterFolder(this.folderPath[this.folderPath.length - 2])
    },
    buildFolderPath(folder) {
      const path = []
      let cursor = folder
      while (cursor && cursor.folderId) {
        path.unshift(cursor)
        if (!cursor.parentId) break
        cursor = this.allFolderList.find(item => item.folderId === cursor.parentId)
      }
      return path
    },
    getDescendantIds(folderId) {
      const ids = []
      const collect = parentId => {
        this.allFolderList
          .filter(item => item.parentId === parentId)
          .forEach(item => {
            ids.push(item.folderId)
            collect(item.folderId)
          })
      }
      collect(folderId)
      return ids
    },
    handleAddFolder() {
      this.folderForm = {
        folderId: undefined,
        parentId: this.currentFolderId,
        folderName: '',
        intro: '',
        visibleScope: 'all',
        topFlag: '0'
      }
      this.folderTitle = '新建文件夹'
      this.folderOpen = true
      this.$nextTick(() => this.$refs.folderForm && this.$refs.folderForm.clearValidate())
    },
    handleRename(item) {
      if (item.resourceType === 'folder') {
        this.folderForm = {
          folderId: item.folderId,
          parentId: item.parentId || 0,
          folderName: item.folderName,
          intro: item.intro || '',
          visibleScope: item.visibleScope || 'all',
          topFlag: item.topFlag || '0'
        }
        this.folderTitle = '重命名文件夹'
        this.folderOpen = true
        return
      }
      if (item.resourceType === 'common') {
        getCommonResource(item.resourceId).then(response => {
          const data = response.data || {}
          this.resetFileForm()
          this.fileForm = {
            resourceId: data.resourceId,
            documentName: data.documentName,
            creator: data.creator,
            fileUrl: data.fileUrl,
            fileName: data.fileName,
            fileSize: data.fileSize || '',
            folderId: data.folderId || 0,
            folderName: data.folderName || ''
          }
          this.fileTitle = '编辑文件'
          this.fileOpen = true
        })
      }
    },
    submitFolderForm() {
      this.$refs.folderForm.validate(valid => {
        if (!valid) return
        const request = this.folderForm.folderId ? updateLibraryFolder(this.folderForm) : addLibraryFolder(this.folderForm)
        request.then(() => {
          this.$modal.msgSuccess(this.folderForm.folderId ? '修改成功' : '新增成功')
          this.folderOpen = false
          this.refreshAll()
        })
      })
    },
    handleAddFile() {
      this.resetFileForm()
      this.fileTitle = '上传文件'
      this.fileOpen = true
      this.$nextTick(() => this.$refs.fileForm && this.$refs.fileForm.clearValidate())
    },
    resetFileForm() {
      this.fileForm = {
        resourceId: undefined,
        documentName: '',
        creator: this.$store.getters.name || 'admin',
        fileUrl: '',
        fileName: '',
        fileSize: '',
        folderId: this.currentFolderId,
        folderName: this.currentFolderName
      }
      this.resetForm('fileForm')
    },
    handleFileUploadSuccess(payload) {
      this.fileForm.fileSize = payload.fileSize || ''
    },
    submitFileForm() {
      this.$refs.fileForm.validate(valid => {
        if (!valid) return
        const fileUrl = this.fileForm.fileUrl
        const data = {
          resourceId: this.fileForm.resourceId,
          documentName: this.fileForm.documentName,
          creator: this.fileForm.creator,
          fileUrl,
          fileName: this.getFileName(fileUrl),
          fileSize: this.fileForm.fileSize || '',
          folderId: this.fileForm.folderId || 0,
          folderName: this.fileForm.folderName || '',
          storageStatus: this.fileForm.resourceId ? undefined : 'processing',
          progressText: this.fileForm.resourceId ? '文本解析智能体解析成功' : '文本解析智能体解析中'
        }
        const request = this.fileForm.resourceId ? updateCommonResource(data) : addCommonResource(data)
        request.then(() => {
          this.$modal.msgSuccess(this.fileForm.resourceId ? '修改成功' : '上传成功')
          this.fileOpen = false
          this.loadDirectory()
        })
      })
    },
    handleMove(item) {
      this.movingItem = item
      this.targetFolderId = item.resourceType === 'folder' ? (item.parentId || 0) : (item.folderId || 0)
      this.moveOpen = true
    },
    submitMove() {
      const target = this.allFolderList.find(item => item.folderId === this.targetFolderId)
      const folderName = target ? target.folderName : ''
      let request
      if (this.movingItem.resourceType === 'folder') {
        request = updateLibraryFolder({
          folderId: this.movingItem.folderId,
          parentId: this.targetFolderId,
          folderName: this.movingItem.folderName,
          intro: this.movingItem.intro || '',
          visibleScope: this.movingItem.visibleScope || 'all',
          topFlag: this.movingItem.topFlag || '0'
        })
      } else if (this.movingItem.resourceType === 'common') {
        request = assignCommonResourceFolder({
          resourceId: this.movingItem.resourceId,
          folderId: this.targetFolderId,
          folderName
        })
      } else {
        request = assignTaskResourceFolder({
          resourceId: this.movingItem.resourceId,
          folderId: this.targetFolderId,
          folderName
        })
      }
      request.then(() => {
        this.$modal.msgSuccess('移动成功')
        this.moveOpen = false
        this.refreshAll()
      })
    },
    handleDelete(item) {
      const name = item.displayName
      const message = item.resourceType === 'folder'
        ? '是否确认删除文件夹“' + name + '”及其全部子内容？'
        : '是否确认删除文件“' + name + '”？'
      this.$modal.confirm(message).then(() => {
        if (item.resourceType === 'folder') return delLibraryFolder(item.folderId)
        if (item.resourceType === 'common') return delCommonResource(item.resourceId)
        return delTaskResource(item.resourceId)
      }).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.refreshAll()
      }).catch(() => {})
    },
    handleVersion(item) {
      getCommonResource(item.resourceId).then(response => {
        const data = response.data || {}
        this.versionList = data.versionList || []
        this.currentVersionNo = data.currentVersionNo || ''
        this.versionOpen = true
      })
    },
    openFile(url) {
      if (!url) {
        this.$modal.msgWarning('暂无可访问文件')
        return
      }
      window.open(encodeURI(url))
    },
    handleDownload(item) {
      const url = item && item.fileUrl
      if (!url) {
        this.$modal.msgWarning('暂无可下载文件')
        return
      }
      const fileName = item.fileName || item.displayName || this.getFileName(url) || 'download'
      fetch(encodeURI(url), { credentials: 'include' }).then(response => {
        if (!response.ok) {
          throw new Error('download failed')
        }
        return response.blob()
      }).then(blob => {
        saveAs(blob, fileName)
      }).catch(() => {
        this.$modal.msgError('下载文件失败，请重试')
      })
    },
    progressWidth(row) {
      if (row.status === 'stored') return '100%'
      if (row.status === 'failed') return '70%'
      return '30%'
    },
    getCreator(item) {
      if (!item) return '--'
      if (item.resourceType === 'common') return item.creator || '--'
      if (item.resourceType === 'folder') return item.createBy || '--'
      return item.creator || item.createBy || '--'
    },
    getFileName(url) {
      if (!url) return ''
      return url.substring(url.lastIndexOf('/') + 1)
    },
    getFileExt(item) {
      const source = (item && (item.fileName || item.displayName || item.fileUrl || item.previewFileUrl)) || ''
      const cleanSource = source.split('?')[0].split('#')[0]
      const fileName = this.getFileName(cleanSource)
      const dotIndex = fileName.lastIndexOf('.')
      return dotIndex > -1 ? fileName.substring(dotIndex + 1).toLowerCase() : ''
    },
    getFileTypeLabel(item) {
      const ext = this.getFileExt(item)
      const labelMap = {
        docx: 'DOC',
        xlsx: 'XLS',
        pptx: 'PPT',
        jpeg: 'JPG',
        markdown: 'MD'
      }
      return (labelMap[ext] || ext || 'FILE').toUpperCase().substring(0, 4)
    },
    getFileTypeClass(item) {
      const ext = this.getFileExt(item)
      if (['pdf'].includes(ext)) return 'is-pdf'
      if (['doc', 'docx'].includes(ext)) return 'is-word'
      if (['xls', 'xlsx'].includes(ext)) return 'is-excel'
      if (['ppt', 'pptx'].includes(ext)) return 'is-ppt'
      if (['png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp', 'svg'].includes(ext)) return 'is-image'
      if (['txt', 'md', 'markdown', 'log'].includes(ext)) return 'is-text'
      if (['zip', 'rar', 'gz', 'bz2', '7z'].includes(ext)) return 'is-archive'
      if (['mp4', 'avi', 'rmvb', 'mov', 'wmv'].includes(ext)) return 'is-video'
      return 'is-default'
    },
    taskStatusLabel(status) {
      if (status === 'archived') return '已归集'
      if (status === 'failed') return '归集失败'
      return status || '归集处理中'
    }
  }
}
</script>

<style scoped lang="scss">
.audit-folder-page {
  min-height: calc(100vh - 96px);
  background: #f2f3f5;
}

.query-form,
.file-manager {
  background: #fff;
  border-radius: 4px;
}

.query-form {
  padding: 16px 18px 6px;
  margin-bottom: 12px;
}

.file-manager {
  min-height: calc(100vh - 178px);
  padding: 18px;
}

.manager-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #edf0f5;
}

.page-title {
  margin-bottom: 8px;
  color: #2f3440;
  font-size: 22px;
  font-weight: 700;
  line-height: 30px;
}

.breadcrumb-link {
  color: #409eff;
  cursor: pointer;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.view-switch {
  margin-right: 4px;
}

.search-summary {
  margin: 14px 0 0;
  color: #909399;
  font-size: 13px;
}

.manager-table {
  margin-top: 16px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.manager-table ::v-deep th.el-table__cell {
  background: #f6f8fb;
  color: #606266;
  font-weight: 600;
}

.table-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  cursor: pointer;
}

.table-name-cell span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.table-folder-icon {
  position: relative;
  flex: 0 0 30px;
  width: 30px;
  height: 22px;
}

.table-folder-icon .folder-tab {
  top: -4px;
  left: 3px;
  width: 12px;
  height: 6px;
  border-radius: 3px 3px 0 0;
}

.table-folder-icon .folder-body {
  width: 30px;
  height: 22px;
  border-radius: 4px;
}

.manager-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(168px, 1fr));
  gap: 16px;
  padding-top: 18px;
}

.manager-item {
  position: relative;
  height: 174px;
  border: 1px solid #e6eaf0;
  border-radius: 8px;
  background: #fff;
  cursor: default;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.manager-item:hover {
  border-color: #cdd8e7;
  box-shadow: 0 6px 16px rgba(29, 54, 87, 0.08);
}

.item-menu {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 2;
  color: #909399;
  font-size: 18px;
  cursor: pointer;
}

.item-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 92px;
  cursor: pointer;
}

.folder-icon {
  position: relative;
  width: 86px;
  height: 62px;
}

.folder-tab {
  position: absolute;
  top: -8px;
  left: 7px;
  width: 34px;
  height: 13px;
  border-radius: 5px 5px 0 0;
  background: #f2c230;
}

.folder-body {
  display: block;
  width: 86px;
  height: 62px;
  border: 1px solid rgba(214, 153, 0, 0.32);
  border-radius: 8px;
  background: linear-gradient(180deg, #ffd95a 0%, #f0b90d 100%);
  box-shadow: inset 0 -5px 0 rgba(217, 142, 0, 0.14);
}

.file-type-icon {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #d6dfec;
  border-radius: 6px;
  background: linear-gradient(180deg, #ffffff 0%, #f7faff 100%);
  color: #5f6f85;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(43, 66, 96, 0.08);
}

.file-type-icon::before {
  content: "";
  position: absolute;
  top: -1px;
  right: -1px;
  width: 18px;
  height: 18px;
  border-left: 1px solid #d6dfec;
  border-bottom: 1px solid #d6dfec;
  border-radius: 0 6px 0 4px;
  background: #eef3fa;
}

.file-type-icon span {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  height: 22px;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  line-height: 22px;
  letter-spacing: 0;
  text-align: center;
  background: #7a8aa0;
}

.file-type-icon--table {
  flex: 0 0 28px;
  width: 28px;
  height: 34px;
  border-radius: 4px;
  box-shadow: none;
}

.file-type-icon--table::before {
  width: 10px;
  height: 10px;
  border-radius: 0 4px 0 3px;
}

.file-type-icon--table span {
  height: 14px;
  font-size: 8px;
  line-height: 14px;
}

.file-type-icon--grid {
  width: 66px;
  height: 78px;
}

.file-type-icon--version {
  flex: 0 0 30px;
  width: 30px;
  height: 36px;
  border-radius: 4px;
  box-shadow: none;
}

.file-type-icon--version::before {
  width: 11px;
  height: 11px;
  border-radius: 0 4px 0 3px;
}

.file-type-icon--version span {
  height: 15px;
  font-size: 8px;
  line-height: 15px;
}

.file-type-icon.is-pdf span {
  background: #e05252;
}

.file-type-icon.is-word span {
  background: #2f73d9;
}

.file-type-icon.is-excel span {
  background: #2f9e67;
}

.file-type-icon.is-ppt span {
  background: #e17637;
}

.file-type-icon.is-image span {
  background: #7b61d1;
}

.file-type-icon.is-text span {
  background: #607286;
}

.file-type-icon.is-archive span {
  background: #9a6b35;
}

.file-type-icon.is-video span {
  background: #1f8fa3;
}

.file-type-icon.is-default span {
  background: #7a8aa0;
}

.progress-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.progress-bar {
  width: 210px;
  max-width: 100%;
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

.item-name {
  padding: 0 16px;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
  line-height: 20px;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 24px;
  margin-top: 10px;
  padding: 0 12px;
  color: #909399;
  font-size: 12px;
}

.delete-btn {
  color: #f56c6c;
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

.drag-upload-panel ::v-deep .upload-file-uploader .el-upload {
  width: 100%;
}

.drag-upload-panel ::v-deep .upload-file-uploader .el-upload-dragger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 140px;
  padding: 22px 16px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  background: #fafcff;
  box-sizing: border-box;
}

.drag-upload-panel ::v-deep .upload-drag-icon {
  margin-bottom: 12px;
  font-size: 34px;
  line-height: 1;
}

.drag-upload-panel ::v-deep .upload-drag-copy {
  display: flex;
  align-items: baseline;
  justify-content: center;
  flex-wrap: wrap;
  width: 100%;
  line-height: 20px;
  text-align: center;
}

.version-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.version-card {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 14px 16px;
  background: #fff;
}

.version-head,
.version-file,
.version-meta {
  display: flex;
  align-items: center;
}

.version-head {
  justify-content: space-between;
  margin-bottom: 10px;
}

.version-title {
  font-weight: 600;
  color: #303133;
}

.version-tag {
  display: inline-block;
  margin-left: 8px;
  padding: 2px 6px;
  border-radius: 4px;
  background: #ecf5ff;
  color: #409eff;
  font-size: 12px;
}

.version-time,
.version-meta {
  color: #909399;
  font-size: 13px;
}

.version-file {
  gap: 8px;
  color: #606266;
}

.version-file span {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.version-meta {
  gap: 24px;
  margin-top: 8px;
}

::v-deep .library-file-dialog .el-dialog__body {
  max-height: calc(100vh - 190px);
  overflow-y: auto;
  padding: 12px 20px 4px !important;
}

@media (max-width: 768px) {
  .manager-toolbar {
    flex-direction: column;
  }

  .toolbar-actions {
    justify-content: flex-start;
  }
}
</style>
