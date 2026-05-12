# AI审核工作流结果回传问题排查说明

## 背景

业务系统中新增审核任务后，会自动创建一条 AI 审核队列任务。本次问题任务信息如下：

- 审核任务编号：`SF-1778477584773`
- 产品名称：`本安-矿用本安型手机`
- 页面现象：`AI审核管理 -> AI任务队列` 显示 `AI分析完成，建议人工复核`
- 页面现象：点击 `详情` 后，`AI任务详情 -> 检测结果` 显示为空或暂无检测结果

业务系统当前配置使用外部工作流：

```yaml
audit-workflow:
  enabled: true
  base-url: http://127.0.0.1:8080
  workflow-code: policy_document_audit
  uploaded-basis-workflow-code: uploaded_basis_document_audit
```

## 初步判断

该现象更符合工作流结果接口返回格式不完整或字段位置不符合当前业务系统解析协议。

原因是：

1. 队列中的 `AI分析完成，建议人工复核` 来自业务系统表 `audit_ai_task.progress_text`。
2. 这个状态可以仅由工作流返回的 `summary` 文本触发。
3. 详情页 `检测结果` 读取的是业务系统表 `audit_ai_finding`。
4. `audit_ai_finding` 只会在工作流结果中存在结构化 `findings` 或 `issues` 数组时写入。
5. 如果工作流只返回 `summary`，或者把检测项放在当前业务系统不识别的位置，业务系统会显示任务完成，但详情检测结果为空。

## 当前业务系统读取的结果接口

业务系统在工作流任务成功后，会调用：

```http
GET /api/audit/tasks/{workflowTaskId}/result
```

业务系统要求响应体满足：

```json
{
  "code": 200,
  "data": {
    "summary": "本次AI分析结论文本",
    "findings": [
      {
        "type": "问题类型",
        "title": "问题标题",
        "content": "问题详细描述",
        "location": "问题位置",
        "suggestion": "修改建议",
        "severity": "medium"
      }
    ]
  }
}
```

也兼容把 `findings` 字段命名为 `issues`：

```json
{
  "code": 200,
  "data": {
    "summary": "本次AI分析结论文本",
    "issues": [
      {
        "type": "问题类型",
        "title": "问题标题",
        "content": "问题详细描述",
        "location": "问题位置",
        "suggestion": "修改建议"
      }
    ]
  }
}
```

## 字段映射要求

工作流侧至少需要返回：

| 字段 | 是否必需 | 说明 |
| --- | --- | --- |
| `code` | 是 | 必须为 `200`，否则业务系统认为查询结果失败 |
| `data.summary` | 建议 | 写入 `audit_ai_task.ai_summary`，也会影响队列进度文案 |
| `data.findings` 或 `data.issues` | 是 | 必须是数组；数组为空时，详情页无检测项 |
| `type` | 建议 | 写入问题类型；缺失时业务系统兜底为 `AI审核问题` |
| `title` | 建议 | 写入问题标题；缺失时业务系统兜底为 `AI发现问题` |
| `content` | 是 | 写入问题正文；缺失时详情页没有有效检测内容 |
| `location` | 可选 | 业务系统会拼接到检测内容中 |
| `suggestion` | 可选 | 业务系统会拼接到检测内容中 |
| `severity` 或 `risk_level` | 可选 | 当前主要用于后续扩展，建议保留 |

## 需要工作流侧重点排查的情况

请工作流系统侧检查任务 `SF-1778477584773` 对应的工作流结果，重点确认以下几点：

1. `GET /api/audit/tasks/{workflowTaskId}/result` 是否返回了 `data.findings` 或 `data.issues`。
2. `findings` 或 `issues` 是否为 JSON 数组，而不是字符串。
3. 检测项是否被放在了嵌套字段中，例如 `data.result.findings`、`data.output.findings`、`data.content.findings`。
4. 检测项是否被包在 Markdown 代码块或大模型文本中，例如 `data.result = "```json ... ```"`。
5. 检测项字段名是否使用了业务系统当前不识别的名称，例如 `problemList`、`riskItems`、`checkResults`。
6. 工作流任务状态为 `SUCCESS` 时，结果接口是否已经完成结果落库，避免业务系统轮询到成功后立即查询结果但结构化结果尚未生成。

## 容易导致当前现象的返回示例

以下返回会导致队列显示完成，但详情检测结果为空：

```json
{
  "code": 200,
  "data": {
    "summary": "AI分析完成，建议人工复核。报告中存在若干风险信号。"
  }
}
```

以下返回也可能导致详情检测结果为空，因为当前业务系统只读取 `data.findings` 或 `data.issues`：

```json
{
  "code": 200,
  "data": {
    "summary": "AI分析完成，建议人工复核。",
    "result": {
      "findings": [
        {
          "type": "内容缺失",
          "title": "缺少关键检测结论",
          "content": "报告未给出明确检测结论。"
        }
      ]
    }
  }
}
```

## 建议工作流侧修正后的返回示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "summary": "本次报告存在需要人工复核的问题，建议重点核查检测结论、依据文件一致性和关键参数。",
    "findings": [
      {
        "type": "依据不一致",
        "title": "检测依据与报告结论存在不一致",
        "content": "报告中的部分检测结论与依据文件描述不完全一致，需要人工复核。",
        "location": "报告检测结论章节",
        "suggestion": "请核对报告结论、检测依据文件和原始检测记录，必要时补充说明。",
        "severity": "medium"
      },
      {
        "type": "内容缺失",
        "title": "缺少关键参数说明",
        "content": "报告中未完整说明部分关键防爆/本安参数。",
        "location": "产品参数章节",
        "suggestion": "补充关键参数来源、检测值和判定依据。",
        "severity": "low"
      }
    ]
  }
}
```

## 业务系统侧可验证 SQL

工作流侧修复后，业务系统侧可以检查是否已写入结构化检测项：

```sql
select ai_task_id, task_no, product_name, task_status, progress_text, ai_analysis_count, ai_summary
from audit_ai_task
where task_no = 'SF-1778477584773';

select f.finding_id, f.ai_task_id, f.finding_type, f.finding_title, f.finding_content, f.sort_num
from audit_ai_finding f
join audit_ai_task t on t.ai_task_id = f.ai_task_id
where t.task_no = 'SF-1778477584773'
order by f.sort_num, f.finding_id;
```

如果第一条 SQL 中任务为 `completed`，但第二条 SQL 无记录，则说明工作流结果没有被业务系统解析成结构化检测项。

## 结论

请工作流系统侧优先确认并修正结果接口 `/api/audit/tasks/{workflowTaskId}/result` 的返回结构，确保结构化检测项位于 `data.findings` 或 `data.issues` 数组中。只返回 `summary` 无法支撑业务系统详情页的 `检测结果` 展示。
