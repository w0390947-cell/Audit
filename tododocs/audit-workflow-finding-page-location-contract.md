# AI审核问题页码定位字段对接说明

日期：2026-05-12

本文档面向工作流系统侧，用于约定 AI 审核结果中每条问题的页码定位字段。目标是支持业务系统“AI任务详情 -> 检测结果”与“报告预览”联动：用户点击某条检测问题后，报告预览自动跳转到该问题所在页。

## 1. 背景

业务系统“AI任务详情”界面包含两个核心组件：

- `报告预览`：展示用户提交的报告文件。
- `检测结果`：展示 AI 工作流返回的审核问题列表。

当前希望第一阶段实现页码级联动：

1. 用户点击检测结果中的某条问题。
2. 前端读取该问题的页码定位信息。
3. 报告预览跳转到对应页码。
4. 如果该问题没有页码定位，前端提示“暂无定位信息”。

本阶段暂不要求页内高亮、坐标框选、段落定位或文字选区定位。

## 2. 工作流侧需要返回的字段

请工作流系统侧在终态回调 `result.findings` 或 `result.issues` 的每条问题中返回页码信息。

推荐字段结构：

```json
{
  "type": "数据错误",
  "title": "报告编号与依据文件不一致",
  "content": "报告首页编号与依据文件登记编号不一致。",
  "severity": "medium",
  "location": {
    "page": 1
  },
  "suggestion": "请核对并统一报告编号。"
}
```

字段要求：

| 字段 | 是否必填 | 说明 |
| --- | --- | --- |
| `location.page` | 强烈建议必填 | 问题所在报告页码，从 1 开始计数。 |
| `location.section` | 可选 | 问题所在章节、标题或段落描述。第一阶段前端不依赖。 |
| `location.quote` | 可选 | 问题对应的原文短引用。第一阶段前端不依赖。 |

## 3. 兼容字段建议

为了降低对接风险，如果工作流侧当前已有页码字段，也可以同步返回以下兼容字段之一：

```json
{
  "page": 1,
  "page_no": 1,
  "pageNo": 1,
  "location": {
    "page": 1,
    "page_no": 1,
    "pageNo": 1
  }
}
```

业务系统后续实现时可以兼容读取：

1. `location.page`
2. `location.pageNo`
3. `location.page_no`
4. `page`
5. `pageNo`
6. `page_no`

但推荐工作流侧统一使用 `location.page`。

## 4. 页码规则

请工作流系统侧确认并遵守以下页码规则：

1. 页码从 `1` 开始，不使用从 `0` 开始的索引。
2. 页码必须对应业务系统“报告预览”中的页码。
3. 如果原始文件是 Word / doc / docx，请以最终预览文件的分页为准。
4. 如果工作流侧基于转换后的 PDF 做解析，请返回转换后 PDF 的页码。
5. 如果工作流侧无法确认页码，不要猜测页码；可以不返回页码，由前端提示“暂无定位信息”。

## 5. 多页问题的处理

第一阶段只支持跳转到一个页码。

如果一个问题涉及多页，建议返回：

```json
{
  "location": {
    "page": 3,
    "pages": [3, 4]
  }
}
```

说明：

- `location.page` 表示默认跳转页。
- `location.pages` 表示涉及页码列表，供后续扩展使用。
- 第一阶段前端只使用 `location.page`。

## 6. 找不到页码时的处理

如果工作流侧无法定位页码，请返回问题本身，但可以省略 `location.page`：

```json
{
  "type": "其他",
  "title": "缺少必要说明",
  "content": "报告中缺少某项说明，但当前无法定位具体页码。",
  "severity": "low",
  "location": {
    "section": "未知"
  }
}
```

业务系统前端会在用户点击该问题时提示：

```text
暂无定位信息
```

## 7. 完整成功回调中的示例

```json
{
  "callback_event_id": "wf-audit-123-wf-run-987654-success",
  "biz_id": "AI-TASK-123",
  "workflow_task_id": "wf-run-987654",
  "task_status": "SUCCESS",
  "status": "completed",
  "progress_percent": 100,
  "progress_text": "AI审核工作流执行完成",
  "stages": [],
  "result": {
    "summary": "本次AI审核发现2个问题，建议按问题清单修订报告。",
    "findings": [
      {
        "type": "数据错误",
        "title": "报告编号与依据文件不一致",
        "content": "报告首页编号与依据文件登记编号不一致。",
        "severity": "medium",
        "location": {
          "page": 1,
          "section": "报告首页"
        },
        "suggestion": "请核对并统一报告编号。"
      },
      {
        "type": "内容缺失",
        "title": "缺少防爆标志说明",
        "content": "报告中未见防爆标志 Ex ib I Mb 的完整说明。",
        "severity": "high",
        "location": {
          "page": 6,
          "section": "技术参数"
        },
        "suggestion": "请补充防爆标志及适用标准说明。"
      }
    ]
  }
}
```

## 8. 需要工作流系统侧确认的问题

请工作流系统侧确认：

1. 当前 AI 审核问题是否已经具备页码字段？
2. 如果已有页码字段，字段名是什么？是否能统一改为 `location.page`？
3. 页码来源是什么：原始 Word 解析页码、PDF 转换页码、OCR 页码，还是模型推断页码？
4. 如果报告是 doc/docx，工作流侧是否能基于最终预览 PDF 的页码返回？
5. 是否能保证页码从 `1` 开始？
6. 如果问题涉及多页，是否能同时返回 `location.page` 和 `location.pages`？
7. 如果无法定位页码，是否可以明确不返回 `location.page`，避免错误跳转？

## 9. 工作流系统侧处理记录

处理日期：2026-05-12

已完成工作流侧适配：

1. AI审核分片模式下，工作流会基于 `audit_task_content_chunk.page_no` 补齐每条 `finding.location.page`。
2. `location` 已统一规范为对象结构，优先输出：

```json
{
  "location": {
    "page": 1,
    "pageNo": 1,
    "page_no": 1,
    "section": "报告首页",
    "quote": "问题对应原文短引用"
  }
}
```

3. 为兼容业务系统读取逻辑，页码同时补充到顶层 `page`、`pageNo`、`page_no`。
4. 页码来源不是模型猜测，而是待审核报告分片元数据中的 `page_no`。PDF 解析时该页码从 1 开始；Word/doc/docx 当前解析链路没有可靠最终预览分页时，不会凭空猜测页码。
5. 如果模型返回字符串型 `location`，工作流会转换为 `location.section`，并保留可确定的页码。
6. 结果保存时不再把 `finding.location` 二次包成 `{ "text": ... }`，会保留 `location.page` 等结构化字段。

当前限制：

- 多页问题暂不主动生成 `location.pages`；如果模型后续返回 `pages`，校验与保存链路不会删除该字段。
- 如果分片没有 `page_no`，工作流不会返回 `location.page`，业务系统前端可按本契约提示“暂无定位信息”。
