# AI任务详情页码跳转与检测结果质量修复说明（交付业务系统侧）

日期：2026-05-12

## 1. 背景

业务系统侧联调任务：

| 字段 | 值 |
| --- | --- |
| 产品名称 | 本安-矿用本安型手机2 |
| 任务编号 | SF-1778571597055 |
| AI任务ID | 17 |
| 工作流任务ID | 12 |
| 报告文件 | etst_20260512153955A001.docx |
| 报告文件地址 | /profile/upload/2026/05/12/etst_20260512153955A001.docx |

业务侧反馈的问题主要有两类：

1. 终态回调 `result.findings[*]` 没有返回 `location.page`，导致 `audit_ai_finding.page_no` 全部为 `NULL`，前端点击检测结果无法跳转 PDF 页。
2. 部分检测结果质量不稳定，存在依据与产品类型不匹配、误用控制箱依据、对本安型手机误判缺少隔爆型标志、问题描述不完整等情况。

工作流侧已完成针对性修复。本文档说明业务系统侧下一次联调需要传入的字段、回调结果变化和验收方式。

## 2. 工作流侧已修复内容

### 2.1 页码定位

工作流侧已经支持在 finding 中输出页码字段：

```json
{
  "page": 6,
  "pageNo": 6,
  "page_no": 6,
  "location": {
    "page": 6,
    "pageNo": 6,
    "page_no": 6,
    "section": "技术参数",
    "quote": "报告原文短引用",
    "source_chunk_id": 155,
    "source_chunk_no": 1
  }
}
```

页码来源优先级：

1. 模型返回的有效页码。
2. 工作流分片元数据 `audit_task_content_chunk.page_no`。
3. 如果分片没有可靠页码，则不猜测页码，不返回 `location.page`。

对于 PDF 文件，工作流解析时会按 PDF 页解析文本并生成 `page_no`。

对于 doc/docx 文件，工作流不能直接从 Word 文本解析结果中得到与业务侧 PDF 预览完全一致的页码。因此，doc/docx 要实现点击跳页，需要业务系统在创建工作流任务时同时传入最终预览 PDF 地址。

### 2.2 doc/docx 预览 PDF 解析

工作流侧已支持以下字段作为预览 PDF 地址：

| 字段位置 | 字段名 |
| --- | --- |
| `input` 顶层 | `preview_pdf_url` |
| `input` 顶层 | `previewPdfUrl` |
| `input` 顶层 | `pdf_url` |
| `input` 顶层 | `pdfUrl` |
| `input.metadata` | `preview_pdf_url` |
| `input.metadata` | `previewPdfUrl` |
| `input.metadata` | `pdf_url` |
| `input.metadata` | `pdfUrl` |

当 `file_type=doc/docx` 且存在上述任一 PDF 地址时：

1. 工作流会下载预览 PDF。
2. 工作流会以该 PDF 作为实际解析文件。
3. `audit_task_content_chunk.page_no` 会按预览 PDF 页码生成。
4. 终态 finding 会补齐 `location.page` 和顶层兼容页码字段。
5. 原始 Word 文件信息会保留在任务输入元数据中。

当 `file_type=doc/docx` 但没有传预览 PDF 地址时：

1. 工作流仍可审核文本内容。
2. 工作流不会猜测页码。
3. 终态 finding 不返回 `location.page`。
4. 终态 `result.warnings` 会包含 `page_location_unavailable` 诊断。

## 3. 业务系统侧需要调整的创建任务入参

本次联调的文件是 docx，业务侧预览也是将 docx 转换成 PDF 后展示。因此下一次联调请在创建工作流任务时传入“最终预览 PDF”的可下载 URL。

推荐使用 `input.preview_pdf_url`：

```json
{
  "workflow_code": "policy_document_audit",
  "biz_id": "SF-1778571597055",
  "callback_url": "http://业务系统地址/api/audit/callback",
  "input": {
    "file_id": "SF-1778571597055",
    "file_url": "http://业务系统地址/profile/upload/2026/05/12/etst_20260512153955A001.docx",
    "file_name": "etst_20260512153955A001.docx",
    "file_type": "docx",
    "preview_pdf_url": "http://业务系统地址/profile/preview/2026/05/12/etst_20260512153955A001.pdf",
    "metadata": {
      "business_type": "audit_review",
      "product_name": "本安-矿用本安型手机2"
    },
    "knowledge_scope": {
      "knowledge_base_codes": ["default"],
      "effective_only": true
    },
    "caller_context": {
      "permission_mode": "explicit_scope"
    }
  }
}
```

也可以放在 `metadata.preview_pdf_url`：

```json
{
  "input": {
    "file_url": "http://业务系统地址/profile/upload/2026/05/12/etst_20260512153955A001.docx",
    "file_name": "etst_20260512153955A001.docx",
    "file_type": "docx",
    "metadata": {
      "product_name": "本安-矿用本安型手机2",
      "preview_pdf_url": "http://业务系统地址/profile/preview/2026/05/12/etst_20260512153955A001.pdf"
    }
  }
}
```

注意：

1. `preview_pdf_url` 必须是工作流服务可访问的 `http/https` 地址。
2. 该 PDF 必须与业务系统前端最终预览使用的是同一份文件。
3. 如果业务系统每次预览都会重新转换 PDF，请确保传给工作流的是本次任务实际展示的 PDF，避免分页不一致。
4. 如果只有相对地址，请业务侧在创建任务前拼成工作流服务可访问的绝对 URL。

## 4. 终态回调结果变化

### 4.1 有可靠页码时

业务侧可继续按已有兼容顺序读取页码：

1. `location.page`
2. `location.pageNo`
3. `location.page_no`
4. `page`
5. `pageNo`
6. `page_no`

成功示例：

```json
{
  "result": {
    "success": true,
    "summary": "本次审核发现3个问题",
    "totalIssues": 3,
    "findings": [
      {
        "type": "标准不符",
        "title": "问题标题",
        "content": "完整的问题描述。",
        "severity": "medium",
        "location": {
          "page": 6,
          "pageNo": 6,
          "page_no": 6,
          "section": "问题所在章节",
          "quote": "报告原文短引用",
          "source_chunk_id": 155,
          "source_chunk_no": 1
        },
        "page": 6,
        "pageNo": 6,
        "page_no": 6,
        "basis": {
          "file_name": "依据文件名.pdf",
          "quote": "依据原文短引用",
          "kb_chunk_id": "KB-001"
        },
        "confidence": 0.82,
        "suggestion": "修改建议。"
      }
    ]
  }
}
```

### 4.2 无可靠页码时

如果业务侧没有传 doc/docx 的预览 PDF 地址，工作流不会返回页码，并会返回诊断信息：

```json
{
  "result": {
    "success": true,
    "summary": "本次审核发现2个问题",
    "totalIssues": 2,
    "findings": [
      {
        "title": "问题标题",
        "location": {
          "section": "片段1",
          "quote": "报告原文短引用",
          "source_chunk_id": 155,
          "source_chunk_no": 1
        }
      }
    ],
    "warnings": [
      {
        "type": "page_location_unavailable",
        "message": "当前文件解析链路无法取得与预览 PDF 一致的页码，终态 finding 不返回 location.page。",
        "page_location_source": "unsupported_word_pagination",
        "file_type": "docx"
      }
    ]
  }
}
```

业务侧前端可保持现有“暂无定位信息”提示逻辑。

## 5. 检测结果质量控制变化

工作流侧在 `result_validate` 阶段新增了过滤规则：

1. `title/content/suggestion/location` 仍必须存在。
2. `location.quote` 必须存在，且表示报告原文短引用。
3. finding 必须有依据证据 `basis`，或可由当前分片检索结果反推依据。
4. `content` 不能是明显未完成句子，例如以“标注为”“描述为”“为”“如下：”结尾。
5. 产品为“手机”时，控制箱、KXJ127、PLC 控制箱类依据会被识别为不匹配。
6. 产品为“本安”且非“隔爆”时，不再仅因缺少隔爆 `db` 标志判定不合格。

被过滤的 finding 不进入最终 `result.findings`，但会写入：

```json
{
  "result": {
    "validation_warnings": [
      {
        "type": "finding_filtered",
        "reason": "finding_basis_product_mismatch",
        "finding_index": 3,
        "title": "外壳材质与标准要求不一致",
        "source_chunk_id": 168
      }
    ]
  }
}
```

常见 `reason`：

| reason | 含义 |
| --- | --- |
| `finding_content_incomplete` | 问题描述不完整 |
| `finding_quote_missing` | 缺少报告原文引用 |
| `finding_basis_missing` | 缺少依据证据 |
| `finding_basis_product_mismatch` | 依据产品类型或保护型式与当前产品不匹配 |

## 6. 下一次联调建议

建议业务侧使用新任务重新跑，不建议复用已经完成回调的旧任务数据。

重点检查：

1. 创建任务时传入 `preview_pdf_url`。
2. 工作流 `file_parse` 阶段输出包含：

```json
{
  "file_type": "pdf",
  "page_count": 10,
  "page_location_source": "preview_pdf",
  "page_location_support": true
}
```

3. 工作流终态回调 `result.findings[*].location.page` 至少主要问题不为空。
4. `result.findings[*].location.quote` 有报告原文短引用。
5. 不再出现“本安-矿用本安型手机2”明显误用控制箱/KXJ127 依据或隔爆 `db` 误判的问题。
6. 如果有 `validation_warnings`，业务侧可暂不展示给普通用户，但建议记录到调试日志。

## 7. 业务库验收 SQL

重新跑任务后，业务侧可检查：

```sql
select finding_id, sort_num, finding_title, page_no, location_json
from audit_ai_finding
where ai_task_id = 新AI任务ID
order by sort_num, finding_id;
```

预期：

1. 至少主要问题的 `page_no` 不为 `NULL`。
2. `location_json` 中包含 `page`、`pageNo`、`page_no` 中的至少一个，推荐包含 `page`。
3. 前端检测结果卡片显示“第 N 页”。
4. 点击带页码的问题后，报告预览跳转到对应 PDF 页。
5. 无页码的问题仍提示“暂无定位信息”，不发生错误跳转。

## 8. 本次旧任务说明

旧任务：

| 字段 | 值 |
| --- | --- |
| AI任务ID | 17 |
| 工作流任务ID | 12 |

该任务已经完成终态回调，且原始回调中没有页码字段。工作流侧修复不会自动修改旧回调 payload，也不会自动补写业务库 `audit_ai_finding.page_no`。

请业务侧使用同一报告文件重新创建新任务，并按本文档传入预览 PDF 地址后再验收页码跳转。

## 9. 工作流侧验证

工作流侧已执行编译验证：

```bash
mvn clean -DskipTests compile
```

结果：编译成功。
