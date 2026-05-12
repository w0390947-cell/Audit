# “本安-矿用本安型手机3”测试任务检测结果过滤问题说明

日期：2026-05-12

本文档面向工作流系统侧，用于说明“本安-矿用本安型手机3”测试任务的执行结果和需要工作流侧排查的问题。

## 1. 结论概述

本次测试中，AI 审核工作流主流程已经正常完成：

- 输入校验成功；
- 报告文件解析成功；
- 文本切分成功；
- 知识库检索成功；
- AI审核阶段成功；
- 结果校验成功；
- 结果保存成功；
- 终态成功回调已被业务系统处理。

但是存在一个关键不一致：

- `ai_audit` 阶段输出显示：`AI审核完成，发现1个问题`，`finding_count = 1`。
- `result_validate` 阶段输出显示：`totalIssues = 0`。
- `result_save` 阶段输出显示：`issue_count = 0`。
- 业务系统最终 `audit_ai_finding` 表中保存问题数为 `0`。

因此，本次无法验收“检测结果点击跳转报告页”功能，因为最终没有任何检测结果可点击。

请工作流系统侧重点检查：为什么 AI审核阶段发现了 1 个候选问题，但结果校验后变成 0 个最终问题；该候选问题被过滤的具体原因是什么。

## 2. 任务基本信息

业务系统 AI 任务：

| 字段 | 值 |
| --- | --- |
| `ai_task_id` | `18` |
| `task_no` | `SF-1778577348336` |
| 产品名称 | `本安-矿用本安型手机3` |
| 报告文件 | `etst_20260512171546A001.docx` |
| 报告文件 URL | `/profile/upload/2026/05/12/etst_20260512171546A001.docx` |
| 提交时间 | `2026-05-12 17:15:48` |
| 当前任务状态 | `completed` |
| 当前复核状态 | `reviewing` |
| 当前进度 | `100` |
| 当前进度文案 | `AI分析完成，未发现关键问题` |
| 业务系统更新时间 | `2026-05-12 17:16:42` |

工作流任务：

| 字段 | 值 |
| --- | --- |
| `workflow_task_id` | `13` |
| `workflow_task_no` | `AUDIT-20260512-000007` |
| `workflow_code` | `policy_document_audit` |

## 3. 工作流阶段执行情况

业务系统 `audit_ai_flow_stage` 中该任务阶段如下：

| 阶段 | 状态 | 时间 | 关键输出 |
| --- | --- | --- | --- |
| `input_validate` 输入校验 | `completed` | `17:16:09 - 17:16:10` | `report_file_count = 1` |
| `file_parse` 文件解析 | `completed` | `17:16:09 - 17:16:10` | `page_count = 54`，`page_location_support = true` |
| `text_split` 文本切分 | `completed` | `17:16:10 - 17:16:11` | `chunk_count = 25` |
| `knowledge_retrieve` 知识库检索 | `completed` | `17:16:10 - 17:16:23` | `reference_count = 200` |
| `ai_audit` AI审核 | `completed` | `17:16:23 - 17:16:41` | `finding_count = 1` |
| `result_validate` 结果校验 | `completed` | `17:16:40 - 17:16:41` | `totalIssues = 0` |
| `result_save` 结果保存 | `completed` | `17:16:40 - 17:16:41` | `issue_count = 0` |
| `callback` 回调通知 | `completed` | `17:16:40 - 17:16:41` | 终态回调已提交 |

## 4. 页码定位链路观察

本次文件解析阶段已经具备页码定位能力：

```json
{
  "file_type": "pdf",
  "page_count": 54,
  "page_location_source": "preview_pdf",
  "page_location_support": true,
  "parse_status": "SUCCESS"
}
```

这说明工作流侧已经基于预览 PDF 建立页码来源，理论上如果最终 `result.findings` 中存在问题，应可以输出：

```json
{
  "location": {
    "page": 6,
    "pageNo": 6,
    "page_no": 6
  },
  "page": 6,
  "pageNo": 6,
  "page_no": 6
}
```

但是由于最终保存问题数为 0，本次无法验证业务系统 `audit_ai_finding.page_no` 是否能正确落库，也无法验证前端点击检测结果跳转 PDF 页码。

## 5. AI审核阶段关键输出

`ai_audit` 阶段输出摘要：

```json
{
  "audit_mode": "business_report_findings",
  "audit_status": "SUCCESS",
  "finding_count": 1,
  "audit_strategy": "chunk_then_merge",
  "reference_count": 200,
  "model_call_count": 25,
  "chunk_failed_count": 0,
  "chunk_result_count": 25,
  "chunk_success_count": 25,
  "references_used_in_prompt": 100,
  "basis_chunks_used_in_prompt": 100,
  "retrieval_used_summary": {
    "reference_count": 200,
    "retrieval_count": 25,
    "source_chunk_count": 25,
    "reference_truncated": true,
    "covered_source_chunk_count": 25,
    "uncovered_source_chunk_ids": [],
    "reference_selection_strategy": "chunk_then_merge"
  },
  "model_usage_summary": {
    "model": "qwen3.5-plus",
    "duration_ms": 49840,
    "parallelism": 3,
    "input_tokens": 125818,
    "output_tokens": 858,
    "model_call_count": 25,
    "chunk_success_count": 25,
    "chunk_failed_count": 0,
    "timeout_retries": 0,
    "ai_audit_timeout_seconds": 840
  }
}
```

观察：

- AI审核实际完成，并非超时。
- 模型调用 25 次，全部成功。
- 产生了 1 个候选问题。
- 依据命中 200 条，实际进入 prompt 的依据 100 条。
- `reference_truncated = true`，说明已有依据裁剪策略。

## 6. 结果校验与保存情况

`result_validate` 阶段输出：

```json
{
  "totalIssues": 0,
  "output_format": "business_report_findings",
  "validate_status": "SUCCESS"
}
```

`result_save` 阶段输出：

```json
{
  "result_id": 11,
  "issue_count": 0
}
```

业务系统最终保存：

```sql
select count(*) from audit_ai_finding where ai_task_id = 18;
-- 0
```

请工作流侧确认：

1. `ai_audit` 生成的 1 个候选问题是什么？
2. 该候选问题是否进入了 `result_validate`？
3. 如果被 `result_validate` 过滤，过滤原因是什么？
4. 为什么 `result_validate.output` 中没有返回过滤详情？
5. 为什么终态 `result.summary` 只体现“未发现关键问题”，但没有说明“候选问题已过滤”？

## 7. 业务系统收到的回调事件

业务系统已收到并成功处理完整回调链：

| `callback_event_id` | 状态 |
| --- | --- |
| `wf-audit-13-0-stage-input_validate-running` | `processed` |
| `wf-audit-13-0-stage-input_validate-success` | `processed` |
| `wf-audit-13-0-stage-file_parse-running` | `processed` |
| `wf-audit-13-0-stage-file_parse-success` | `processed` |
| `wf-audit-13-0-stage-text_split-running` | `processed` |
| `wf-audit-13-0-stage-text_split-success` | `processed` |
| `wf-audit-13-0-stage-knowledge_retrieve-running` | `processed` |
| `wf-audit-13-0-stage-knowledge_retrieve-success` | `processed` |
| `wf-audit-13-0-stage-ai_audit-running` | `processed` |
| `wf-audit-13-0-stage-ai_audit-success` | `processed` |
| `wf-audit-13-0-stage-result_validate-running` | `processed` |
| `wf-audit-13-0-stage-result_validate-success` | `processed` |
| `wf-audit-13-0-stage-result_save-running` | `processed` |
| `wf-audit-13-0-stage-result_save-success` | `processed` |
| `wf-audit-13-0-stage-callback-running` | `processed` |
| `wf-audit-13-0-success` | `processed` |

因此，本次问题不是业务系统未收到回调，也不是回调处理失败。

## 8. 需要工作流系统侧处理的问题

请工作流系统侧排查并反馈以下问题：

1. `workflow_task_id = 13` / `AUDIT-20260512-000007` 的 AI审核阶段生成的原始候选 finding 内容。
2. `result_validate` 将该 finding 过滤掉的具体规则和原因。
3. 是否可以在 `result_validate.output` 中增加过滤统计和过滤明细，例如：

```json
{
  "totalIssues": 0,
  "candidate_count": 1,
  "valid_issue_count": 0,
  "filtered_count": 1,
  "filtered_findings": [
    {
      "finding_index": 1,
      "title": "候选问题标题",
      "reason": "finding_basis_product_mismatch",
      "message": "依据产品类型或保护型式与当前产品不匹配"
    }
  ]
}
```

4. 是否可以在终态 `result` 中返回诊断信息，例如：

```json
{
  "summary": "未发现关键问题；AI审核生成1个候选问题，经结果校验过滤1个。",
  "findings": [],
  "diagnostics": {
    "candidate_count": 1,
    "filtered_count": 1,
    "filter_reasons": [
      {
        "reason": "finding_basis_product_mismatch",
        "count": 1
      }
    ]
  }
}
```

5. 如果过滤后的最终结果为 0 条问题，请确认是否仍应在 `ai_audit.stage_summary` 中写“发现1个问题”。建议改为更准确的表述：

```text
AI审核完成，生成1个候选问题
```

或者：

```text
AI审核完成，候选问题1个，最终有效问题0个
```

6. 如果该候选问题其实应当保留，请修正 `result_validate` 规则，并确保终态 `result.findings[*]` 返回 `location.page` 等页码字段。

## 9. 对“检测结果点击跳转报告页”验收的影响

本次测试无法验证点击跳转页码功能，原因是业务系统最终没有保存任何 `audit_ai_finding`。

下一次联调建议准备一个明确会保留至少 1 条有效问题的测试样例，并确认终态回调中至少包含：

```json
{
  "findings": [
    {
      "title": "问题标题",
      "content": "问题描述",
      "location": {
        "page": 6,
        "pageNo": 6,
        "page_no": 6
      },
      "page": 6,
      "pageNo": 6,
      "page_no": 6
    }
  ]
}
```

业务系统侧随后可以检查：

```sql
select finding_id, sort_num, finding_title, page_no, location_json
from audit_ai_finding
where ai_task_id = 18
order by sort_num, finding_id;
```

预期：

- 至少 1 条记录；
- `page_no` 不为 `NULL`；
- `location_json` 保留页码和定位信息；
- 前端检测结果点击后能跳转报告 PDF 对应页。

