<template>
  <div class="app-container home-page">
    <el-row :gutter="14" class="metric-row">
      <el-col :xs="24" :sm="12" :lg="6" v-for="item in metrics" :key="item.label">
        <div class="metric-card">
          <div class="metric-label">{{ item.label }}</div>
          <div class="metric-value">{{ item.value }}</div>
          <div class="metric-desc">{{ item.desc }}</div>
        </div>
      </el-col>
    </el-row>

    <div class="asset-insight-card">
      <div class="asset-insight-main">
        <div>
          <div class="section-title">审核资产概览</div>
          <div class="asset-total-label">审核资源记录总数</div>
          <div class="asset-total-value">{{ assetStats.totalCount || 0 }}</div>
          <div class="asset-total-desc">{{ assetStats.yearLabel || '本年度' }}记录 {{ assetStats.currentCategoryCount || 0 }} 条</div>
        </div>
        <div class="asset-status-grid">
          <div class="asset-status-item approved">
            <span>审核通过归档</span>
            <strong>{{ approvedCount }}</strong>
          </div>
          <div class="asset-status-item returned">
            <span>驳回归档</span>
            <strong>{{ returnedCount }}</strong>
          </div>
        </div>
      </div>
      <div class="asset-chart-grid">
        <div ref="assetTrendChart" class="asset-chart" />
        <div ref="assetPieChart" class="asset-chart" />
      </div>
    </div>

    <div class="ai-queue-card">
      <div class="ai-queue-head">
        <div>
          <div class="section-title">AI队列概览</div>
          <div class="ai-queue-subtitle">队列数量 {{ aiStats.queueGroupCount || 0 }} 队</div>
        </div>
        <div class="ai-completion">
          <el-progress
            type="circle"
            :percentage="Number(aiStats.completionRate || 0)"
            :width="92"
            :stroke-width="8"
          />
          <span>平均完成进度</span>
        </div>
      </div>
      <div class="ai-queue-body">
        <div class="ai-total-box">
          <span>队列总数</span>
          <strong>{{ aiStats.totalTaskCount || 0 }}</strong>
          <em>当前队列 {{ aiStats.currentQueueTaskCount || 0 }} 个任务</em>
        </div>
        <div class="ai-status-list">
          <div class="ai-status-item waiting">
            <span>等待中</span>
            <strong>{{ aiStats.waitingCount || 0 }}</strong>
          </div>
          <div class="ai-status-item executing">
            <span>执行中</span>
            <strong>{{ aiStats.executingCount || 0 }}</strong>
          </div>
          <div class="ai-status-item paused">
            <span>已暂停</span>
            <strong>{{ aiStats.pausedCount || 0 }}</strong>
          </div>
          <div class="ai-status-item completed">
            <span>已完成</span>
            <strong>{{ aiStats.completedCount || 0 }}</strong>
          </div>
        </div>
        <div ref="aiPriorityChart" class="ai-priority-chart" />
      </div>
    </div>

    <el-row :gutter="14">
      <el-col :xs="24">
        <div class="section-card">
          <div class="section-title">待办概览</div>
          <div class="todo-list">
            <div class="todo-item" v-for="item in todos" :key="item.label">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { listAssetStats } from '@/api/audit/asset'
import { getAiStats } from '@/api/audit/ai'

export default {
  name: 'Home',
  data() {
    return {
      metrics: [
        { label: '审核任务', value: '--', desc: '当前审核列表总览' },
        { label: 'AI任务', value: '--', desc: 'AI队列处理情况' },
        { label: '资产记录', value: 0, desc: '审核资产库记录' },
        { label: '待处理', value: '--', desc: '需要关注的事项' }
      ],
      assetStats: {
        yearLabel: '',
        totalCount: 0,
        currentCategoryCount: 0,
        monthLabels: [],
        monthApprovedData: [],
        monthReturnedData: [],
        pieLabels: [],
        pieData: []
      },
      aiStats: {
        queueGroupCount: 0,
        totalTaskCount: 0,
        currentQueueTaskCount: 0,
        waitingCount: 0,
        executingCount: 0,
        pausedCount: 0,
        completedCount: 0,
        completionRate: 0,
        highCount: 0,
        mediumCount: 0,
        lowCount: 0
      },
      chartInstances: [],
      todos: [
        { label: '等待审核确认', value: '--' },
        { label: 'AI队列执行中', value: '--' },
        { label: '待修改报告', value: '--' },
        { label: '今日新增任务', value: '--' }
      ]
    }
  },
  computed: {
    approvedCount() {
      const index = (this.assetStats.pieLabels || []).indexOf('审核通过归档')
      return index >= 0 ? (this.assetStats.pieData || [])[index] || 0 : 0
    },
    returnedCount() {
      const index = (this.assetStats.pieLabels || []).indexOf('驳回归档')
      return index >= 0 ? (this.assetStats.pieData || [])[index] || 0 : 0
    }
  },
  created() {
    this.getAssetStats()
    this.getAiStats()
  },
  mounted() {
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    this.chartInstances.forEach(item => item && item.dispose())
  },
  methods: {
    getAssetStats() {
      listAssetStats().then(response => {
        this.assetStats = response.data || this.assetStats
        this.metrics = this.metrics.map(item => {
          if (item.label === '资产记录') {
            return { ...item, value: this.assetStats.totalCount || 0 }
          }
          return item
        })
        this.$nextTick(() => {
          this.renderAssetCharts()
        })
      }).catch(() => {
        this.$nextTick(() => {
          this.renderAssetCharts()
        })
      })
    },
    getAiStats() {
      getAiStats().then(response => {
        this.aiStats = response.data || this.aiStats
        this.metrics = this.metrics.map(item => {
          if (item.label === 'AI任务') {
            return { ...item, value: this.aiStats.totalTaskCount || 0 }
          }
          return item
        })
        this.$nextTick(() => {
          this.renderAiPriorityChart()
        })
      }).catch(() => {
        this.$nextTick(() => {
          this.renderAiPriorityChart()
        })
      })
    },
    renderAssetCharts() {
      this.renderAssetTrendChart()
      this.renderAssetPieChart()
    },
    renderAssetTrendChart() {
      if (!this.$refs.assetTrendChart) {
        return
      }
      const chart = echarts.init(this.$refs.assetTrendChart)
      chart.setOption({
        color: ['#2f88ec', '#ef6d68'],
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        legend: {
          top: 4,
          right: 8,
          icon: 'circle',
          itemWidth: 8,
          itemHeight: 8,
          textStyle: { color: '#626b7a', fontSize: 12 }
        },
        grid: { left: 18, right: 12, top: 40, bottom: 16, containLabel: true },
        xAxis: {
          type: 'category',
          data: this.assetStats.monthLabels || [],
          axisTick: { show: false },
          axisLine: { lineStyle: { color: '#e1e7f0' } },
          axisLabel: { color: '#758094' }
        },
        yAxis: {
          type: 'value',
          splitLine: { lineStyle: { color: '#edf1f6' } },
          axisTick: { show: false },
          axisLine: { show: false },
          axisLabel: { color: '#8b95a6' }
        },
        series: [
          { name: '审核通过', type: 'bar', barWidth: 10, data: this.assetStats.monthApprovedData || [] },
          { name: '驳回归档', type: 'bar', barWidth: 10, data: this.assetStats.monthReturnedData || [] }
        ]
      })
      this.saveChart(chart)
    },
    renderAssetPieChart() {
      if (!this.$refs.assetPieChart) {
        return
      }
      const labels = this.assetStats.pieLabels || []
      const values = this.assetStats.pieData || []
      const chart = echarts.init(this.$refs.assetPieChart)
      chart.setOption({
        color: ['#2f88ec', '#ef6d68'],
        tooltip: { trigger: 'item' },
        legend: {
          bottom: 4,
          left: 'center',
          icon: 'circle',
          itemWidth: 8,
          itemHeight: 8,
          textStyle: { color: '#626b7a', fontSize: 12 }
        },
        series: [
          {
            name: '状态分布',
            type: 'pie',
            radius: ['48%', '72%'],
            center: ['50%', '46%'],
            avoidLabelOverlap: true,
            label: { formatter: '{d}%', color: '#4b5563', fontSize: 12 },
            labelLine: { length: 8, length2: 8 },
            data: labels.map((item, index) => ({
              name: item,
              value: values[index] || 0
            }))
          }
        ]
      })
      this.saveChart(chart)
    },
    renderAiPriorityChart() {
      if (!this.$refs.aiPriorityChart) {
        return
      }
      const chart = echarts.init(this.$refs.aiPriorityChart)
      chart.setOption({
        color: ['#2f88ec', '#4cc3d9', '#ffb44c'],
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          bottom: 0,
          left: 'center',
          icon: 'circle',
          itemWidth: 8,
          itemHeight: 8,
          textStyle: { color: '#626b7a', fontSize: 12 }
        },
        series: [
          {
            name: '优先级分布',
            type: 'pie',
            radius: ['46%', '70%'],
            center: ['50%', '44%'],
            label: { formatter: '{d}%', color: '#4b5563', fontSize: 12 },
            labelLine: { length: 8, length2: 8 },
            data: [
              { value: this.aiStats.highCount || 0, name: '高优先级' },
              { value: this.aiStats.mediumCount || 0, name: '中优先级' },
              { value: this.aiStats.lowCount || 0, name: '低优先级' }
            ]
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
    }
  }
}
</script>

<style scoped lang="scss">
.home-page {
  background: #f4f6f8;
  min-height: calc(100vh - 84px);
  padding-top: 10px;
}

.metric-row {
  margin-bottom: 14px;
}

.metric-card,
.section-card,
.asset-insight-card,
.ai-queue-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 2px;
}

.metric-card {
  min-height: 118px;
  padding: 16px;
  margin-bottom: 14px;
}

.metric-label {
  color: #606266;
  font-size: 14px;
}

.metric-value {
  margin-top: 12px;
  color: #303133;
  font-size: 30px;
  line-height: 1;
  font-weight: 600;
}

.metric-desc {
  margin-top: 12px;
  color: #909399;
  font-size: 13px;
}

.asset-insight-card {
  display: grid;
  grid-template-columns: 330px minmax(0, 1fr);
  gap: 18px;
  padding: 18px;
  margin-bottom: 14px;
}

.asset-insight-main {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 238px;
  border-right: 1px solid #eef1f5;
  padding-right: 18px;
}

.asset-total-label {
  color: #6b7280;
  font-size: 13px;
}

.asset-total-value {
  margin-top: 12px;
  color: #1f2937;
  font-size: 44px;
  line-height: 1;
  font-weight: 700;
}

.asset-total-desc {
  margin-top: 10px;
  color: #8b95a6;
  font-size: 13px;
}

.asset-status-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.asset-status-item {
  height: 72px;
  border: 1px solid #e7ebf1;
  border-radius: 4px;
  padding: 12px;
}

.asset-status-item span {
  display: block;
  color: #6b7280;
  font-size: 13px;
}

.asset-status-item strong {
  display: block;
  margin-top: 9px;
  color: #303133;
  font-size: 24px;
  line-height: 1;
}

.asset-status-item.approved {
  background: #f4f9ff;
}

.asset-status-item.returned {
  background: #fff7f6;
}

.asset-chart-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(260px, 0.8fr);
  gap: 12px;
}

.asset-chart {
  min-height: 238px;
}

.ai-queue-card {
  padding: 18px;
  margin-bottom: 14px;
}

.ai-queue-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18px;
  border-bottom: 1px solid #eef1f5;
  padding-bottom: 16px;
}

.ai-queue-subtitle {
  color: #8b95a6;
  font-size: 13px;
}

.ai-completion {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #6b7280;
  font-size: 13px;
}

.ai-queue-body {
  display: grid;
  grid-template-columns: 230px minmax(320px, 1fr) minmax(260px, 0.9fr);
  gap: 16px;
  align-items: stretch;
  padding-top: 16px;
}

.ai-total-box {
  min-height: 190px;
  border: 1px solid #e7ebf1;
  border-radius: 4px;
  padding: 18px;
  background: #f8fbff;
}

.ai-total-box span,
.ai-total-box em {
  display: block;
  color: #6b7280;
  font-size: 13px;
  font-style: normal;
}

.ai-total-box strong {
  display: block;
  margin: 18px 0 16px;
  color: #1f2937;
  font-size: 42px;
  line-height: 1;
}

.ai-status-list {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.ai-status-item {
  min-height: 190px;
  border: 1px solid #e7ebf1;
  border-radius: 4px;
  padding: 14px;
}

.ai-status-item span {
  display: block;
  color: #6b7280;
  font-size: 13px;
}

.ai-status-item strong {
  display: block;
  margin-top: 16px;
  color: #303133;
  font-size: 30px;
  line-height: 1;
}

.ai-status-item.waiting {
  background: #fffaf2;
}

.ai-status-item.executing {
  background: #f4f9ff;
}

.ai-status-item.paused {
  background: #f7f8fa;
}

.ai-status-item.completed {
  background: #f1fbf5;
}

.ai-priority-chart {
  min-height: 190px;
}

.section-card {
  padding: 16px;
  margin-bottom: 14px;
}

.section-title {
  color: #303133;
  font-size: 16px;
  line-height: 1;
  font-weight: 600;
  margin-bottom: 14px;
}

.todo-list {
  border: 1px solid #ebeef5;
}

.todo-item {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 14px;
  color: #606266;
  font-size: 14px;
}

.todo-item + .todo-item {
  border-top: 1px solid #ebeef5;
}

.todo-item strong {
  color: #303133;
  font-size: 16px;
}

@media (max-width: 768px) {
  .asset-status-grid,
  .asset-chart-grid,
  .ai-status-list,
  .ai-queue-body {
    grid-template-columns: 1fr;
  }

  .ai-queue-head,
  .ai-completion {
    align-items: flex-start;
    flex-direction: column;
  }
}

@media (max-width: 1200px) {
  .asset-insight-card {
    grid-template-columns: 1fr;
  }

  .asset-insight-main {
    border-right: none;
    border-bottom: 1px solid #eef1f5;
    padding-right: 0;
    padding-bottom: 16px;
  }

  .ai-queue-body {
    grid-template-columns: 1fr;
  }
}
</style>
