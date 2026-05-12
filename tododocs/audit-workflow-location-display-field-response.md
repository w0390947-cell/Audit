# AI审核工作流问题定位与展示字段处理说明

日期：2026-05-12

本文档面向业务系统侧，说明工作流系统针对 `location` 字段展示问题的处理结果。目标是让业务系统既能使用结构化字段完成报告预览定位，又能使用用户友好的字段展示检测结果，避免页面出现 JSON、`source_chunk_id` 等内部技术字段。

## 1. 本次处理结论

工作流系统已完成以下调整：

1. 每条有效 `finding` 会返回顶层 `quote`。
2. 每条有效 `finding` 会返回顶层 `location_text`。
3. `location` 保持结构化对象，用于机器定位，不建议直接展示给用户。
4. `source_chunk_id`、`source_chunk_no` 已从对外 `location` 中移除，并转入 `debug`。
5. `summary`、`totalIssues`、`findings.length`、`diagnostics.valid_issue_count` 已按有效问题数量统一。
6. 两个工作流的实际生效 prompt 已更新，要求模型直接输出 `quote`、`location_text`、结构化 `location` 和 `debug`。

涉及工作流：

- `policy_document_audit`
- `uploaded_basis_document_audit`

## 2. 新返回结构

结果查询接口：

```http
GET /api/audit/tasks/{taskId}/result
```

返回中的 `data.findings[]` 将按以下结构输出：

```json
{
  "type": "标准不符",
  "title": "委托编号与页眉不一致",
  "content": "报告正文中样品编号栏标注为'20250409002 (2025520398-2)'，而报告页眉及任务书号引用的委托编号为'2025520398FB'，两者后缀标识不一致。",
  "quote": "样品编号 20250409002 (2025520398-2)",
  "location_text": "第1页，样品信息表",
  "location": {
    "page": 1,
    "pageNo": 1,
    "page_no": 1,
    "section": "样品信息表",
    "quote": "样品编号 20250409002 (2025520398-2)"
  },
  "debug": {
    "source_chunk_id": 519,
    "source_chunk_no": 1
  },
  "basis": [
    {
      "file_name": "依据文件名.docx",
      "quote": "依据原文短引用",
      "kb_chunk_id": "KB-001"
    }
  ],
  "severity": "medium",
  "confidence": 0.95,
  "suggestion": "核对委托编号规则，统一报告页眉与正文中的委托编号后缀标识。"
}
```

终态 callback 的 `result.findings[]` 与结果查询接口 `data.findings[]` 字段语义保持一致。

## 3. 字段说明

| 字段 | 说明 | 建议业务系统用途 |
| --- | --- | --- |
| `quote` | 待审报告中的问题原文引用，不是知识库依据文本 | 展示“原文引用” |
| `location_text` | 用户可读位置文案，例如 `第1页，样品信息表` | 展示检测结果位置 |
| `location` | 机器可读定位对象 | 跳页、后续高亮、系统定位 |
| `location.page` | 报告预览页码，从 1 开始 | 报告预览跳页 |
| `location.pageNo` | 页码兼容字段，值与 `page` 一致 | 兼容读取 |
| `location.page_no` | 页码兼容字段，值与 `page` 一致 | 兼容读取 |
| `location.section` | 问题所在章节、栏目、表格或区域 | 可作为定位辅助 |
| `location.quote` | 定位对象中的原文锚点，与顶层 `quote` 语义一致 | 后续页内搜索或高亮 |
| `debug.source_chunk_id` | 工作流内部来源分片 ID | 联调排查，不建议展示 |
| `debug.source_chunk_no` | 工作流内部来源分片序号 | 联调排查，不建议展示 |
| `basis[*].quote` | 知识库、标准或依据文件中的依据原文 | 展示审核依据时使用 |

## 4. 展示建议

业务系统检测结果卡片建议使用：

- 位置：`finding.location_text`
- 原文引用：`finding.quote`
- 跳页页码：`finding.location.page`
- 问题描述：`finding.content`
- 修改建议：`finding.suggestion`

不建议把 `finding.location` 整体直接渲染到页面上。

不建议向终端用户展示：

- `debug`
- `source_chunk_id`
- `source_chunk_no`
- `kb_chunk_id`

## 5. 计数字段规则

工作流侧已按以下规则统一：

```text
totalIssues = findings.length = diagnostics.valid_issue_count
diagnostics.candidate_count = diagnostics.valid_issue_count + diagnostics.filtered_count
```

如果存在被过滤候选问题，`summary` 会区分有效问题和过滤候选问题，例如：

```text
本次审核发现3个有效问题，另有1个候选问题经结果校验过滤。
```

不会再把候选问题数量写成最终有效问题数量。

## 6. 页码说明

工作流系统当前页码来源规则：

1. PDF 文件：按 PDF 真实页码返回，页码从 1 开始。
2. DOC/DOCX 文件：如果业务系统传入 `preview_pdf_url`、`previewPdfUrl`、`pdf_url` 或 `pdfUrl`，工作流会优先解析该预览 PDF，并返回与预览 PDF 对应的页码。
3. DOC/DOCX 文件如果没有预览 PDF，当前解析链路无法稳定获取与预览一致的页码，此时不编造页码，但仍返回 `location_text`。

如果页码不可用，工作流会在顶层 `warnings` 中返回：

```json
{
  "type": "page_location_unavailable",
  "message": "当前文件解析链路无法取得与预览 PDF 一致的页码，终态 finding 不返回 location.page。",
  "file_type": "docx",
  "page_location_source": "unsupported_word_pagination"
}
```

## 7. 生效范围

本次处理对新创建并重新执行的工作流任务生效。

说明：

- 数据库中的工作流 prompt 已更新。
- 代码改动需要工作流服务重启后生效。
- 历史任务结果不会自动重算；如需验证新字段，请重新触发审核任务。

## 8. 联调验收点

建议业务系统侧按以下点验收：

1. `data.findings[*].location_text` 非空，且不是 JSON 字符串。
2. `data.findings[*].quote` 非空，且来自待审报告原文。
3. `data.findings[*].location` 是结构化对象。
4. 有可靠页码时，`data.findings[*].location.page` 为从 1 开始的数字。
5. 页面不再展示 `source_chunk_id`、`source_chunk_no` 等内部字段。
6. `data.totalIssues = data.findings.length`。
7. `data.diagnostics.valid_issue_count = data.findings.length`。
8. 终态 callback 的 `result.findings[]` 与结果查询接口 `data.findings[]` 字段一致。

