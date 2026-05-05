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

    <el-row :gutter="14">
      <el-col :xs="24" :lg="12">
        <div class="section-card">
          <div class="section-title">快捷入口</div>
          <div class="quick-grid">
            <div class="quick-item" v-for="item in quickLinks" :key="item.path" @click="goTo(item.path)">
              <svg-icon :icon-class="item.icon" class-name="quick-icon" />
              <span>{{ item.title }}</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="12">
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
export default {
  name: 'Home',
  data() {
    return {
      metrics: [
        { label: '审核任务', value: '--', desc: '当前审核列表总览' },
        { label: 'AI任务', value: '--', desc: 'AI队列处理情况' },
        { label: '资源入库', value: '--', desc: '审核资源沉淀' },
        { label: '待处理', value: '--', desc: '需要关注的事项' }
      ],
      quickLinks: [
        { title: '审核列表', path: '/audit/review', icon: 'list' },
        { title: 'AI任务队列', path: '/audit-ai/queue', icon: 'education' },
        { title: '审核资源列表', path: '/audit-asset/list', icon: 'table' },
        { title: '菜单管理', path: '/system/menu', icon: 'tree-table' }
      ],
      todos: [
        { label: '等待审核确认', value: '--' },
        { label: 'AI队列执行中', value: '--' },
        { label: '待修改报告', value: '--' },
        { label: '今日新增任务', value: '--' }
      ]
    }
  },
  methods: {
    goTo(path) {
      this.$router.push(path)
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
.section-card {
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

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.quick-item {
  height: 54px;
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 0 14px;
  color: #303133;
  font-size: 14px;
  cursor: pointer;
}

.quick-item:hover {
  color: #409eff;
  border-color: #c6e2ff;
  background: #f5faff;
}

.quick-icon {
  width: 18px;
  height: 18px;
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
  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
