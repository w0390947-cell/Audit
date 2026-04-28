<template>
  <div class="app-container audit-folder-page" v-loading="loading">
    <div class="library-panel">
      <div class="section-head section-head-top">
        <div class="section-title">置顶文件库</div>
        <el-button
          type="primary"
          icon="el-icon-plus"
          size="small"
          @click="handleAdd"
          v-hasPermi="['audit:library:folder:add']"
        >
          新建文件库
        </el-button>
      </div>

      <div class="folder-grid top-grid">
        <div
          v-for="item in topFolders"
          :key="item.folderId"
          class="folder-card"
        >
          <div class="folder-settings">
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

      <div class="section-head">
        <div class="section-title">全部文件库</div>
      </div>
      <div class="folder-grid">
        <div
          v-for="item in normalFolders"
          :key="item.folderId"
          class="folder-card"
        >
          <div class="folder-settings">
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
  </div>
</template>

<script>
import {
  addLibraryFolder,
  delLibraryFolder,
  listLibraryFolders,
  updateLibraryFolder
} from '@/api/audit/library'

export default {
  name: 'AuditLibraryFolder',
  data() {
    return {
      loading: false,
      open: false,
      title: '',
      folderList: [],
      form: {
        folderId: undefined,
        folderName: '',
        intro: '',
        visibleScope: 'all',
        topFlag: '0'
      },
      visibleScopeValues: ['all'],
      rules: {
        folderName: [
          { required: true, message: '文件库名称不能为空', trigger: 'blur' }
        ],
        visibleScope: [
          { required: true, message: '可见范围不能为空', trigger: 'change' }
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
