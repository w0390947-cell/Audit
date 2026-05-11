# AI任务详情-执行流程组件第一阶段实施方案

## 1. 背景

当前 `AI任务详情` 页面中的 `流转状态` 组件主要使用审核任务详情 `reviewDetail.stageList`。这类数据表达的是审核单流转，不是 AI 任务执行过程，和页面目标不完全一致。

本阶段目标是在不改后端接口和数据库的前提下，将该组件调整为面向 AI 任务的执行流程视图。

## 2. 目标

将 `流转状态` 改为 `执行流程`，用现有 AI 任务详情数据 `detail` 推导任务执行阶段，让用户能看到：

- AI 任务当前处于哪一步。
- AI 分析是否等待、执行中、暂停、失败或完成。
- 当前进度和执行说明。
- 是否已经生成检测结果。
- 人工复核是否已经处理。

## 3. 范围

本阶段只修改前端：

- `ruoyi-ui/src/views/audit/ai/detail.vue`

不修改：

- 后端接口。
- 数据库表结构。
- AI 工作流回调逻辑。
- 检测结果展示组件。
- 报告预览组件。

## 4. 当前问题

### 4.1 数据语义不匹配

当前组件优先展示 `reviewDetail.stageList`。该字段来自：

```text
GET /audit/review/{reviewTaskId}?versionId={reviewVersionId}
```

它描述审核任务版本的流转阶段，而不是 AI 任务自身的执行阶段。

### 4.2 默认数据会误导用户

当前前端在无阶段数据时会展示默认阶段：

- 报告上传
- 报告解析
- 报告检测

并包含固定的 2024 年时间和固定说明。这会让用户以为这些步骤真实发生过。

### 4.3 状态展示过于固定

当前所有节点都显示 `已完成`，无法表达：

- `waiting`
- `executing`
- `paused`
- `completed`
- `failed`

## 5. 组件目标形态

建议将组件标题从 `流转状态` 改为：

```text
执行流程
```

阶段固定为四个前端推导节点：

1. 任务入队
2. AI分析执行
3. 结果生成
4. 人工复核

## 6. 阶段推导规则

### 6.1 任务入队

数据来源：

- `detail.submitTime`
- `detail.priority`
- `detail.taskStatus`

展示内容：

- 提交时间。
- 优先级。
- 当前任务状态。

状态规则：

- 如果存在 `detail.aiTaskId` 或 `detail.taskNo`，显示 `已完成`。
- 否则显示 `未开始`。

### 6.2 AI分析执行

数据来源：

- `detail.taskStatus`
- `detail.progressPercent`
- `detail.progressText`
- `detail.estimatedDuration`

展示内容：

- 当前进度百分比。
- 执行说明。
- 预计执行时间。

状态规则：

| taskStatus | 展示状态 |
| --- | --- |
| waiting | 等待中 |
| executing | 执行中 |
| paused | 已暂停 |
| completed | 已完成 |
| failed | 已失败 |
| 其他空值 | 未开始 |

### 6.3 结果生成

数据来源：

- `detail.taskStatus`
- `detail.findingList`
- `detail.aiSummary`

展示内容：

- 检测结果数量。
- AI 摘要。

状态规则：

- `detail.findingList.length > 0`：已完成。
- `detail.taskStatus === 'completed'`：已完成，但提示 `未发现异常或暂无结构化问题`。
- `detail.taskStatus === 'failed'`：已失败。
- `detail.taskStatus === 'executing'`：等待生成。
- 其他状态：未开始。

### 6.4 人工复核

数据来源：

- `detail.reviewStatus`
- `detail.reviewOpinion`
- `detail.reviewer`
- `detail.updateTime`

展示内容：

- 复核状态。
- 复核意见。
- 复核人。

状态规则：

| reviewStatus | 展示状态 |
| --- | --- |
| approved | 已通过 |
| pending | 待修改 |
| 其他空值 | 待复核 |

## 7. 前端实现建议

### 7.1 新增计算属性

在 `detail.vue` 中新增：

```js
aiFlowStageList() {
  return [
    this.buildQueueStage(),
    this.buildAnalysisStage(),
    this.buildResultStage(),
    this.buildReviewStage()
  ]
}
```

将模板中的：

```js
displayStageList
```

替换为：

```js
aiFlowStageList
```

### 7.2 新增阶段构造方法

建议新增方法：

- `buildQueueStage()`
- `buildAnalysisStage()`
- `buildResultStage()`
- `buildReviewStage()`
- `flowStatusLabel(status)`
- `flowStatusClass(status)`
- `taskStatusLabel(taskStatus)`

阶段对象结构建议：

```js
{
  stageCode: 'analysis',
  stageName: 'AI分析执行',
  status: 'running',
  statusText: '执行中',
  timeText: '--',
  summary: 'AI审核工作流分析中',
  lines: [
    '执行进度：35%',
    '预计执行时间：3分钟'
  ]
}
```

### 7.3 状态枚举

前端内部建议使用以下状态：

| 内部状态 | 含义 |
| --- | --- |
| done | 已完成 |
| running | 执行中 |
| waiting | 等待中 |
| paused | 已暂停 |
| failed | 已失败 |
| pending | 待处理 |

### 7.4 样式建议

节点样式按状态区分：

| 状态 | 建议颜色 |
| --- | --- |
| done | `#67c23a` |
| running | `#409eff` |
| waiting | `#909399` |
| paused | `#e6a23c` |
| failed | `#f56c6c` |
| pending | `#c0c4cc` |

当前 `.stage-node`、`.stage-status-icon`、`.stage-status-text` 可以增加状态 class：

```html
<div :class="['stage-node', 'stage-' + item.status]">
```

### 7.5 删除默认假数据

建议移除或停止使用：

- `defaultStageList()`
- 默认 2024 年阶段时间。
- 固定的 PDF 大小、MinIO 路径、字段数量等文案。

没有真实数据时，统一展示：

```text
--
```

或：

```text
等待执行
```

## 8. 页面文案建议

原文案：

```text
流转状态
```

建议改为：

```text
执行流程
```

原文案：

```text
处理日志
```

第一阶段没有真实日志接口，建议先隐藏按钮，或改为不可点击文本：

```text
暂无日志
```

## 9. 验收标准

### 9.1 等待中任务

当 `detail.taskStatus === 'waiting'`：

- `任务入队` 显示已完成。
- `AI分析执行` 显示等待中。
- `结果生成` 显示未开始。
- `人工复核` 显示待复核。

### 9.2 执行中任务

当 `detail.taskStatus === 'executing'`：

- `AI分析执行` 显示执行中。
- 展示 `progressPercent` 和 `progressText`。
- `结果生成` 显示等待生成。

### 9.3 已完成任务

当 `detail.taskStatus === 'completed'`：

- `任务入队`、`AI分析执行`、`结果生成` 显示已完成。
- `结果生成` 展示 `findingList.length`。
- 如果有 `aiSummary`，展示 AI 摘要。

### 9.4 失败任务

当 `detail.taskStatus === 'failed'`：

- `AI分析执行` 或 `结果生成` 应显示已失败。
- 页面不能继续显示所有节点已完成。

### 9.5 无审核详情数据

当 `/audit/review/{reviewTaskId}` 请求失败或无数据：

- 执行流程仍可基于 `detail` 正常展示。
- 不出现前端写死的假时间和假步骤。

## 10. 风险和限制

第一阶段的阶段数据由前端推导，不能表达真实工作流内部步骤。例如：

- 哪个智能体执行失败。
- 每个步骤开始和结束时间。
- 每个步骤的日志。
- 工作流返回的原始结构化结果。

这些能力应在第二阶段由后端提供真实数据后补齐。
