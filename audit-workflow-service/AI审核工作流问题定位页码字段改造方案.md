# AI审核工作流问题定位页码字段改造方案

## 1. 背景

业务系统 `AI任务详情` 页面左侧展示报告预览，右侧展示 AI 检测结果。当前工作流系统返回的问题项中，问题位置主要写在自然语言字段里，例如：

```json
{
  "title": "检验报告签字人员信息缺失",
  "content": "报告片段中“批准”、“审核”、“主检”栏目后仅有“电子签”字样或为空白…… 位置：第 1 页，检验结论下方签字栏。建议：补充完整批准人、审核人、主检人的姓名或有效电子签名。",
  "location": "第1页页眉"
}
```

业务系统如果要实现“点击问题位置后，左侧报告预览自动跳转到对应页”，目前只能从 `content` 或 `location` 中正则提取“第 N 页”。这种方式不稳定，容易受模型表述变化影响。

因此需要工作流系统侧在每个审核发现项 `findings[]` 中返回结构化页码字段，业务系统直接消费该字段完成 PDF 页码跳转。

## 2. 改造目标

工作流系统需要在查询结果和终态 callback 中，为每个问题项补充结构化定位字段：

- `page_no`：问题所在报告页码，数字类型，从 1 开始。
- `location_text`：页内位置描述，字符串类型。
- `quote`：可选，原文摘录，便于人工核验和后续高亮。

业务系统第一阶段只依赖 `page_no` 做页码跳转；`location_text` 和 `quote` 主要用于页面展示和后续扩展。

## 3. 字段约定

### 3.1 findings 新增字段

在以下两个返回位置都应补充字段：

1. `GET /api/audit/tasks/{taskId}/result` 返回的 `data.findings[]`
2. 终态 callback 返回的 `result.findings[]`

推荐结构：

```json
{
  "type": "内容缺失",
  "title": "检验报告签字人员信息缺失",
  "content": "报告片段中“批准”、“审核”、“主检”栏目后仅有“电子签”字样或为空白，未见具体人员姓名或有效电子签名标识。",
  "finding_type": "内容缺失",
  "finding_title": "检验报告签字人员信息缺失",
  "finding_content": "报告片段中“批准”、“审核”、“主检”栏目后仅有“电子签”字样或为空白，未见具体人员姓名或有效电子签名标识。",
  "severity": "medium",
  "page_no": 1,
  "location_text": "检验结论下方签字栏",
  "location": "第 1 页，检验结论下方签字栏",
  "quote": "批准：电子签；审核：电子签；主检：",
  "suggestion": "补充完整批准人、审核人、主检人的姓名或有效电子签名。",
  "source_chunk_id": 349,
  "source_chunk_no": 1,
  "sort_num": 1
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page_no` | number / null | 建议必填 | 问题所在报告页码，从 1 开始。无法判断时返回 `null`，不要返回 0 或字符串。 |
| `location_text` | string | 建议必填 | 页内位置描述，例如 `检验结论下方签字栏`、`第 4 页主要零部件表`。无法判断时返回空字符串。 |
| `location` | string | 保留 | 人类可读位置，可组合为 `第 {page_no} 页，{location_text}`。用于兼容现有业务系统展示。 |
| `quote` | string | 可选 | 原文摘录。后续如果业务系统做页内搜索或高亮，可优先使用该字段。 |

### 3.2 命名要求

统一使用：

```text
page_no
location_text
quote
```

不要混用 `pageNo`、`page`、`page_number` 作为主字段。若工作流系统已有内部字段，可以在内部转换为上述字段后再输出。

### 3.3 页码规则

`page_no` 必须满足：

- 从 1 开始计数，与 PDF 预览页码一致。
- 类型为数字，不要返回字符串 `"1"`。
- 如果一个问题跨多页，第一阶段返回主问题所在页。可选扩展字段见第 8 节。
- 如果模型无法判断页码，返回 `null`。

## 4. 工作流侧处理要求

### 4.1 文件解析阶段应保留页码信息

工作流在解析报告文件时，应尽量让后续分片携带页码来源信息。

建议每个待审文件分片至少包含：

```json
{
  "source_chunk_id": 349,
  "source_chunk_no": 1,
  "page_no": 1,
  "section_title": "检验结论",
  "text": "..."
}
```

如果 DOC/DOCX 转换或解析无法精确到页，应采用可解释策略：

- 如果解析器能获得分页符或页码，使用真实页码。
- 如果只能按文本分片，允许返回估算页码，但应在内部日志中标记来源。
- 如果无法估算，返回 `page_no=null`，不要编造页码。

### 4.2 模型提示词必须要求输出页码

AI 审核 prompt 中，应明确要求模型对每个问题返回结构化字段：

```text
每个 findings[] 元素必须包含：
- type：问题类型
- title：问题标题
- content：问题描述
- severity：严重程度
- page_no：问题所在报告页码，数字类型，从 1 开始；无法判断时为 null
- location_text：页内位置描述，不要包含页码
- location：人类可读位置，格式建议为“第 N 页，xxx”
- quote：报告原文摘录
- suggestion：整改建议
```

同时应禁止模型把页码只写在 `content` 中：

```text
不要只在 content 中描述“第几页”。page_no 必须单独输出为数字字段。
```

### 4.3 结果规范化阶段应兜底补齐

工作流系统应在模型输出后做规范化处理，避免业务系统收到不稳定结构。

规范化建议：

1. 如果模型返回了 `page_no` 且是有效数字，直接保留。
2. 如果模型未返回 `page_no`，但 `location` 或 `content` 中出现 `第 N 页`，可兜底解析并写入 `page_no=N`。
3. 如果 `location_text` 为空，但 `location` 为 `第 N 页，xxx`，可去掉页码部分后写入 `location_text=xxx`。
4. 如果 `location` 为空，但有 `page_no` 和 `location_text`，生成 `location="第 N 页，xxx"`。
5. 如果无法判断页码，返回 `page_no=null`，保留 `location_text` 或 `location` 中的自然语言描述。

### 4.4 callback 规范化输出

终态 callback 中 `result.findings[]` 必须包含规范化后的字段。示例：

```json
{
  "result": {
    "summary": "本次审核发现 3 个问题",
    "findings": [
      {
        "type": "内容缺失",
        "title": "检验报告签字人员信息缺失",
        "content": "报告签字栏缺少具体人员信息。",
        "finding_type": "内容缺失",
        "finding_title": "检验报告签字人员信息缺失",
        "finding_content": "报告签字栏缺少具体人员信息。",
        "severity": "medium",
        "page_no": 1,
        "location_text": "检验结论下方签字栏",
        "location": "第 1 页，检验结论下方签字栏",
        "quote": "批准：电子签；审核：电子签；主检：",
        "suggestion": "补充完整签字人员信息。",
        "sort_num": 1
      }
    ]
  }
}
```

## 5. 兼容要求

工作流系统必须继续保留现有字段：

- `location`
- `source_chunk_id`
- `source_chunk_no`
- `type/title/content`
- `finding_type/finding_title/finding_content`

新增字段不得破坏现有业务系统解析逻辑。业务系统在升级前会忽略未知字段；升级后会优先使用 `page_no`。

## 6. 业务系统消费方式

业务系统计划按以下方式使用：

1. callback 到达后，落库 `result.findings[]`。
2. 每个问题项保存或透传 `page_no`、`location_text`。
3. `AI任务详情` 页面中，在检测结果卡片里展示：

```text
位置：第 1 页，检验结论下方签字栏
```

4. 用户点击位置后，左侧 PDF 预览跳转：

```text
preview.pdf#page=1
```

5. 如果 `page_no` 为空，则位置只展示为普通文本，不提供跳转。

## 7. 验收用例

### 7.1 正常页码

模型发现问题在第 1 页签字栏。

期望返回：

```json
{
  "page_no": 1,
  "location_text": "检验结论下方签字栏",
  "location": "第 1 页，检验结论下方签字栏"
}
```

### 7.2 多位页码

模型发现问题在第 12 页附表。

期望返回：

```json
{
  "page_no": 12,
  "location_text": "附表 2 检验收费明细表",
  "location": "第 12 页，附表 2 检验收费明细表"
}
```

### 7.3 无法判断页码

模型只能判断章节，但无法确认页码。

期望返回：

```json
{
  "page_no": null,
  "location_text": "检验依据章节",
  "location": "检验依据章节"
}
```

### 7.4 callback 与结果查询一致

同一工作流任务：

- `GET /api/audit/tasks/{taskId}/result` 中 `findings[0].page_no = 1`
- 终态 callback 中 `result.findings[0].page_no = 1`

两处字段应一致。

## 8. 工作流侧处理结果

处理时间：2026-05-11 晚间

### 8.1 已确认现有页码来源

工作流系统当前待审文件分片表已经具备页码字段：

```text
audit_task_content_chunk.page_no
```

其中：

1. PDF 文件解析时可以按真实 PDF 页码写入 `page_no`。
2. TXT、DOC、DOCX 当前解析器无法稳定获得真实分页，分片 `page_no` 可能为 `null`。
3. 如果模型能从报告文本中识别出“第 N 页”，结果规范化阶段会兜底解析页码。
4. 如果无法判断页码，按约定返回 `page_no=null`，不返回 0，不编造页码。

### 8.2 已增强 AI 审核 Prompt

已在分片审核 Prompt 和默认业务报告审核 Prompt 中要求模型对每个 `findings[]` 输出：

```text
page_no
location_text
location
quote
```

并明确要求：

1. `page_no` 必须为数字或 `null`，从 1 开始。
2. 不得只把页码写在 `content` 中。
3. `location_text` 不包含页码。
4. `location` 建议为 `第 N 页，xxx`。
5. `quote` 为待审报告原文摘录。

涉及代码：

```text
src/main/java/com/audit/workflow/node/AiAuditNodeExecutor.java
```

### 8.3 已增强模型结果规范化

已在结果校验阶段统一补齐每个 `findings[]` 的结构化定位字段。

规范化逻辑：

1. 如果模型返回了有效 `page_no`，直接保留。
2. 如果模型返回了 `pageNo`、`page_number`、`page` 等变体字段，会转换为主字段 `page_no`。
3. 如果模型未返回 `page_no`，但 `location`、`content` 或 `title` 中包含 `第 N 页`，会解析出 `page_no=N`。
4. 如果仍无法识别页码，会使用该问题所属报告分片的 `page_no`。
5. 如果最终仍无法判断页码，返回 `page_no=null`。
6. 如果 `location_text` 为空，会从 `location` 中去掉页码前缀后补齐。
7. 如果 `location` 为空，但有 `page_no` 和 `location_text`，会生成 `location="第 N 页，xxx"`。
8. 如果 `quote` 为空，会保留为空字符串，不影响业务系统解析。

涉及代码：

```text
src/main/java/com/audit/workflow/node/ResultValidateNodeExecutor.java
```

### 8.4 已同步结果保存转换

当工作流将 `findings[]` 转换为内部 `issues[]` 保存时，已同步携带：

```text
source_chunk_id
page_no
location_text
quote
```

这样后续如果业务系统或工作流后台从问题明细表排查，也可以看到定位信息。

涉及代码：

```text
src/main/java/com/audit/workflow/node/ResultSaveNodeExecutor.java
```

### 8.5 返回结构

修复后，以下两个位置都会包含结构化定位字段：

1. `GET /api/audit/tasks/{taskId}/result` 返回的 `data.findings[]`
2. 终态 callback 返回的 `result.findings[]`

示例：

```json
{
  "type": "内容缺失",
  "title": "检验报告签字人员信息缺失",
  "content": "报告签字栏缺少具体人员信息。",
  "finding_type": "内容缺失",
  "finding_title": "检验报告签字人员信息缺失",
  "finding_content": "报告签字栏缺少具体人员信息。",
  "severity": "medium",
  "page_no": 1,
  "location_text": "检验结论下方签字栏",
  "location": "第 1 页，检验结论下方签字栏",
  "quote": "批准：电子签；审核：电子签；主检：",
  "suggestion": "补充完整签字人员信息。",
  "source_chunk_id": 349,
  "source_chunk_no": 1,
  "sort_num": 1
}
```

### 8.6 验证结果

已执行编译验证：

```bash
mvn -DskipTests compile
```

结果：

```text
BUILD SUCCESS
```

### 8.7 需要业务系统侧复测确认

请业务系统侧在工作流服务重启后重新触发审核任务，重点确认：

1. `GET /api/audit/tasks/{taskId}/result` 中 `findings[].page_no` 为数字或 `null`。
2. 终态 callback 中 `result.findings[].page_no` 与结果查询接口一致。
3. `location_text` 不包含页码。
4. `location` 仍保留原有人类可读位置描述。
5. PDF 输入场景下，页面跳转可直接使用 `page_no`。
6. DOC/DOCX 如果无法稳定获取真实页码，允许 `page_no=null`，业务系统不应提供跳转。

## 9. 后续扩展字段

第一阶段只要求 `page_no` 跳页。后续如果要实现页内滚动、高亮或定位到文字区域，可扩展：

```json
{
  "page_no": 1,
  "location_text": "检验结论下方签字栏",
  "quote": "批准：电子签；审核：电子签；主检：",
  "bbox": {
    "x": 120,
    "y": 680,
    "width": 360,
    "height": 80
  }
}
```

`bbox` 坐标约定需要单独确认：

- 坐标原点在页面左上还是左下。
- 单位是 PDF point、像素还是相对比例。
- 是否基于转换后的预览 PDF 页面尺寸。

当前阶段不要求返回 `bbox`。

## 10. 最小交付要求

工作流系统侧最小需要完成：

1. `result.findings[]` 增加 `page_no`。
2. `result.findings[]` 增加 `location_text`。
3. 继续保留 `location`。
4. 查询结果接口和终态 callback 两处都返回。
5. `page_no` 为数字类型，从 1 开始；无法判断时为 `null`。

满足以上要求后，业务系统即可实现“点击检测结果位置，左侧报告预览跳转到对应页”的第一阶段能力。
