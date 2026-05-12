# AI审核工作流问题定位与用户展示字段优化需求说明

日期：2026-05-12

本文档面向工作流系统侧，用于说明当前 AI 审核结果返回中 `location` 字段的使用问题，并提出字段拆分需求。目标是让业务系统既能用结构化字段完成报告预览定位，又能向用户展示自然、可读的位置文案，避免页面出现 JSON、`source_chunk_id` 等技术细节。

当前返回样本见：

```text
tododocs/back.md
```

## 1. 背景

业务系统“AI任务详情 -> 检测结果”组件需要展示每条审核问题，包括：

- 问题类型
- 问题标题
- 问题描述
- 报告页码
- 原文引用
- 用户可读的位置说明
- 修改建议

同时，业务系统还需要结构化定位数据，用于：

- 点击检测结果后跳转报告预览页码。
- 后续支持原文高亮、页内定位。
- 排查工作流分片来源。

当前工作流返回的 `location` 同时承载了“机器定位”和“展示文案”两种语义，导致业务系统在保存或展示时容易把完整 JSON 暴露给用户。

## 2. 当前返回现状

当前 `tododocs/back.md` 中有效 finding 示例：

```json
{
  "type": "标准不符",
  "title": "委托编号与页眉不一致",
  "content": "报告正文中样品编号栏标注为'20250409002 (2025520398-2)'，而报告页眉及任务书号引用的委托编号为'2025520398FB'，两者后缀标识不一致（'-2'与'FB'），不符合委托编号一致性要求。",
  "location": {
    "quote": "样品编号 20250409002 (2025520398-2)",
    "section": "样品信息表",
    "source_chunk_id": 519,
    "source_chunk_no": 1
  },
  "severity": "medium",
  "confidence": 0.95,
  "suggestion": "核对委托编号规则，统一报告页眉与正文中的委托编号后缀标识。"
}
```

当前返回存在以下问题：

1. `location` 是结构化对象，不适合直接展示给用户。
2. `location` 内包含 `source_chunk_id`、`source_chunk_no`，这些是工作流内部诊断字段，不应显示给终端用户。
3. 报告原文引用在 `location.quote` 中，但没有同步到顶层 `quote`。
4. 当前有效 finding 没有 `location.page`，业务系统无法稳定跳转报告预览页。
5. 当前没有用户友好的位置文案字段，例如 `第1页，样品信息表`。
6. 当前 `summary = 本次审核发现4个问题`，但 `totalIssues = 3`、`findings.length = 3`、`valid_issue_count = 3`，摘要与有效问题数量不一致。

## 3. 需求目标

请工作流系统侧将“机器可读定位字段”和“用户可读位置字段”拆开返回：

| 字段 | 用途 | 是否面向用户展示 |
| --- | --- | --- |
| `location` | 机器可读定位对象，用于跳页、高亮、诊断 | 否 |
| `location_text` | 用户可读位置文案，用于检测结果卡片展示 | 是 |
| `quote` | 待审报告原文引用，用于“原文引用”区域展示 | 是 |
| `debug` 或 `diagnostics` | 工作流内部排查信息，例如 chunk id | 否 |

核心原则：

- `location` 可以是结构化对象，但不得要求业务系统把它直接展示给用户。
- `location_text` 必须是自然语言短文本。
- `quote` 必须是待审报告原文引用，建议放在 finding 顶层。
- `source_chunk_id`、`source_chunk_no` 等内部字段建议放入 `debug` 或仅保留在 `location` 中供系统使用，不能作为展示文案。

## 4. 推荐字段结构

### 4.1 有页码时

推荐每条 finding 返回：

```json
{
  "type": "数据异常",
  "title": "报告编号不一致",
  "content": "待审阅报告片段中出现的检验报告编号为'№：201854833'，与本报告主体编号'№：2025520398FB'不一致，存在引用错误或拼贴无关报告内容的风险。",
  "quote": "№：201854833",
  "location_text": "第48页，样品描述部分",
  "location": {
    "page": 48,
    "pageNo": 48,
    "page_no": 48,
    "section": "样品描述部分",
    "quote": "№：201854833"
  },
  "debug": {
    "source_chunk_id": 532,
    "source_chunk_no": 14
  },
  "severity": "high",
  "confidence": 0.95,
  "suggestion": "核实并修正报告编号，确保全文编号统一且与封面一致。"
}
```

说明：

- `location.page` 是系统跳页字段。
- `location_text` 是用户展示字段。
- `quote` 是报告原文引用字段。
- `debug.source_chunk_id`、`debug.source_chunk_no` 是排查字段，不参与用户展示。

### 4.2 暂时无法确认页码时

如果当前文件解析链路确实无法取得与预览 PDF 一致的页码，请仍然返回 `location_text`：

```json
{
  "type": "内容缺失",
  "title": "检验报告缺少主检、审核、批准人签字",
  "content": "报告结论页中“批准”、“审核”、“主检”栏目均为空白，未填写人员姓名或加盖电子签章，不符合检验报告有效性要求。",
  "quote": "批准：审核：主检：电子签",
  "location_text": "检验结论页签署栏",
  "location": {
    "section": "检验结论页签署栏",
    "quote": "批准：审核：主检：电子签"
  },
  "debug": {
    "source_chunk_id": 520,
    "source_chunk_no": 2
  },
  "severity": "high",
  "confidence": 0.95,
  "suggestion": "补充主检、审核、批准人签字或电子签章。"
}
```

同时可在顶层 `warnings` 中说明页码不可用：

```json
{
  "type": "page_location_unavailable",
  "message": "当前文件解析链路无法取得与预览 PDF 一致的页码，终态 finding 不返回 location.page。",
  "file_type": "docx",
  "page_location_source": "unsupported_word_pagination"
}
```

## 5. 字段详细要求

### 5.1 `location`

`location` 是机器可读字段，建议结构：

```json
{
  "page": 48,
  "pageNo": 48,
  "page_no": 48,
  "section": "页眉",
  "quote": "第 47 页 共 22 页"
}
```

字段要求：

| 字段 | 是否必填 | 说明 |
| --- | --- | --- |
| `page` | 强烈建议必填 | 报告预览页码，从 1 开始。 |
| `pageNo` | 可选 | 兼容字段，值与 `page` 相同。 |
| `page_no` | 可选 | 兼容字段，值与 `page` 相同。 |
| `section` | 建议必填 | 问题所在章节、栏目、表格或区域。 |
| `quote` | 建议保留 | 待审报告原文引用；同时应同步到顶层 `quote`。 |

注意：

- `location` 不应作为用户展示文案直接渲染。
- `source_chunk_id`、`source_chunk_no` 可以保留，但更推荐放入 `debug`，减少业务系统误展示风险。
- 如果保留在 `location` 中，仍必须同时返回 `location_text`。

### 5.2 `location_text`

`location_text` 是用户可读字段，建议必填。

格式建议：

```text
第{page}页，{section}
```

示例：

```text
第1页，样品信息表
第4页，检验结论页签署栏
第48页，样品描述部分
附页2，产品检验收费明细表
检验结论页签署栏
```

生成规则：

1. 有 `location.page` 和 `location.section` 时，返回 `第{page}页，{section}`。
2. 只有 `location.page` 时，返回 `第{page}页`。
3. 只有 `location.section` 时，返回 `{section}`。
4. 不要返回 JSON 字符串。
5. 不要包含 `source_chunk_id`、`source_chunk_no`、`kb_chunk_id` 等内部字段。
6. 文案应简短，建议不超过 50 个中文字符。

### 5.3 `quote`

`quote` 是用户可读的报告原文引用，建议顶层必填。

当前返回中 `quote` 位于 `location.quote`，建议调整为顶层同步返回：

```json
{
  "quote": "样品编号 20250409002 (2025520398-2)",
  "location": {
    "quote": "样品编号 20250409002 (2025520398-2)"
  }
}
```

要求：

1. `quote` 必须来自待审报告原文。
2. 不要使用知识库依据文本作为 finding 的顶层 `quote`。
3. 知识库依据引用应继续放在 `basis[*].quote`。
4. `quote` 和 `basis[*].quote` 语义必须区分清楚。

字段语义：

| 字段 | 含义 |
| --- | --- |
| `quote` | 待审报告中的问题原文 |
| `basis[*].quote` | 知识库、标准、依据文件中的依据原文 |
| `location.quote` | 可保留，作为定位对象中的原文锚点 |

### 5.4 `debug`

建议将内部排查字段移动到 `debug`：

```json
{
  "debug": {
    "source_chunk_id": 532,
    "source_chunk_no": 14,
    "page_location_source": "preview_pdf"
  }
}
```

这些字段用于联调排查，不作为用户展示内容。

## 6. 结果查询接口推荐格式

`GET /api/audit/tasks/{task_id}/result` 推荐返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "summary": "本次审核发现3个有效问题，建议人工复核并修订报告。",
    "totalIssues": 3,
    "findings": [
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
            "quote": "№：2025520398FB",
            "file_name": "2025520398FB（批注本安部分）(0725)_20260509095033A009.docx",
            "kb_chunk_id": "4"
          }
        ],
        "severity": "medium",
        "confidence": 0.95,
        "suggestion": "核对委托编号规则，统一报告页眉与正文中的委托编号后缀标识。"
      }
    ],
    "diagnostics": {
      "candidate_count": 4,
      "valid_issue_count": 3,
      "filtered_count": 1
    }
  }
}
```

## 7. 终态回调 result 推荐格式

如果工作流侧向业务系统发送终态回调，请在 `result` 中使用同样的 finding 字段：

```json
{
  "callback_event_id": "wf-audit-33-0-success",
  "biz_id": "AI-TASK-45",
  "workflow_task_id": "33",
  "workflow_task_no": "AUDIT-20260512-000005",
  "task_status": "SUCCESS",
  "status": "completed",
  "progress_percent": 100,
  "progress_text": "AI审核工作流执行完成",
  "result": {
    "success": true,
    "summary": "本次审核发现3个有效问题，建议人工复核并修订报告。",
    "totalIssues": 3,
    "findings": [
      {
        "type": "数据异常",
        "title": "报告编号不一致",
        "content": "待审阅报告片段中出现的检验报告编号为'№：201854833'，与本报告主体编号'№：2025520398FB'不一致。",
        "quote": "№：201854833",
        "location_text": "第48页，样品描述部分",
        "location": {
          "page": 48,
          "pageNo": 48,
          "page_no": 48,
          "section": "样品描述部分",
          "quote": "№：201854833"
        },
        "debug": {
          "source_chunk_id": 532,
          "source_chunk_no": 14
        },
        "severity": "high",
        "confidence": 0.95,
        "suggestion": "核实并修正报告编号，确保全文编号统一且与封面一致。"
      }
    ],
    "diagnostics": {
      "candidate_count": 4,
      "valid_issue_count": 3,
      "filtered_count": 1
    }
  }
}
```

## 8. 计数字段一致性要求

当前样本中存在：

```json
{
  "summary": "本次审核发现4个问题",
  "totalIssues": 3,
  "diagnostics": {
    "candidate_count": 4,
    "valid_issue_count": 3,
    "filtered_count": 1
  }
}
```

这会导致业务侧和用户理解不一致。请按以下规则生成：

| 字段 | 应代表的数量 |
| --- | --- |
| `findings.length` | 有效问题数量 |
| `totalIssues` | 有效问题数量，必须等于 `findings.length` |
| `diagnostics.valid_issue_count` | 有效问题数量，必须等于 `findings.length` |
| `diagnostics.candidate_count` | 候选问题数量 |
| `diagnostics.filtered_count` | 被过滤候选问题数量 |

关系必须满足：

```text
candidate_count = valid_issue_count + filtered_count
totalIssues = findings.length = valid_issue_count
```

`summary` 应基于有效问题数量生成：

```text
本次审核发现3个有效问题，另有1个候选问题因依据产品类型不匹配被过滤。
```

不要把候选问题数量写成最终发现问题数量。

## 9. 页码定位要求

业务系统希望通过 `location.page` 实现检测结果与报告预览联动。

请工作流侧尽量返回与业务系统预览一致的页码：

1. 页码从 `1` 开始。
2. 对 PDF 文件，返回 PDF 页码。
3. 对 doc/docx 文件，如果业务系统传入 `preview_pdf_url`，请优先基于该预览 PDF 解析并返回页码。
4. 如果无法获取可靠页码，不要猜测页码；可以不返回 `location.page`，但必须返回 `location_text`。
5. 如无法返回页码，请在 `warnings` 中明确说明原因。

当前样本中存在：

```json
{
  "type": "page_location_unavailable",
  "message": "当前文件解析链路无法取得与预览 PDF 一致的页码，终态 finding 不返回 location.page。",
  "file_type": "docx",
  "page_location_source": "unsupported_word_pagination"
}
```

请工作流侧确认：如果创建任务入参包含 `preview_pdf_url`，是否可以基于该 PDF 补齐 `location.page`。

## 10. 不推荐的返回方式

不要只返回下面这种字段，然后让业务系统自行决定如何展示：

```json
{
  "location": {
    "quote": "№：201854833",
    "section": "样品描述部分",
    "source_chunk_id": 532,
    "source_chunk_no": 14
  }
}
```

原因：

- 该对象不是用户展示文案。
- 缺少 `location.page` 时无法跳页。
- 缺少 `location_text` 时业务系统容易把 JSON 当文本展示。
- `source_chunk_id`、`source_chunk_no` 对用户没有意义。

## 11. 工作流侧处理清单

请工作流系统侧按以下清单调整：

1. 每条有效 finding 增加顶层 `location_text`。
2. 每条有效 finding 增加顶层 `quote`，从 `location.quote` 同步即可，但必须确保语义是待审报告原文。
3. `location` 保持结构化对象，用于系统定位，不作为展示文案。
4. 有可靠页码时，返回 `location.page`、`location.pageNo`、`location.page_no`。
5. 将 `source_chunk_id`、`source_chunk_no` 移入 `debug`，或至少不要依赖业务系统展示这些字段。
6. 保证 `summary`、`totalIssues`、`findings.length`、`diagnostics.valid_issue_count` 一致。
7. 保留 `diagnostics.filtered_findings` 用于排查，但不要把过滤问题计入 `totalIssues`。
8. 如果页码不可用，在 `warnings` 中说明原因，同时仍返回用户友好的 `location_text`。

## 12. 验收标准

下一次联调请按以下标准验收：

1. `data.findings[*].location_text` 非空，且不是 JSON。
2. `data.findings[*].quote` 非空，且来自待审报告原文。
3. `data.findings[*].location` 是结构化对象。
4. 有可靠页码时，`data.findings[*].location.page` 非空且从 1 开始。
5. 检测结果页面不再出现 `{"page":...}`、`source_chunk_id`、`source_chunk_no` 等 JSON 或内部字段。
6. `data.totalIssues = data.findings.length`。
7. `data.diagnostics.valid_issue_count = data.findings.length`。
8. `summary` 中的问题数量与有效问题数量一致。
9. 如果存在被过滤候选问题，`summary` 或 `diagnostics` 能明确区分“有效问题”和“候选过滤问题”。
10. 终态回调 `result.findings` 与结果查询接口 `data.findings` 字段语义一致。
