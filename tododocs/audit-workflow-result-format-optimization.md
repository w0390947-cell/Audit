# AI审核工作流结果返回格式优化说明

日期：2026-05-12

本文档面向工作流系统侧，用于基于本次 `scripts/test_audit_workflow_result.py` 测试返回结果，规范工作流最终结果的数据格式。测试返回样本保存于 `tododocs/back.md`。

## 1. 本次测试结论

本次脚本调用的是工作流系统结果查询接口：

```text
GET /api/audit/tasks/{task_id}/result
```

当前接口返回 HTTP 层与业务包装层均成功：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "summary": "未发现关键问题；AI审核生成21个候选问题，经结果校验过滤21个。",
    "findings": [],
    "totalIssues": 0
  }
}
```

但结果不可用于业务系统展示检测问题，原因是：

- `data.findings` 为空。
- `data.totalIssues = 0`。
- `diagnostics.candidate_count = 21`，但 `diagnostics.valid_issue_count = 0`。
- 21 个候选问题全部进入 `diagnostics.filtered_findings`。
- 过滤原因中 20 条为 `finding_quote_missing`，1 条为 `finding_content_incomplete`。

因此当前返回不是“未发现问题”，而是“发现了候选问题，但候选问题未满足工作流侧结果校验规则，最终没有形成可保存的有效问题”。

## 2. 当前返回中的主要缺口

### 2.1 候选问题缺少报告原文 quote

当前 `filtered_findings` 中问题已具备：

- `title`
- `content`
- `severity`
- `location.section`
- `location.source_chunk_id`
- `location.source_chunk_no`

但缺少可用于定位和复核的报告原文引用字段：

```json
{
  "quote": "报告原文中的短句或短段"
}
```

当前工作流校验规则会因缺少 `quote` 将候选问题过滤为：

```json
{
  "reason": "finding_quote_missing",
  "message": "候选问题缺少报告原文 quote，无法定位问题原文"
}
```

请工作流侧在 AI 审核生成、合并、校验、保存各环节保证每条有效 finding 都有 `quote`。

### 2.2 缺少业务预览可用的页码字段

当前返回中 `location` 主要是：

```json
{
  "section": "报告页眉及委托编号栏",
  "source_chunk_id": 479,
  "source_chunk_no": 1
}
```

业务系统不会把 `source_chunk_id` 或 `source_chunk_no` 当成报告页码。请返回从 1 开始的报告页码：

```json
{
  "location": {
    "page": 1,
    "pageNo": 1,
    "page_no": 1
  }
}
```

推荐至少稳定返回 `location.page`。`pageNo`、`page_no` 可以作为兼容字段同步返回。

### 2.3 候选问题描述存在截断或不完整

样本中存在候选问题内容以半句话结束，例如：

```text
待审阅报告元信息显示产品名称为“本安 - 矿用本安型手机”，而知识库搜索返回的审核依据（产品说明书及检验报告）中明确记载的产品名称为
```

这类问题会被过滤为：

```json
{
  "reason": "finding_content_incomplete",
  "message": "候选问题描述不完整，无法形成可保存的有效问题"
}
```

请工作流侧在模型输出、分片合并和 JSON 截断处理后增加完整性检查，避免将半句话作为候选问题进入最终校验。

### 2.4 诊断字段与最终结果字段重复较多

当前 `diagnostics.filtered_findings` 与 `validation_warnings` 内容基本重复。建议保留一个主诊断字段即可：

- 推荐保留：`diagnostics.filtered_findings`
- 可选保留：`validation_warnings`

业务系统最终展示和保存只依赖 `findings` 或 `issues`，不会从 `diagnostics.filtered_findings` 中恢复问题。

## 3. 目标结果格式

### 3.1 结果查询接口返回格式

`GET /api/audit/tasks/{task_id}/result` 建议返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "summary": "本次AI审核发现2个有效问题，建议人工复核并修订报告。",
    "totalIssues": 2,
    "findings": [
      {
        "type": "数据错误",
        "title": "检验结论签发日期未填写",
        "content": "报告检验结论部分签发日期为空，不符合检验报告完整性要求。",
        "quote": "签发日期：年 月 日",
        "severity": "high",
        "location": {
          "page": 1,
          "pageNo": 1,
          "page_no": 1,
          "section": "检验结论"
        },
        "suggestion": "补充实际签发日期，并确保签发日期与报告流程记录一致。"
      },
      {
        "type": "内容缺失",
        "title": "关键人员签字缺失",
        "content": "报告签署栏缺少主检、审核或批准人签字，不符合报告有效性要求。",
        "quote": "批准：    审核：    主检：",
        "severity": "high",
        "location": {
          "page": 1,
          "pageNo": 1,
          "page_no": 1,
          "section": "报告签署栏"
        },
        "suggestion": "补齐主检、审核、批准人签字或电子签章。"
      }
    ],
    "diagnostics": {
      "candidate_count": 21,
      "valid_issue_count": 2,
      "filtered_count": 19,
      "filter_reasons": [
        {
          "reason": "finding_quote_missing",
          "count": 18,
          "message": "候选问题缺少报告原文 quote，无法定位问题原文"
        },
        {
          "reason": "finding_content_incomplete",
          "count": 1,
          "message": "候选问题描述不完整，无法形成可保存的有效问题"
        }
      ]
    },
    "model_used": "qwen3.5-plus"
  }
}
```

要求：

- `data.findings` 只放通过校验、可保存、可展示的有效问题。
- `data.totalIssues` 必须等于 `data.findings.length`。
- `data.summary` 应基于有效问题数量生成，不应把“候选问题全部过滤”描述成“未发现关键问题”。
- `diagnostics` 只用于排查，不作为业务系统保存问题的数据源。

### 3.2 终态回调 result 格式

如果工作流侧向业务系统发送终态回调，请在回调 payload 的 `result` 中放置与 `data` 等价的结果对象，不要把 `code/message/data` 外层包装塞进 `result`。

推荐格式：

```json
{
  "callback_event_id": "wf-audit-123-success",
  "biz_id": "AI-TASK-44",
  "workflow_task_id": "123",
  "workflow_task_no": "AUDIT-20260512-000001",
  "task_status": "SUCCESS",
  "status": "completed",
  "progress_percent": 100,
  "progress_text": "AI审核工作流执行完成",
  "result": {
    "success": true,
    "summary": "本次AI审核发现2个有效问题，建议人工复核并修订报告。",
    "totalIssues": 2,
    "findings": [
      {
        "type": "数据错误",
        "title": "检验结论签发日期未填写",
        "content": "报告检验结论部分签发日期为空，不符合检验报告完整性要求。",
        "quote": "签发日期：年 月 日",
        "severity": "high",
        "location": {
          "page": 1,
          "pageNo": 1,
          "page_no": 1,
          "section": "检验结论"
        },
        "suggestion": "补充实际签发日期，并确保签发日期与报告流程记录一致。"
      }
    ],
    "diagnostics": {
      "candidate_count": 21,
      "valid_issue_count": 2,
      "filtered_count": 19
    }
  }
}
```

业务系统读取终态回调时，优先解析：

1. `result.findings`
2. 如果 `result.findings` 为空，再解析 `result.issues`

因此请工作流侧优先使用 `result.findings`。

## 4. finding 字段契约

每条有效 finding 推荐字段如下：

| 字段 | 是否必填 | 说明 |
| --- | --- | --- |
| `type` | 建议必填 | 问题类型，例如 `数据错误`、`内容缺失`、`格式错误`、`标准不符`、`其他`。 |
| `title` | 必填 | 问题标题，短句，适合列表展示。 |
| `content` | 必填 | 问题说明，必须是完整句子，不应截断。 |
| `quote` | 必填 | 待审报告原文短引用，不是知识库依据文本。 |
| `severity` | 建议必填 | 严重程度，建议使用 `high`、`medium`、`low`。 |
| `location.page` | 强烈建议必填 | 报告预览页码，从 1 开始。 |
| `location.section` | 可选 | 章节、栏目或区域描述。 |
| `suggestion` | 建议必填 | 修订建议。 |

兼容字段：

- 问题数组：`findings`、`issues`
- 问题类型：`type`、`finding_type`
- 问题标题：`title`、`finding_title`
- 问题内容：`content`、`finding_content`、`problem`
- 原文引用：`quote`、`evidence_quote`、`source_quote`、`original_text`、`location.quote`
- 页码：`location.page`、`location.pageNo`、`location.page_no`、`page`、`pageNo`、`page_no`

虽然业务系统兼容多个别名，但工作流侧推荐统一输出：

```json
{
  "quote": "报告原文短引用",
  "location": {
    "page": 1
  }
}
```

## 5. quote 生成规则

`quote` 应满足以下规则：

1. 必须来自待审报告原文，不应来自知识库、说明书或标准依据。
2. 应是能在报告页内找到的短原文，建议 10 到 120 个中文字符。
3. 如果问题来自表格，应引用表格中的关键单元格文本或同一行的关键组合文本。
4. 如果问题是“缺少签字/盖章/日期”，应引用报告中对应空栏、签署栏、日期栏附近原文。
5. 如果报告原文没有直接可引用文本，不建议生成有效 finding；可进入 `diagnostics.filtered_findings` 并说明原因。

示例：

```json
{
  "title": "检验结论签发日期未填写",
  "content": "报告检验结论部分签发日期为空，不符合检验报告完整性要求。",
  "quote": "签发日期：年 月 日",
  "location": {
    "page": 1,
    "section": "检验结论"
  }
}
```

## 6. location 生成规则

`location.page` 应与业务系统报告预览中的页码一致：

1. 页码从 `1` 开始。
2. 如果工作流基于 PDF 解析，则返回 PDF 页码。
3. 如果原始文件是 doc/docx，但业务系统预览为转换后的 PDF，请尽量基于最终预览 PDF 页码返回。
4. `source_chunk_id`、`source_chunk_no` 可保留用于诊断，但不能替代 `location.page`。
5. 无法确认页码时，不要猜测页码；可以保留 finding，但应返回 `location.section`，并在诊断中说明页码缺失。

推荐结构：

```json
{
  "location": {
    "page": 1,
    "pageNo": 1,
    "page_no": 1,
    "section": "报告签署栏",
    "source_chunk_id": 480,
    "source_chunk_no": 2
  }
}
```

## 7. 诊断字段建议

诊断信息建议放在 `diagnostics` 下：

```json
{
  "diagnostics": {
    "candidate_count": 21,
    "valid_issue_count": 2,
    "filtered_count": 19,
    "filter_reasons": [
      {
        "reason": "finding_quote_missing",
        "count": 18,
        "message": "候选问题缺少报告原文 quote，无法定位问题原文"
      }
    ],
    "filtered_findings": []
  }
}
```

要求：

- `candidate_count = valid_issue_count + filtered_count`。
- `valid_issue_count = findings.length`。
- `totalIssues = findings.length`。
- `filtered_findings` 只用于问题排查，不应被业务系统当成有效检测结果。
- 如保留 `validation_warnings`，应与 `diagnostics.filtered_findings` 保持一致，避免两个字段内容冲突。

## 8. 摘要文案规则

请按最终有效问题数量生成 `summary`：

| 场景 | 推荐 summary |
| --- | --- |
| 有有效问题 | `本次AI审核发现 N 个有效问题，建议人工复核并修订报告。` |
| 无候选问题 | `本次AI审核未发现关键问题。` |
| 有候选但全部过滤 | `AI审核生成 N 个候选问题，但因缺少原文引用或内容不完整，未形成有效检测结果，请检查工作流输出质量。` |
| 工作流失败 | `AI审核工作流执行失败：{失败原因}` |

不要在“候选问题全部被过滤”的情况下仅返回 `未发现关键问题`，否则业务侧和用户会误判审核结论。

## 9. 工作流侧建议调整点

请工作流系统侧优先处理以下事项：

1. 在 AI 审核提示词或结构化输出 schema 中强制每条 finding 输出 `quote`。
2. 在分片模式下，从 `source_chunk_id` 对应的待审报告分片中补齐 `quote`，不要使用知识库依据文本作为 `quote`。
3. 在结果校验前补齐 `location.page`，至少保证能从分片页码或预览 PDF 页码得到页码。
4. 在结果合并阶段去重，避免同类“产品名称与依据不一致”重复出现多条。
5. 对 `content` 做完整性检查，过滤或重生成明显截断的半句话。
6. 将有效问题放入 `findings`，将被过滤问题放入 `diagnostics.filtered_findings`，不要混用。
7. 保证计数字段一致：`totalIssues = findings.length = valid_issue_count`。
8. 优化 `summary`，区分“真的没发现问题”和“候选问题全部被过滤”。

## 10. 验收标准

下一次联调可按以下标准验收：

1. `GET /api/audit/tasks/{task_id}/result` 返回 `code = 200`。
2. 如果存在有效问题，`data.findings.length > 0`。
3. `data.totalIssues = data.findings.length`。
4. 每条 `data.findings[*].title`、`content`、`quote` 非空。
5. 每条 `data.findings[*].quote` 来自待审报告原文。
6. 每条主要问题具备 `location.page`，页码从 1 开始。
7. `diagnostics.valid_issue_count = data.findings.length`。
8. `diagnostics.candidate_count = diagnostics.valid_issue_count + diagnostics.filtered_count`。
9. 终态回调的 `result.findings` 与结果查询接口 `data.findings` 语义一致。
10. “候选问题全部过滤”时，`summary` 明确说明过滤原因，不再简单表述为“未发现关键问题”。
