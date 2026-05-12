# AI任务详情页码跳转与检测结果质量联调问题报告

日期：2026-05-12

## 1. 测试任务信息

业务系统测试任务：

| 字段 | 值 |
| --- | --- |
| 产品名称 | 本安-矿用本安型手机2 |
| 任务编号 | SF-1778571597055 |
| AI任务ID | 17 |
| 工作流任务ID | 12 |
| 终态回调事件ID | wf-audit-12-0-success |
| 任务状态 | completed |
| 报告文件 | etst_20260512153955A001.docx |
| 报告文件地址 | /profile/upload/2026/05/12/etst_20260512153955A001.docx |

本次测试目标是验证 `tododocs/audit-ai-detail-finding-preview-jump-requirements.md` 中定义的第一阶段能力：

1. 工作流返回的每条问题携带页码。
2. 业务系统保存页码到 `audit_ai_finding.page_no`。
3. 用户点击 AI 任务详情的检测结果后，报告预览跳转到对应 PDF 页。

## 2. 当前测试现象

1. AI 任务详情中“检测结果”返回了 23 条问题，但内容质量不理想，存在明显产品/依据不匹配。
2. 点击检测结果无法跳转报告页。
3. 业务系统数据库中 23 条 `audit_ai_finding.page_no` 全部为 `NULL`。
4. `audit_ai_finding.location_json` 中只有 `source_chunk_id`、`source_chunk_no`、`section`、`quote` 等字段，没有 `page`、`pageNo`、`page_no`。

## 3. 业务侧排查结论

### 3.1 点击不能跳转的直接原因

业务系统前端读取页码的字段包括：

- `location.page`
- `location.pageNo`
- `location.page_no`
- `page`
- `pageNo`
- `page_no`

后端也已将上述字段解析并保存到：

- `audit_ai_finding.page_no`
- `audit_ai_finding.location_json`

但本次工作流终态回调中没有返回任何页码字段，导致业务系统无法保存页码。

数据库落库结果示例：

```text
finding_id: 228
sort_num: 1
finding_type: 标准不符
finding_title: 防爆标志与产品保护型式不一致
page_no: NULL
location_json: {"source_chunk_id":155,"source_chunk_no":1}
```

终态回调原始 payload 首条 finding 示例：

```json
{
  "type": "标准不符",
  "title": "防爆标志与产品保护型式不一致",
  "content": "报告片段中产品名称为“矿用本安型手机”，防爆标志标注为",
  "location": {
    "source_chunk_id": 155,
    "source_chunk_no": 1
  },
  "severity": "medium",
  "suggestion": "请依据知识库审核依据修正相关内容。",
  "source_chunk_id": 155,
  "source_chunk_no": 1
}
```

统计结果：

```text
result.findings 数量：23
result.findings[*].location.page 数量：0
result.findings[*].page 数量：0
```

结论：本次点击不跳转不是业务侧解析丢字段，而是工作流终态结果没有返回页码。

### 3.2 业务侧无法自行从 source_chunk_id 补页码

业务系统当前只收到并保存了回调 payload 和最终 finding。

本地业务库中没有 `audit_task_content_chunk` 或等价的分片页码表，无法根据：

```json
{
  "source_chunk_id": 155,
  "source_chunk_no": 1
}
```

反查该问题对应的报告页码。

因此，工作流侧需要在终态结果中直接返回 `location.page`。

## 4. 工作流侧需要重点排查的问题

### 4.1 页码字段未按契约返回

已约定终态回调 `result.findings` 或 `result.issues` 每条问题应返回：

```json
{
  "location": {
    "page": 1
  }
}
```

兼容字段也可以同步返回：

```json
{
  "page": 1,
  "pageNo": 1,
  "page_no": 1,
  "location": {
    "page": 1,
    "pageNo": 1,
    "page_no": 1
  }
}
```

但本次实际返回只有：

```json
{
  "location": {
    "source_chunk_id": 155,
    "source_chunk_no": 1
  }
}
```

请工作流侧确认：

1. `source_chunk_id/source_chunk_no` 是否能映射到分片表中的 `page_no`。
2. `docx` 文件解析链路是否生成了可靠页码。
3. 如果使用 PDF 预览作为页码基准，工作流是否基于同一份转换后的 PDF 做解析或分页。
4. 如果没有可靠页码，是否应明确不返回页码，并在质量报告中标记“无法页码定位”，而不是让业务侧误以为可跳转。

### 4.2 本次任务是 docx，页码基准需要统一

本次报告文件为：

```text
etst_20260512153955A001.docx
```

业务系统预览侧会将 `doc/docx` 转换为 PDF 后预览。页码跳转必须以最终预览 PDF 的页码为准。

请工作流侧确认采用以下其中一种方案：

1. 工作流使用业务侧同一份预览 PDF 进行文本解析和分片，返回 PDF 页码。
2. 工作流内部先将 docx 转换为 PDF，再基于 PDF 页码解析和分片，并保证与业务侧预览分页一致。
3. 如果暂时无法保证 docx 页码稳定，则对 docx 不返回 `location.page`，并明确告知该类文件暂不支持页码跳转。

第一阶段联动需求更推荐方案 1 或方案 2。

## 5. 检测结果内容质量问题

本次返回 23 条问题，其中多条疑似与测试产品不匹配。

测试产品是：

```text
本安-矿用本安型手机2
```

但回调结果中出现了明显偏离产品类型的内容，例如：

### 5.1 引用了控制箱企业标准 KXJ127

原始 finding 示例：

```json
{
  "title": "外壳材质与标准要求不一致",
  "content": "报告片段中描述样品外壳材质为\"ABS 塑料\"，而依据企业标准（KXJ127）第 4.4.5 条规定，该类产品外壳材质应为\"Q235B\"。",
  "location": {
    "quote": "外壳\\tABS 塑料",
    "section": "主要零 (元) 受控部件及重要原材料明细表",
    "source_chunk_id": 168,
    "source_chunk_no": 14
  }
}
```

问题：

- `KXJ127` 看起来是控制箱类产品依据，不应直接用于“矿用本安型手机2”。
- 手机类产品外壳为塑料并不必然等同于控制箱企业标准中的 `Q235B` 要求。
- 该问题很可能是知识库检索范围或依据文件匹配错误导致。

### 5.2 对本安型手机误判缺少隔爆型标志

原始 finding 中存在类似结论：

```text
防爆标志缺少保护等级标识
报告第 12.4 节给出的防爆标志为 "Ex ib I Mb"，未包含隔爆型保护等级标识（如 "db"）。
```

问题：

- 测试产品名称包含“本安型手机”，其保护型式应优先按本质安全型 `ib` 判断。
- 不能默认要求本安产品包含隔爆型 `db` 标志。
- 如果依据来自隔爆或隔爆兼本安产品，应先经过产品类型/保护型式适配校验。

### 5.3 部分 finding 内容不完整

首条 finding 的 content 为：

```text
报告片段中产品名称为“矿用本安型手机”，防爆标志标注为
```

问题：

- 句子未完整说明“标注为什么”。
- 该类不完整内容不应进入最终问题清单。
- 建议工作流在 `result_validate` 阶段增加内容完整性校验。

## 6. 建议工作流侧调整项

### 6.1 页码定位

工作流终态输出每条 finding 时，必须把分片页码展开到结构化字段：

```json
{
  "location": {
    "page": 6,
    "pageNo": 6,
    "page_no": 6,
    "section": "技术参数",
    "quote": "对应原文短引用",
    "source_chunk_id": 155,
    "source_chunk_no": 1
  },
  "page": 6,
  "pageNo": 6,
  "page_no": 6
}
```

最低要求：

```json
{
  "location": {
    "page": 6
  }
}
```

业务侧会使用 `location.page` 作为第一优先级。

### 6.2 知识库检索范围

请工作流侧检查本次任务的知识库检索输入和过滤条件：

```text
产品名称：本安-矿用本安型手机2
报告文件：etst_20260512153955A001.docx
basis_file_count: 0
reference_count: 120
source_chunk_count: 15
```

需要确认：

1. `knowledge_scope` 是否正确限定到本安型手机相关依据。
2. 是否错误命中了控制箱、隔爆兼本安设备、KXJ127 等不相关依据。
3. 当 `basis_file_count = 0` 时，是否应降低结论强度，避免引用不匹配的企业标准给出确定性不合格结论。
4. 是否应按产品类型、保护型式、防爆标志先做依据过滤，再进入模型审核。

### 6.3 结果校验

建议 `result_validate` 阶段增加以下校验：

1. 每条 finding 必须有完整 `title/content/suggestion`。
2. 每条 finding 必须有报告原文证据 `location.quote`。
3. 每条 finding 必须有对应依据证据，例如 `basis_quote`、`basis_file_name`、`basis_chunk_id` 或同等字段。
4. 如果依据产品类型与报告产品类型不一致，应剔除或降级为“需人工确认”，不能作为明确问题输出。
5. 如果无法定位页码，应保留 finding 但不返回 `location.page`，同时在内部日志中记录缺页码原因。
6. 对明显不完整句子，例如 content 以“标注为”结尾，应判定为无效 finding。

### 6.4 结果数量控制

本次返回 23 条问题，数量偏多且包含多条疑似误判。

建议工作流侧在分片审核合并阶段：

1. 合并重复问题。
2. 剔除依据不匹配的问题。
3. 优先输出高置信度、高影响问题。
4. 对低置信度问题增加 `confidence` 字段，业务侧后续可按置信度展示或过滤。

建议字段：

```json
{
  "confidence": 0.82,
  "basis": {
    "file_name": "GB/T 3836.4-2021.pdf",
    "section": "相关条款",
    "quote": "依据原文短引用",
    "chunk_id": 123
  }
}
```

## 7. 建议工作流侧回传的诊断信息

为便于下次联调，请工作流侧针对工作流任务 `12` 提供以下信息：

1. `source_chunk_id = 155..169` 对应的 `page_no`、文本范围、页码来源。
2. 本次检索命中的 120 条 reference 的文件名、产品类型、标准号、top_k 分数。
3. 是否命中了 `KXJ127` 控制箱相关依据，以及命中原因。
4. 本次 `knowledge_scope` 的实际入参和过滤条件。
5. docx 解析链路是否支持页码，如果支持，为什么终态 finding 没有输出 `location.page`。
6. `result_validate` 是否检查了 finding 内容完整性、依据匹配性、页码字段完整性。

## 8. 业务侧当前状态

业务侧已经完成以下能力：

1. `audit_ai_finding` 已新增 `page_no` 和 `location_json` 字段。
2. 终态回调解析已支持 `location.page`、`location.pageNo`、`location.page_no`、顶层 `page/pageNo/page_no`。
3. AI任务详情页“检测结果”已只读取 `audit_ai_finding`，不再使用审核详情预置 issue 兜底。
4. 点击检测结果时，如果 `page_no` 有效且不超出 PDF 总页数，会通过 PDF `#page=N` 跳转。
5. 如果没有有效页码，会提示“暂无定位信息”。

因此，本次任务无法跳转的阻断点在工作流终态结果未返回页码。

## 9. 联调验收建议

工作流侧修复后，请使用同一任务或新任务重新跑一次，验收以下 SQL 结果：

```sql
select finding_id, sort_num, finding_title, page_no, location_json
from audit_ai_finding
where ai_task_id = 17
order by sort_num, finding_id;
```

预期：

1. 至少主要问题的 `page_no` 不为 `NULL`。
2. `location_json` 中包含 `page`。
3. 前端检测结果卡片显示“第 N 页”标签。
4. 点击带页码的问题，报告预览跳转到对应页。
5. 输出问题不再出现与“本安-矿用本安型手机2”明显不匹配的控制箱依据或隔爆型误判。

