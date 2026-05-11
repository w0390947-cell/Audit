<template>
  <div class="app-container audit-ai-page">
    <el-form ref="queryForm" :model="queryParams" :inline="true" size="small" class="query-form">
      <el-form-item prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          clearable
          placeholder="请输入任务编号或产品名称"
          style="width: 280px"
          @keyup.enter.native="handleQuery"
        >
          <i slot="prefix" class="el-input__icon el-icon-search" />
        </el-input>
      </el-form-item>
      <el-form-item label="优先级" prop="priority">
        <el-select v-model="queryParams.priority" clearable placeholder="请选择优先级" style="width: 180px">
          <el-option
            v-for="dict in dict.type.audit_review_priority"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="任务状态" prop="taskStatus">
        <el-select v-model="queryParams.taskStatus" clearable placeholder="请选择任务状态" style="width: 180px">
          <el-option
            v-for="dict in dict.type.audit_ai_task_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="提交人" prop="submitter">
        <el-select v-model="queryParams.submitter" clearable placeholder="请选择提交人" style="width: 180px">
          <el-option v-for="item in submitterOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="content-row">
      <div class="stats-panel">
        <div class="stats-card">
          <div class="stats-title">队伍情况</div>
          <div class="stats-summary">
            <div>队列总数：{{ stats.totalTaskCount || 0 }}个</div>
          </div>
          <div ref="priorityChart" class="priority-chart" />
          <div class="completion-box">
            <el-progress :percentage="Number(stats.completionRate || 0)" :stroke-width="10" :show-text="false" />
            <div class="completion-text">已完成进度：{{ stats.completionRate || 0 }}%</div>
          </div>
          <table class="stat-table">
            <tbody>
              <tr>
                <th>任务总数</th>
                <td>{{ stats.currentQueueTaskCount || 0 }}个</td>
              </tr>
              <tr>
                <th>待执行数</th>
                <td>{{ stats.waitingCount || 0 }}个</td>
              </tr>
              <tr>
                <th>执行中数</th>
                <td>{{ stats.executingCount || 0 }}个</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="table-panel">
        <div class="batch-toolbar">
          <div />
          <div class="batch-actions">
            <el-button
              size="small"
              @click="handleBatchStatus('paused')"
              :disabled="multiple"
              v-hasPermi="['audit:ai:changeStatus']"
            >批量暂停</el-button>
            <el-button
              size="small"
              @click="handleBatchStatus('waiting')"
              :disabled="multiple"
              v-hasPermi="['audit:ai:changeStatus']"
            >批量恢复</el-button>
            <el-button
              size="small"
              type="danger"
              plain
              @click="handleDelete()"
              :disabled="multiple"
              v-hasPermi="['audit:ai:remove']"
            >批量删除</el-button>
            <el-button
              size="small"
              @click="handleRaisePriority()"
              :disabled="multiple"
              v-hasPermi="['audit:ai:raisePriority']"
            >批量提升优先级</el-button>
          </div>
        </div>

        <el-table
          v-loading="loading"
          :data="taskList"
          class="task-table"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="任务编号" align="center" prop="taskNo" min-width="150" />
          <el-table-column label="产品名称" align="center" prop="productName" min-width="110" />
          <el-table-column label="送检单位" align="center" prop="deliveryUnit" min-width="110" />
          <el-table-column label="提交人" align="center" prop="submitter" min-width="90" />
          <el-table-column label="优先级" align="center" prop="priority" min-width="90">
            <template slot-scope="scope">
              <span :class="['priority-text', 'priority-' + scope.row.priority]">{{ priorityLabel(scope.row.priority) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="队列位置" align="center" prop="queuePosition" min-width="90">
            <template slot-scope="scope">
              {{ scope.row.queuePosition > 0 ? '第' + scope.row.queuePosition + '位' : '--' }}
            </template>
          </el-table-column>
          <el-table-column label="任务状态" align="center" prop="taskStatus" min-width="100">
            <template slot-scope="scope">
              <dict-tag :options="dict.type.audit_ai_task_status" :value="scope.row.taskStatus" />
            </template>
          </el-table-column>
          <el-table-column label="预计执行时间" align="center" prop="estimatedDuration" min-width="110" />
          <el-table-column label="执行进度" align="center" min-width="220">
            <template slot-scope="scope">
              <div class="progress-cell">
                <span class="progress-text">{{ scope.row.progressText || '--' }}</span>
                <el-progress :percentage="scope.row.progressPercent || 0" :show-text="false" :stroke-width="8" />
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" min-width="220" class-name="small-padding fixed-width">
            <template slot-scope="scope">
              <el-button
                size="mini"
                type="text"
                @click="handleDetail(scope.row)"
                v-hasPermi="['audit:ai:detail']"
              >详情</el-button>
              <el-button
                size="mini"
                type="text"
                @click="handleRaisePriority(scope.row)"
                v-hasPermi="['audit:ai:raisePriority']"
              >提升优先级</el-button>
              <el-button
                size="mini"
                type="text"
                :disabled="!canChangeRowStatus(scope.row)"
                @click="handleBatchStatus(scope.row.taskStatus === 'paused' ? 'waiting' : 'paused', scope.row)"
                v-hasPermi="['audit:ai:changeStatus']"
              >{{ scope.row.taskStatus === 'paused' ? '恢复' : '暂停' }}</el-button>
              <el-button
                size="mini"
                type="text"
                class="delete-btn"
                @click="handleDelete(scope.row)"
                v-hasPermi="['audit:ai:remove']"
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
  </div>
</template>

<script>
import * as echarts from 'echarts'
require('echarts/theme/macarons')
import {
  changeAiTaskStatus,
  delAiTask,
  getAiStats,
  listAiSubmitters,
  listAiTask,
  raiseAiPriority
} from '@/api/audit/ai'

export default {
  name: 'AuditAiQueue',
  dicts: ['audit_review_priority', 'audit_ai_task_status'],
  data() {
    return {
      loading: true,
      total: 0,
      ids: [],
      multiple: true,
      taskList: [],
      submitterOptions: [],
      stats: {
        queueGroupCount: 0,
        totalTaskCount: 0,
        currentQueueTaskCount: 0,
        waitingCount: 0,
        executingCount: 0,
        pausedCount: 0,
        completionRate: 0,
        highCount: 0,
        mediumCount: 0,
        lowCount: 0
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        keyword: undefined,
        priority: undefined,
        taskStatus: undefined,
        submitter: undefined
      },
      chart: null,
      resizeHandler: null
    }
  },
  created() {
    this.getSubmitters()
    this.getStats()
    this.getList()
  },
  mounted() {
    this.resizeHandler = () => {
      if (this.chart) {
        this.chart.resize()
      }
    }
    window.addEventListener('resize', this.resizeHandler)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeHandler)
    if (this.chart) {
      this.chart.dispose()
      this.chart = null
    }
  },
  methods: {
    getList() {
      this.loading = true
      listAiTask(this.queryParams).then(response => {
        this.taskList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    getStats() {
      getAiStats().then(response => {
        this.stats = response.data || this.stats
        this.$nextTick(() => {
          this.renderChart()
        })
      })
    },
    getSubmitters() {
      listAiSubmitters().then(response => {
        this.submitterOptions = response.data || []
      })
    },
    renderChart() {
      if (!this.$refs.priorityChart) {
        return
      }
      if (!this.chart) {
        this.chart = echarts.init(this.$refs.priorityChart, 'macarons')
      }
      this.chart.setOption({
        color: ['#409EFF', '#41c3d9', '#ffb44c'],
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          top: 4,
          left: 'center',
          itemWidth: 10,
          itemHeight: 10,
          icon: 'circle',
          textStyle: {
            color: '#606266'
          },
          data: ['高优先级', '中优先级', '低优先级']
        },
        series: [
          {
            type: 'pie',
            radius: '70%',
            center: ['50%', '58%'],
            label: {
              formatter: '{d}%',
              color: '#fff',
              fontSize: 14,
              fontWeight: 500
            },
            labelLine: {
              show: false
            },
            data: [
              { value: this.stats.highCount || 0, name: '高优先级' },
              { value: this.stats.mediumCount || 0, name: '中优先级' },
              { value: this.stats.lowCount || 0, name: '低优先级' }
            ]
          }
        ]
      })
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
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.aiTaskId)
      this.multiple = !selection.length
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getStats()
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        keyword: undefined,
        priority: undefined,
        taskStatus: undefined,
        submitter: undefined
      }
      this.handleQuery()
    },
    handleDetail(row) {
      this.$router.push('/audit-ai/detail/' + row.aiTaskId)
    },
    handleBatchStatus(taskStatus, row) {
      const aiTaskIds = row ? [row.aiTaskId] : this.ids
      if (!aiTaskIds.length) {
        this.$message.warning('请先选择任务')
        return
      }
      const selectedRows = row ? [row] : this.taskList.filter(item => this.ids.includes(item.aiTaskId))
      if (taskStatus === 'paused' && selectedRows.some(item => item.taskStatus === 'executing')) {
        this.$message.warning('执行中的任务正在被工作流处理，不能暂停')
        return
      }
      if (taskStatus === 'paused' && selectedRows.some(item => item.taskStatus !== 'waiting')) {
        this.$message.warning('只有等待中的任务可以暂停')
        return
      }
      if (taskStatus === 'waiting' && selectedRows.some(item => item.taskStatus !== 'paused')) {
        this.$message.warning('只有已暂停的任务可以恢复')
        return
      }
      const actionText = taskStatus === 'paused' ? '暂停' : '恢复'
      this.$modal.confirm('是否确认' + actionText + '选中的任务？').then(() => {
        return changeAiTaskStatus({
          aiTaskIds: aiTaskIds,
          taskStatus: taskStatus
        })
      }).then(() => {
        this.$modal.msgSuccess(actionText + '成功')
        this.getStats()
        this.getList()
      }).catch(() => {})
    },
    canChangeRowStatus(row) {
      return row.taskStatus === 'waiting' || row.taskStatus === 'paused'
    },
    handleRaisePriority(row) {
      const aiTaskIds = row ? [row.aiTaskId] : this.ids
      if (!aiTaskIds.length) {
        this.$message.warning('请先选择任务')
        return
      }
      this.$modal.confirm('是否确认提升所选任务优先级？').then(() => {
        return raiseAiPriority({ aiTaskIds: aiTaskIds })
      }).then(() => {
        this.$modal.msgSuccess('优先级已提升')
        this.getStats()
        this.getList()
      }).catch(() => {})
    },
    handleDelete(row) {
      const aiTaskIds = row ? [row.aiTaskId] : this.ids
      if (!aiTaskIds.length) {
        this.$message.warning('请先选择任务')
        return
      }
      this.$modal.confirm('是否确认删除所选 AI 任务？').then(() => {
        return delAiTask(aiTaskIds.join(','))
      }).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getStats()
        this.getList()
      }).catch(() => {})
    },
    handleExport() {
      this.download('audit/ai/export', {
        ...this.queryParams
      }, 'AI任务队列_' + new Date().getTime() + '.xlsx')
    }
  }
}
</script>

<style scoped lang="scss">
.audit-ai-page {
  background: #f5f7fa;
}

.query-form,
.stats-card,
.table-panel {
  background: #fff;
  border-radius: 2px;
  border: 1px solid #ebeef5;
}

.query-form {
  padding: 12px 14px 2px;
  margin-bottom: 12px;
}

.content-row {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.stats-panel {
  width: 350px;
  flex-shrink: 0;
}

.stats-card {
  padding: 14px;
}

.stats-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.stats-summary {
  display: flex;
  justify-content: space-between;
  background: #f5f7fa;
  padding: 12px 14px;
  border-radius: 4px;
  color: #606266;
  font-size: 13px;
  margin-bottom: 12px;
}

.priority-chart {
  width: 100%;
  height: 260px;
}

.completion-box {
  background: #f5f8ff;
  border-radius: 4px;
  padding: 10px 12px;
  margin-bottom: 12px;
}

.completion-text {
  font-size: 13px;
  color: #409eff;
  margin-top: 6px;
}

.stat-table {
  width: 100%;
  border-collapse: collapse;

  th,
  td {
    border: 1px solid #ebeef5;
    padding: 10px 12px;
    font-size: 13px;
    line-height: 1.45;
  }

  th {
    width: 38%;
    color: #303133;
    font-weight: 600;
    background: #fafbfc;
  }

  td {
    color: #606266;
    background: #fff;
  }
}

.table-panel {
  flex: 1;
  min-width: 0;
  padding: 12px 14px 10px;
}

.batch-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.batch-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.task-table ::v-deep .el-table__header-wrapper th {
  background: #f5f7fa;
  color: #606266;
  font-weight: 500;
}

.task-table ::v-deep .el-table__cell {
  padding-top: 10px;
  padding-bottom: 10px;
}

.task-table ::v-deep .el-table__body td {
  vertical-align: top;
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

.progress-cell {
  text-align: left;
}

.progress-text {
  display: block;
  color: #606266;
  font-size: 12px;
  margin-bottom: 6px;
}

.delete-btn {
  color: #f56c6c;
}
</style>
