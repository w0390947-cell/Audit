# AI审核工作流结果接口修复与业务系统侧确认事项

## 本次工作流侧修复

已按 `audit-workflow-result-contract.md` 的要求调整结果查询接口：

```http
GET /api/audit/tasks/{workflowTaskId}/result
```

接口响应仍保持外层统一格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

`data` 中现在会稳定返回以下字段：

```json
{
  "summary": "本次AI分析结论文本",
  "findings": [],
  "issues": [],
  "totalIssues": 0,
  "total_issues": 0,
  "success": true
}
```

其中：

- `data.summary` 保证为字符串。
- `data.findings` 保证为数组。
- `data.issues` 保证为数组。
- `findings[].content` 会从内部 `content`、`problem`、`description`、`title` 中兜底生成。
- `findings[].severity` 会从内部 `severity` 或 `risk_level` 中兜底生成。
- 内部旧格式 `issues` 会转换出业务系统可识别的 `findings`。
- 内部新格式 `findings` 也会同步转换出兼容的 `issues`。

这样即使历史结果只保存了内部 `issues` 结构，业务系统再次调用结果接口时也能读取到顶层 `data.findings`。

## 建议业务系统侧复测

请业务系统侧重新创建一条 AI 审核任务，等待工作流任务成功后调用：

```http
GET /api/audit/tasks/{workflowTaskId}/result
```

重点检查响应中的：

```json
{
  "code": 200,
  "data": {
    "summary": "...",
    "findings": [
      {
        "type": "...",
        "title": "...",
        "content": "...",
        "location": "...",
        "suggestion": "...",
        "severity": "medium"
      }
    ]
  }
}
```

如果 `data.findings` 数组非空，业务系统应写入 `audit_ai_finding`。

## 需要业务系统侧确认

请确认业务系统轮询到工作流任务 `SUCCESS` 后，实际调用的结果地址使用的是工作流返回的 `workflowTaskId`：

```http
GET http://127.0.0.1:8080/api/audit/tasks/{workflowTaskId}/result
```

请同时确认业务系统当前只读取：

- `data.findings`
- 或 `data.issues`

不要读取嵌套字段，例如：

- `data.result.findings`
- `data.output.findings`
- `data.content.findings`

## 如果复测仍为空

请业务系统侧提供以下两项信息：

1. 工作流任务 ID，即创建工作流任务接口返回的 `data.taskId`。
2. 业务系统调用结果接口时拿到的完整响应 JSON。

工作流侧可用以下命令核对结果：

```bat
curl.exe http://127.0.0.1:8080/api/audit/tasks/{workflowTaskId}/result
```

也可以查询工作流库：

```sql
SELECT task_id, task_no, workflow_code, task_status, summary
FROM audit_task
WHERE task_id = {workflowTaskId};

SELECT result_id, task_id, total_issues, summary, result_json
FROM audit_result
WHERE task_id = {workflowTaskId};
```
