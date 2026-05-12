# “本安-矿用本安型手机”任务检测结果为空问题说明

日期：2026-05-12

本文档面向工作流系统侧，用于说明业务系统中任务 `SF-1778588450549` 的 AI 审核执行结果，以及需要工作流侧排查和处理的问题。

## 1. 结论概述

本次任务的工作流已经完整执行成功，业务系统也已收到并处理终态回调。但是最终“检测结果”为空，原因不是业务系统前端未渲染，也不是业务系统未收到回调，而是工作流终态结果中没有返回任何可保存的问题。

关键不一致如下：

- `ai_audit` 阶段显示：生成 `60` 个候选问题。
- `result_validate` 阶段显示：候选问题 `60` 个，最终有效问题 `0` 个，过滤 `60` 个。
- `result_save` 阶段显示：保存 `0` 条问题记录。
- 终态回调 `result.findings` 长度为 `0`，`result.totalIssues = 0`。
- 业务系统 `audit_ai_finding` 表中该任务保存问题数为 `0`。

因此，AI 任务详情页“流转状态”里的 `60` 是工作流 AI 审核阶段产出的候选问题数量，不是最终可展示的检测结果数量。当前“检测结果”区域为空，符合业务系统收到的终态结果。

请工作流系统侧重点处理：为什么 60 个候选问题全部缺少可保存所需信息，尤其是报告原文 `quote`，导致 `result_validate` 全部过滤。

## 2. 任务基本信息

业务系统 AI 任务：

| 字段 | 值 |
| --- | --- |
| `ai_task_id` | `44` |
| `review_task_id` | `42` |
| `review_version_id` | `68` |
| `task_no` | `SF-1778588450549` |
| 产品名称 | `本安-矿用本安型手机` |
| 送检单位 | `山西矿安智能设备有限公司` |
| 报告文件 | `2025520398FB（批注本安部分）(0725)_20260512202048A001.docx` |
| 报告文件 URL | `/profile/upload/2026/05/12/2025520398FB（批注本安部分）(0725)_20260512202048A001.docx` |
| 版本号 | `v1.0` |
| 提交时间 | `2026-05-12 20:20:51` |
| 当前任务状态 | `completed` |
| 当前复核状态 | `reviewing` |
| 当前进度 | `100` |
| 当前进度文案 | `AI分析完成，未发现关键问题` |

工作流任务：

| 字段 | 值 |
| --- | --- |
| `workflow_task_id` | `29` |
| `workflow_task_no` | `AUDIT-20260512-000001` |
| `workflow_code` | `policy_document_audit` |
| `biz_id` | `AI-TASK-44` |

## 3. 阶段执行证据

业务系统 `audit_ai_flow_stage` 中该任务关键阶段如下：

| 阶段 | 状态 | 关键输出 |
| --- | --- | --- |
| `input_validate` 输入校验 | `completed` | `report_file_count = 1` |
| `file_parse` 文件解析 | `completed` | `page_count = 52`，`page_location_support = true` |
| `text_split` 文本切分 | `completed` | `chunk_count = 25` |
| `knowledge_retrieve` 知识库检索 | `completed` | `reference_count = 100`，`retrieval_count = 25` |
| `ai_audit` AI审核 | `completed` | `finding_count = 60`，`candidate_count = 60` |
| `result_validate` 结果校验 | `completed` | `candidate_count = 60`，`valid_issue_count = 0`，`filtered_count = 60` |
| `result_save` 结果保存 | `completed` | `issue_count = 0` |
| `callback` 回调通知 | `completed` | 终态回调已提交 |

`ai_audit` 阶段摘要：

```json
{
  "audit_mode": "business_report_findings",
  "audit_status": "SUCCESS",
  "finding_count": 60,
  "candidate_count": 60,
  "audit_strategy": "chunk_then_merge",
  "reference_count": 100,
  "model_call_count": 25,
  "chunk_result_count": 25,
  "chunk_success_count": 25,
  "chunk_failed_count": 0
}
```

`result_validate` 阶段摘要：

```json
{
  "validate_status": "SUCCESS",
  "output_format": "business_report_findings",
  "candidate_count": 60,
  "valid_issue_count": 0,
  "totalIssues": 0,
  "filtered_count": 60,
  "filter_reasons": [
    {
      "count": 59,
      "reason": "finding_quote_missing",
      "message": "候选问题缺少报告原文 quote，无法定位问题原文"
    },
    {
      "count": 1,
      "reason": "finding_content_incomplete",
      "message": "候选问题描述不完整，无法形成可保存的有效问题"
    }
  ]
}
```

`result_save` 阶段摘要：

```json
{
  "result_id": 24,
  "issue_count": 0
}
```

## 4. 终态回调证据

业务系统收到的终态回调事件：

| 字段 | 值 |
| --- | --- |
| `callback_event_id` | `wf-audit-29-0-success` |
| `event_status` | `processed` |
| `summary` | `未发现关键问题；AI审核生成60个候选问题，经结果校验过滤60个。` |
| `result.findings` 长度 | `0` |
| `result.issues` 长度 | `NULL` |
| `result.totalIssues` | `0` |

业务系统保存检测结果的逻辑只读取终态 `result.findings`，如为空则退回读取 `result.issues`。本次两者均无有效问题，所以不会写入 `audit_ai_finding`。

业务系统最终数据库状态：

```sql
select count(*)
from audit_ai_finding
where ai_task_id = 44;

-- 0
```

## 5. 被过滤问题样例

以下样例来自 `result_validate.output.filtered_findings`。

样例 1：

```json
{
  "type": "finding_filtered",
  "title": "生产单位与申请单位名称填写错误",
  "reason": "finding_quote_missing",
  "message": "候选问题缺少报告原文 quote，无法定位问题原文",
  "content": "报告封面及基本信息栏中，'生产单位'和'申请单位'均填写为'煤科（北京）检测技术有限公司'。根据常规逻辑及知识库中其他文档（如 KXJ127 说明书）显示，检测公司通常为检验机构，而非产品的生产或申请单位，此处极可能将'检验单位'误填为'生产/申请单位'，导致主体身份混淆。",
  "location": {
    "page": 1,
    "pageNo": 1,
    "page_no": 1
  },
  "finding_index": 1
}
```

样例 2：

```json
{
  "type": "finding_filtered",
  "title": "签发日期未填写",
  "reason": "finding_quote_missing",
  "message": "候选问题缺少报告原文 quote，无法定位问题原文",
  "content": "报告末尾'签发日期'字段显示为'年 月 日'，具体日期内容为空，不符合报告完整性要求。",
  "location": {
    "page": 1,
    "pageNo": 1,
    "page_no": 1
  },
  "finding_index": 2
}
```

样例 60：

```json
{
  "type": "finding_filtered",
  "title": "报告缺少必要签字盖章信息",
  "reason": "finding_quote_missing",
  "message": "候选问题缺少报告原文 quote，无法定位问题原文",
  "content": "根据审核依据中'注意事项'第 3 条规定：'报告无主检、审核、批准人签字（章）无效'。待审阅报告片段中未显示主检、审核、批准人的签字或印章，不符合报告有效性要求。",
  "location": {
    "page": 52,
    "pageNo": 52,
    "page_no": 52
  },
  "finding_index": 60
}
```

观察：

- 候选问题已经有 `title`、`content`、`location.page/pageNo/page_no`。
- 但绝大多数没有报告原文 `quote` 字段，被工作流侧校验规则 `finding_quote_missing` 拦截。
- 这说明页码定位链路本身已经具备基础数据，但问题原文锚点缺失，无法满足工作流当前保存规则。

## 6. 需要工作流系统侧处理的问题

请工作流系统侧排查并处理以下问题：

1. 明确 `result_validate` 对 `quote` 的字段契约。
   - 请说明候选问题必须返回的字段名是 `quote`，还是支持 `evidence_quote`、`source_quote`、`original_text` 等别名。
   - 如果只接受 `quote`，请在 AI 审核提示词、结构化输出 schema、后处理映射中强制生成该字段。

2. 修复 `ai_audit` 候选问题缺少 `quote` 的问题。
   - 当前 60 个候选问题中有 59 个因 `finding_quote_missing` 被过滤。
   - 建议 `quote` 必须来自待审报告原文，而不是知识库依据文本。
   - `quote` 应尽量是能在报告页内定位的短原文片段。

3. 修复 1 个 `finding_content_incomplete` 的问题。
   - 请定位该候选问题的原始输出。
   - 确认缺少的是 `title`、`content`、`location`、`quote` 中的哪些字段。
   - 如果模型输出不完整，请在合并或校验前补齐，或在阶段输出中暴露更具体的失败原因。

4. 终态结果需要返回最终有效问题，而不是只在阶段日志中保留候选问题。
   - 业务系统最终只保存 `result.findings` 或 `result.issues`。
   - 如果希望“检测结果”展示问题，终态回调必须包含可保存的 `result.findings[*]`。

5. 建议在终态 `result.diagnostics` 中保留过滤诊断信息。
   - 当前终态只有摘要能看出“过滤60个”，但没有结构化诊断字段。
   - 建议返回如下结构，便于业务系统后续展示或排查：

```json
{
  "summary": "未发现关键问题；AI审核生成60个候选问题，经结果校验过滤60个。",
  "findings": [],
  "totalIssues": 0,
  "diagnostics": {
    "candidate_count": 60,
    "valid_issue_count": 0,
    "filtered_count": 60,
    "filter_reasons": [
      {
        "reason": "finding_quote_missing",
        "count": 59,
        "message": "候选问题缺少报告原文 quote，无法定位问题原文"
      },
      {
        "reason": "finding_content_incomplete",
        "count": 1,
        "message": "候选问题描述不完整，无法形成可保存的有效问题"
      }
    ]
  }
}
```

6. 建议调整阶段文案，避免业务用户误解。
   - 当前 `ai_audit` 阶段文案是“生成60个候选问题”，这是准确的。
   - 但业务界面聚合显示“审核分析完成，发现60个问题”，用户容易理解为最终检测结果应有 60 条。
   - 工作流侧如能在 `result_validate` 或终态摘要中持续明确“最终有效问题0个”，可以降低误解。

## 7. 业务系统侧判断

业务系统侧目前表现符合收到的数据：

- `AI任务详情` 接口读取 `audit_ai_finding` 作为“检测结果”数据源。
- 本次 `audit_ai_finding` 对 `ai_task_id = 44` 的记录数是 `0`。
- 终态回调 `result.findings` 长度也是 `0`。
- 所以“检测结果”为空是终态结果决定的，不是前端列表渲染失败。

业务系统侧已补充原文引用展示能力：

- 终态 `result.findings[*].quote` 会落库到 `audit_ai_finding.quote_text`。
- 同时兼容 `evidence_quote`、`source_quote`、`original_text`、`location.quote`。
- AI任务详情页“检测结果”卡片会在问题描述下方单独展示“原文引用”。

业务系统侧可以考虑后续优化界面文案，把“审核分析”阶段的聚合文案从“发现 N 个问题”改成“生成 N 个候选问题，最终有效 M 个”，但这不影响本次根因判断。

## 8. 建议验收标准

请工作流侧修复后，用同类报告重新触发任务，并满足以下条件：

1. `ai_audit.output.candidate_count > 0`。
2. `result_validate.output.valid_issue_count > 0`。
3. `result_validate.output.filtered_count` 可以存在，但不应把所有有效问题过滤掉。
4. 终态回调中：

```json
{
  "result": {
    "totalIssues": 1,
    "findings": [
      {
        "title": "问题标题",
        "content": "问题描述",
        "quote": "报告中的原文片段",
        "location": {
          "page": 1,
          "pageNo": 1,
          "page_no": 1
        },
        "suggestion": "修改建议"
      }
    ]
  }
}
```

5. 业务系统数据库中能够查到保存结果：

```sql
select finding_id, sort_num, finding_title, quote_text, page_no, location_json
from audit_ai_finding
where ai_task_id = 44
order by sort_num, finding_id;
```

预期：

- 至少 1 条记录；
- `quote_text` 不为空；
- `page_no` 不为 `NULL`；
- `location_json` 保留页码和定位信息；
- AI任务详情页“检测结果”区域显示对应问题；
- AI任务详情页“检测结果”卡片展示“原文引用”；
- 点击检测结果时可以跳转到报告预览 PDF 的对应页。
