<template>
  <div class="app-container audit-asset-page">
    <el-form ref="queryForm" :model="queryParams" :inline="true" size="small" class="query-form">
      <el-form-item prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          clearable
          placeholder="请输入"
          style="width: 280px"
          @keyup.enter.native="handleQuery"
        >
          <i slot="prefix" class="el-input__icon el-icon-search" />
        </el-input>
      </el-form-item>
      <el-form-item label="审核人" prop="reviewer">
        <el-select v-model="queryParams.reviewer" clearable placeholder="请选择审核人" style="width: 220px">
          <el-option
            v-for="item in reviewerOptions"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="审核状态" prop="reviewStatus">
        <el-select v-model="queryParams.reviewStatus" clearable placeholder="请选择审核状态" style="width: 220px">
          <el-option
            v-for="dict in dict.type.audit_asset_review_status"
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
          style="width: 240px"
          type="date"
          value-format="yyyy-MM-dd"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="asset-layout">
      <div class="stats-panel">
        <div class="stats-shell">
          <div class="stats-title">记录总数</div>

          <div class="stats-block">
            <div class="summary-head">
              <span>{{ stats.yearLabel || '2025年' }}</span>
              <span>记录总数{{ stats.totalCount || 0 }}</span>
            </div>
            <div ref="yearChart" class="chart chart-year" />
          </div>

          <div class="stats-block">
            <div class="summary-head">
              <span>月度记录</span>
              <span>当前分类：{{ stats.currentCategoryCount || 0 }}条记录</span>
            </div>
            <div ref="monthChart" class="chart chart-month" />
          </div>

          <div class="stats-block pie-block">
            <div class="summary-head summary-head-bottom">请选择时间分布</div>
            <el-select v-model="timeRangeLabel" style="width: 100%">
              <el-option label="近7天记录数" value="近7天记录数" />
              <el-option label="近30天记录数" value="近30天记录数" />
              <el-option label="近90天记录数" value="近90天记录数" />
            </el-select>
            <div ref="pieChart" class="chart chart-pie" />
          </div>
        </div>
      </div>

      <div class="table-panel">
        <div class="toolbar-row">
          <div />
          <div class="toolbar-actions">
            <el-button
              plain
              size="small"
              @click="handleBatchDownload"
              v-hasPermi="['audit:asset:batchDownload']"
            >批量报告下载</el-button>
            <el-button
              plain
              size="small"
              @click="handleBatchPackage"
              v-hasPermi="['audit:asset:batchPackage']"
            >一键打包</el-button>
            <el-button
              plain
              size="small"
              icon="el-icon-download"
              @click="handleExport"
              v-hasPermi="['audit:asset:export']"
            >批量导出</el-button>
          </div>
        </div>

        <el-table
          v-loading="loading"
          :data="assetList"
          class="asset-table"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="52" align="center" />
          <el-table-column label="任务编号" align="left" prop="taskNo" min-width="150" />
          <el-table-column label="文件名称" align="left" prop="productName" min-width="120" />
          <el-table-column label="文件位置" align="left" prop="deliveryUnit" min-width="120" />
          <el-table-column label="上传者" align="left" prop="submitter" min-width="100" />
          <el-table-column label="审核时间" align="center" prop="reviewTime" min-width="160">
            <template slot-scope="scope">
              {{ parseTime(scope.row.reviewTime) }}
            </template>
          </el-table-column>
          <el-table-column label="AI分析次数" align="center" prop="aiAnalysisCount" width="100" />
          <el-table-column label="审核状态" align="center" prop="reviewStatus" width="120">
            <template slot-scope="scope">
              <dict-tag :options="dict.type.audit_asset_review_status" :value="scope.row.reviewStatus" />
            </template>
          </el-table-column>
          <el-table-column label="操作" align="left" min-width="340" class-name="small-padding">
            <template slot-scope="scope">
              <el-button
                size="mini"
                type="text"
                @click="handleDetail(scope.row)"
                v-hasPermi="['audit:asset:detail']"
              >详情</el-button>
              <el-button
                size="mini"
                type="text"
                @click="handleAssign(scope.row)"
                v-hasPermi="['audit:asset:assign']"
              >权限分配</el-button>
              <el-button
                v-if="canReviewLibraryResource(scope.row)"
                size="mini"
                type="text"
                @click="handleReview(scope.row, 'approved')"
                v-hasPermi="['audit:asset:review']"
              >审核通过</el-button>
              <el-button
                v-if="canReviewLibraryResource(scope.row)"
                size="mini"
                type="text"
                class="delete-btn"
                @click="handleReview(scope.row, 'returned')"
                v-hasPermi="['audit:asset:review']"
              >驳回</el-button>
              <el-button
                size="mini"
                type="text"
                class="delete-btn"
                @click="handleDelete(scope.row)"
                v-hasPermi="['audit:asset:remove']"
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
      </div>
    </div>

    <el-dialog title="权限分配" :visible.sync="assignOpen" width="420px" append-to-body>
      <el-form ref="assignForm" :model="assignForm" label-width="90px">
        <el-form-item label="任务编号">
          <span>{{ assignForm.taskNo }}</span>
        </el-form-item>
        <el-form-item label="分配对象" prop="permissionOwner">
          <el-select
            v-model="assignForm.permissionOwner"
            clearable
            filterable
            allow-create
            default-first-option
            placeholder="请选择或输入"
            style="width: 100%"
          >
            <el-option
              v-for="item in reviewerOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="assignOpen = false">取消</el-button>
        <el-button type="primary" @click="submitAssign">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { download } from '@/utils/request'
import {
  assignAssetPermission,
  batchDownloadAsset,
  batchPackageAsset,
  delAsset,
  listAsset,
  listAssetReviewers,
  listAssetStats,
  reviewAsset
} from '@/api/audit/asset'

export default {
  name: 'AuditAsset',
  dicts: ['audit_asset_review_status'],
  data() {
    return {
      loading: true,
      total: 0,
      queryDate: undefined,
      reviewerOptions: [],
      assetList: [],
      selectedIds: [],
      assignOpen: false,
      timeRangeLabel: '近7天记录数',
      stats: {
        yearLabel: '2025年',
        totalCount: 0,
        currentCategoryCount: 0,
        monthLabels: [],
        yearApprovedData: [],
        yearReturnedData: [],
        monthApprovedData: [],
        monthReturnedData: [],
        pieLabels: [],
        pieData: []
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        keyword: undefined,
        reviewer: undefined,
        reviewStatus: undefined
      },
      assignForm: {
        assetId: undefined,
        taskNo: undefined,
        permissionOwner: undefined
      },
      chartInstances: []
    }
  },
  created() {
    this.getList()
    this.getStats()
    this.getReviewers()
  },
  mounted() {
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    this.chartInstances.forEach(item => item && item.dispose())
  },
  methods: {
    formatDateRange() {
      if (!this.queryDate) {
        return []
      }
      return [this.queryDate, this.queryDate]
    },
    canReviewLibraryResource(row) {
      return row && row.libraryResourceId && row.reviewStatus === 'reviewing'
    },
    getList() {
      this.loading = true
      listAsset(this.addDateRange({ ...this.queryParams }, this.formatDateRange(), 'ReviewTime')).then(response => {
        this.assetList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    getStats() {
      listAssetStats().then(response => {
        this.stats = response.data || this.stats
        this.$nextTick(() => {
          this.renderCharts()
        })
      })
    },
    getReviewers() {
      listAssetReviewers().then(response => {
        this.reviewerOptions = response.data || []
      })
    },
    renderCharts() {
      this.renderYearChart()
      this.renderMonthChart()
      this.renderPieChart()
    },
    renderYearChart() {
      const chart = echarts.init(this.$refs.yearChart)
      chart.setOption({
        color: ['#3187ec', '#f05b47'],
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        legend: {
          data: ['审核通过', '驳回'],
          top: 0,
          left: 'center',
          icon: 'circle',
          itemWidth: 8,
          itemHeight: 8,
          textStyle: { color: '#6b7280', fontSize: 12 }
        },
        grid: { left: 18, right: 18, top: 36, bottom: 8, containLabel: true },
        xAxis: {
          type: 'value',
          splitLine: { lineStyle: { color: '#e9edf5' } },
          axisLabel: { color: '#8d95a5' },
          axisTick: { show: false },
          axisLine: { show: false }
        },
        yAxis: {
          type: 'category',
          data: [this.stats.yearLabel || '2025年'],
          axisTick: { show: false },
          axisLine: { show: false },
          axisLabel: { color: '#707786' }
        },
        series: [
          { name: '审核通过', type: 'bar', barWidth: 16, data: this.stats.yearApprovedData || [0] },
          { name: '驳回', type: 'bar', barWidth: 16, data: this.stats.yearReturnedData || [0] }
        ]
      })
      this.saveChart(chart)
    },
    renderMonthChart() {
      const chart = echarts.init(this.$refs.monthChart)
      chart.setOption({
        color: ['#3187ec', '#f05b47'],
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        legend: {
          data: ['审核通过', '驳回'],
          top: 0,
          left: 'center',
          icon: 'circle',
          itemWidth: 8,
          itemHeight: 8,
          textStyle: { color: '#6b7280', fontSize: 12 }
        },
        grid: { left: 14, right: 14, top: 38, bottom: 12, containLabel: true },
        xAxis: {
          type: 'category',
          data: this.stats.monthLabels || [],
          axisTick: { show: false },
          axisLine: { lineStyle: { color: '#e4e8f0' } },
          axisLabel: { color: '#707786' }
        },
        yAxis: {
          type: 'value',
          splitLine: { lineStyle: { color: '#e9edf5' } },
          axisTick: { show: false },
          axisLine: { show: false },
          axisLabel: { color: '#8d95a5' }
        },
        series: [
          { name: '审核通过', type: 'bar', barWidth: 10, data: this.stats.monthApprovedData || [] },
          { name: '驳回', type: 'bar', barWidth: 10, data: this.stats.monthReturnedData || [] }
        ]
      })
      this.saveChart(chart)
    },
    renderPieChart() {
      const chart = echarts.init(this.$refs.pieChart)
      chart.setOption({
        color: ['#3187ec', '#f05b47'],
        tooltip: { trigger: 'item' },
        legend: {
          data: this.stats.pieLabels || [],
          top: 0,
          left: 'center',
          icon: 'circle',
          itemWidth: 8,
          itemHeight: 8,
          textStyle: { color: '#6b7280', fontSize: 12 }
        },
        series: [
          {
            type: 'pie',
            radius: '66%',
            center: ['50%', '58%'],
            label: {
              formatter: '{d}%',
              color: '#fff',
              fontSize: 16,
              fontWeight: 500
            },
            labelLine: { show: false },
            data: (this.stats.pieLabels || []).map((item, index) => {
              return {
                name: item,
                value: (this.stats.pieData || [])[index] || 0
              }
            })
          }
        ]
      })
      this.saveChart(chart)
    },
    saveChart(chart) {
      const old = this.chartInstances.find(item => item.getDom() === chart.getDom())
      if (old) {
        old.dispose()
        this.chartInstances = this.chartInstances.filter(item => item !== old)
      }
      this.chartInstances.push(chart)
    },
    handleResize() {
      this.chartInstances.forEach(item => item && item.resize())
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
    handleSelectionChange(selection) {
      this.selectedIds = selection.map(item => item.assetId)
    },
    handleDetail(row) {
      this.$router.push('/audit-asset/detail/' + row.assetId)
    },
    handleAssign(row) {
      this.assignForm = {
        assetId: row.assetId,
        taskNo: row.taskNo,
        permissionOwner: row.permissionOwner || row.reviewer
      }
      this.assignOpen = true
    },
    submitAssign() {
      assignAssetPermission({
        assetId: this.assignForm.assetId,
        permissionOwner: this.assignForm.permissionOwner
      }).then(() => {
        this.$modal.msgSuccess('权限分配成功')
        this.assignOpen = false
        this.getList()
      })
    },
    handleReview(row, reviewStatus) {
      const actionText = reviewStatus === 'approved' ? '审核通过' : '驳回'
      const message = reviewStatus === 'approved'
        ? '是否确认审核通过“' + row.productName + '”？通过后将开始向量化入库。'
        : '是否确认驳回“' + row.productName + '”？驳回后将从审核文件库移除。'
      this.$modal.confirm(message).then(() => {
        return reviewAsset({
          assetId: row.assetId,
          reviewStatus
        })
      }).then(() => {
        this.$modal.msgSuccess(actionText + '成功')
        this.getList()
        this.getStats()
      }).catch(() => {})
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除任务编号为“' + row.taskNo + '”的数据项？').then(() => {
        return delAsset(row.assetId)
      }).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
        this.getStats()
      }).catch(() => {})
    },
    handleBatchDownload() {
      const ids = this.selectedIds
      if (!ids.length) {
        this.$modal.msgWarning('请至少选择一条数据')
        return
      }
      batchDownloadAsset({ assetIds: ids }).then(response => {
        const fileList = (response.data && response.data.fileList) || []
        if (!fileList.length) {
          this.$modal.msgWarning('所选记录未找到可下载文件')
          return
        }
        fileList.forEach((item, index) => {
          setTimeout(() => {
            window.open(encodeURI(item.url))
          }, index * 300)
        })
        this.$modal.msgSuccess('已开始批量下载')
      })
    },
    handleBatchPackage() {
      const ids = this.selectedIds
      if (!ids.length) {
        this.$modal.msgWarning('请至少选择一条数据')
        return
      }
      batchPackageAsset({ assetIds: ids }).then(response => {
        const url = response.data && response.data.downloadUrl
        if (!url) {
          this.$modal.msgWarning('暂未生成打包文件')
          return
        }
        window.open(encodeURI(url))
      })
    },
    handleExport() {
      download(
        '/audit/asset/export',
        this.addDateRange({ ...this.queryParams }, this.formatDateRange(), 'ReviewTime'),
        '审核资源列表_' + this.parseTime(new Date(), '{y}{m}{d}{h}{i}{s}') + '.xlsx'
      )
    }
  }
}
</script>

<style scoped lang="scss">
.audit-asset-page {
  min-height: calc(100vh - 96px);
  background: #f2f3f5;
}

.query-form,
.table-panel,
.stats-shell {
  background: #fff;
  border-radius: 4px;
}

.query-form {
  padding: 14px 16px;
  margin-bottom: 14px;

  ::v-deep .el-form-item {
    margin-right: 14px;
    margin-bottom: 8px;
  }
}

.asset-layout {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.stats-panel {
  width: 430px;
  flex-shrink: 0;
}

.stats-shell {
  padding: 14px;
}

.stats-title {
  margin-bottom: 12px;
  color: #2f3440;
  font-size: 24px;
  font-weight: 700;
  line-height: 1;
}

.stats-block {
  margin-bottom: 12px;
  padding: 14px 16px 14px;
  background: #f5f7fb;
  border-radius: 6px;
}

.stats-block:last-child {
  margin-bottom: 0;
}

.summary-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #5f6674;
  font-size: 14px;
  margin-bottom: 10px;
}

.summary-head-bottom {
  margin-bottom: 10px;
}

.chart {
  width: 100%;
}

.chart-year {
  height: 156px;
}

.chart-month {
  height: 238px;
}

.chart-pie {
  height: 220px;
  margin-top: 8px;
}

.table-panel {
  flex: 1;
  padding: 14px;
  min-width: 0;
}

.toolbar-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.toolbar-actions > .el-button + .el-button {
  margin-left: 10px;
}

::v-deep .asset-table th.el-table__cell {
  background: #f2f4f8;
  color: #6b7280;
  font-weight: 600;
}

::v-deep .asset-table .el-table__cell {
  padding-top: 10px;
  padding-bottom: 10px;
}

::v-deep .asset-table .el-button--text {
  color: #4b92e8;
  font-size: 13px;
  padding: 0;
  margin-right: 12px;
}

::v-deep .asset-table .el-button--text:last-child {
  margin-right: 0;
}

.delete-btn {
  color: #f56c6c !important;
}

@media (max-width: 1480px) {
  .asset-layout {
    flex-direction: column;
  }

  .stats-panel {
    width: 100%;
  }
}
</style>
