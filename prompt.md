请你先不要改动代码，先描述你理解的我交给你的这个任务，如果你有任何不清楚、不明白、不理解的问题，可以随时向我提问，确保你完全理解我交给你的这个任务。

很好，你的理解没有问题。针对你提出的问题，我的回答如下：
"""
1. 我想要终态 callback 的 result.model_used / raw_output.model_used 里也要一起删除。
2. 我想要数据库 audit_result.result_json 中保存的历史/内部字段也不再保留。
"""
现在，请你完成该任务。

本安-矿用本安型手机
山西矿安智能设备有限公司



'''json
{
    "code":200,
    "message":"success",
    "data":{
        "success":true,
        "summary":"未发现关键问题；AI审核生成21个候选问题，经结果校验过滤21个。",
        "findings":[

        ],
        "model_used":"qwen3.5-plus",
        "diagnostics":{
            "filter_reasons":[
                {
                    "count":20,
                    "reason":"finding_quote_missing",
                    "message":"候选问题缺少报告原文 quote，无法定位问题原文"
                },
                {
                    "count":1,
                    "reason":"finding_content_incomplete",
                    "message":"候选问题描述不完整，无法形成可保存的有效问题"
                }
            ],
            "filtered_count":21,
            "candidate_count":21,
            "filtered_findings":[
                {
                    "type":"finding_filtered",
                    "title":"委托编号存在比对性错误",
                    "reason":"finding_quote_missing",
                    "content":"报告页眉及正文中的委托编号'№：2025520398FB'与 LIMS 系统或任务单档案明细表右上角编号不一致，存在比对性错误。根据审核依据，防爆报告编号应严格遵循规则（加 FB），且每一页页眉需与此编号保持一致，当前报告显示编号有误。",
                    "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                    "location":{
                        "section":"报告页眉及委托编号栏",
                        "source_chunk_id":479,
                        "source_chunk_no":1
                    },
                    "severity":"high",
                    "finding_index":1,
                    "source_chunk_id":479
                },
                {
                    "type":"finding_filtered",
                    "title":"检验结论签发日期未填写",
                    "reason":"finding_quote_missing",
                    "content":"报告检验结论部分显示“签发日期：年 月 日”，具体日期内容为空，不符合检验报告完整性要求。",
                    "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                    "location":{
                        "section":"检验结论段落",
                        "source_chunk_id":480,
                        "source_chunk_no":2
                    },
                    "severity":"high",
                    "finding_index":2,
                    "source_chunk_id":480
                },
                {
                    "type":"finding_filtered",
                    "title":"关键人员签字缺失",
                    "reason":"finding_quote_missing",
                    "content":"报告批准、审核、主检栏目仅显示职务名称，缺少具体人员签字或电子签章，依据注意事项第 3 条，无签字（章）报告无效。",
                    "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                    "location":{
                        "section":"报告签署栏",
                        "source_chunk_id":480,
                        "source_chunk_no":2
                    },
                    "severity":"high",
                    "finding_index":3,
                    "source_chunk_id":480
                },
                {
                    "type":"finding_filtered",
                    "title":"试验环境条件与产品正常工作环境不一致",
                    "reason":"finding_quote_missing",
                    "content":"待审阅报告显示试验环境温度为 10℃～30℃，而知识库中该产品说明书规定的正常工作环境温度范围为 0℃～40℃，试验环境覆盖范围不足，未涵盖产品允许工作的最低和最高温度极限。",
                    "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                    "location":{
                        "section":"一、试验环境地点、条件描述",
                        "source_chunk_id":480,
                        "source_chunk_no":2
                    },
                    "severity":"medium",
                    "finding_index":4,
                    "source_chunk_id":480
                },
                {
                    "type":"finding_filtered",
                    "title":"产品名称与报告内容不一致",
                    "reason":"finding_quote_missing",
                    "content":"待审阅报告元信息显示产品名称为'本安 - 矿用本安型手机'，但报告正文及知识库依据（KXJ127 说明书、检验报告批注本）均指向'KXJ127 矿用隔爆兼本安型 PLC 控制箱'，产品名称存在严重不符。",
                    "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                    "location":{
                        "section":"待审阅报告片段元信息 - product_name 字段",
                        "source_chunk_id":481,
                        "source_chunk_no":3
                    },
                    "severity":"high",
                    "finding_index":5,
                    "source_chunk_id":481
                },
                {
                    "type":"finding_filtered",
                    "title":"缺少报告关键签署信息",
                    "reason":"finding_quote_missing",
                    "content":"待审阅报告片段中未包含主检、审核、批准人签字（章），依据知识库搜索结果中'注意事项'第 3 条规定，无签字（章）的报告无效。",
                    "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                    "location":{
                        "section":"待审阅报告全文",
                        "source_chunk_id":481,
                        "source_chunk_no":3
                    },
                    "severity":"high",
                    "finding_index":6,
                    "source_chunk_id":481
                },
                {
                    "type":"finding_filtered",
                    "title":"页码连续性异常",
                    "reason":"finding_quote_missing",
                    "content":"报告片段显示页码从'第 1 页 共 22 页'直接跳转至'第 4 页 共 22 页'，中间缺失第 2、3 页内容，且第 4 页标注'本页内以下空白'后直接接'三、检验结果及结论'，排版逻辑混乱。",
                    "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                    "location":{
                        "section":"待审阅报告页眉及页面内容",
                        "source_chunk_id":481,
                        "source_chunk_no":3
                    },
                    "severity":"medium",
                    "finding_index":7,
                    "source_chunk_id":481
                },
                {
                    "type":"finding_filtered",
                    "title":"报告章节标题缺失",
                    "reason":"finding_quote_missing",
                    "content":"待审阅报告中第 12 章仅有章节编号",
                    "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                    "location":{
                        "section":"7.1.2.4 粘结材料 符合GB/T 3836.4表5要求，排除本条款 N/A",
                        "source_chunk_id":482,
                        "source_chunk_no":4
                    },
                    "severity":"medium",
                    "finding_index":8,
                    "source_chunk_id":482
                },
                {
                    "type":"finding_filtered",
                    "title":"产品名称与审核依据不一致",
                    "reason":"finding_content_incomplete",
                    "content":"待审阅报告元信息显示产品名称为“本安 - 矿用本安型手机”，而知识库搜索返回的审核依据（产品说明书及检验报告）中明确记载的产品名称为",
                    "message":"候选问题描述不完整，无法形成可保存的有效问题",
                    "location":{
                        "section":"15.1.2 外部等电位联结 按照 GB/T 3836.4 规定，排除本要求 N/A",
                        "source_chunk_id":483,
                        "source_chunk_no":5
                    },
                    "severity":"medium",
                    "finding_index":9,
                    "source_chunk_id":483
                },
                {
                    "type":"finding_filtered",
                    "title":"产品名称与执行标准不匹配",
                    "reason":"finding_quote_missing",
                    "content":"待审阅报告元信息显示产品名称为'本安 - 矿用本安型手机'，但知识库搜索结果显示的依据文件（说明书及检验报告）均为'KXJ127 矿用隔爆兼本安型 PLC 控制箱'，且执行标准为 GB/T 3836.1/2/4-2021 等针对控制箱的标准。报告对象产品与审核依据中的产品完全不一致，导致依据无法支撑对该手机的合规性审查。",
                    "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                    "location":{
                        "section":"17.2.5.3 风扇和风扇罩 按照 GB/T 3836.4 规定，排除本要求 N/A",
                        "source_chunk_id":484,
                        "source_chunk_no":6
                    },
                    "severity":"high",
                    "finding_index":10,
                    "source_chunk_id":484
                },
                {
                    "type":"finding_filtered",
                    "title":"待审阅报告产品名称与知识库依据不一致",
                    "reason":"finding_quote_missing",
                    "content":"待审阅报告元信息显示产品名称为'本安 - 矿用本安型手机'，而知识库搜索结果中的执行标准、说明书及检验报告均指向'KXJ127 矿用隔爆兼本安型 PLC 控制箱'，两者产品名称及类型完全不符，无法确认报告内容的适用性。",
                    "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                    "location":{
                        "section":"待审阅报告片段元信息 - product_name",
                        "source_chunk_id":485,
                        "source_chunk_no":7
                    },
                    "severity":"high",
                    "finding_index":11,
                    "source_chunk_id":485
                },
                {
                    "type":"finding_filtered",
                    "title":"防爆标志与产品实际防爆型式不一致",
                    "reason":"finding_quote_missing",
                    "content":"待审阅报告第 29.4 条款中记录的爆炸性气体环境防爆标志为'Ex ib Ⅰ Mb'，表明产品仅为本质安全型。然而，知识库搜索结果显示该产品（KXJ127）的说明书及分类明确标注其防爆型式为'矿用隔爆兼本质安全型'，对应的防爆标志应为'Ex db [ib Mb]ⅠMb'。报告中的标志遗漏了隔爆部分'db'，与产品实际认证信息及说明书描述不符。",
                    "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                    "location":{
                        "section":"26.8 耐热试验 符合GB/T 3836.4表5要求，排除本条款 N/A",
                        "source_chunk_id":486,
                        "source_chunk_no":8
                    },
                    "severity":"high",
                    "finding_index":12,
                    "source_chunk_id":486
                },
                {
                    "type":"finding_filtered",
                    "title":"电缆引入装置要求排除依据不足",
                    "reason":"finding_quote_missing",
                    "content":"待审阅报告中附录 A 多项条款（A.1 至 A.4.2）均标注“按照 GB/T 3836.4 规定，排除本要求”，但知识库搜索结果显示该产品为“矿用隔爆兼本安型 PLC 控制箱”，其结构描述中明确包含",
                    "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                    "location":{
                        "source_chunk_id":487,
                        "source_chunk_no":9
                    },
                    "severity":"medium",
                    "finding_index":13,
                    "source_chunk_id":487
                },
                {
                    "type":"finding_filtered",
                    "title":"产品型号与名称不一致",
                    "reason":"finding_quote_missing",
                    "content":"待审阅报告元信息显示产品名称为'本安 - 矿用本安型手机'，但知识库搜索结果显示的说明书及检验报告均针对'KXJ127 矿用隔爆兼本安型 PLC 控制箱'，两者产品名称及类型完全不符。",
                    "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                    "location":{
                        "section":"待审阅报告片段元信息 - product_name",
                        "source_chunk_id":488,
                        "source_chunk_no":10
                    },
                    "severity":"high",
                    "finding_index":14,
                    "source_chunk_id":488
                },
                {
                    "type":"finding_filtered",
                    "title":"产品名称与审核依据不一致",
                    "reason":"finding_quote_missing",
                    "content":"待审阅报告元信息中产品名称为'本安 - 矿用本安型手机'，而知识库搜索结果显示的审核依据（产品说明书及检验报告）对应的产品均为'KXJ127 矿用隔爆兼本安型 PLC 控制箱'，两者产品名称及类型完全不符，无法基于当前依据对该手机产品报告进行合规性判定。",
                    "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                    "location":{
                        "section":"6.3.9 涂层下的间距 电路板涂两遍三防漆处理，涂层下的爬电距离满足要求。 P",
                        "source_chunk_id":489,
                        "source_chunk_no":11
                    },
                    "severity":"high",
                    "finding_index":15,
                    "source_chunk_id":489
                },
                {
                    "type":"finding_filtered",
                    "title":"防爆标志表述不一致",
                    "reason":"finding_quote_missing",
                    "content":"待审阅报告第 12.4 节中记录的防爆标志为'Ex ib I Mb'，而依据产品说明书（知识库搜索结果）第 2.2 节规定的防爆标志应为'Ex db [ib Mb]ⅠMb'。报告中的标志缺失了隔爆部分'db'及关联符号，与产品实际认证信息不符。",
                    "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                    "location":{
                        "section":"10.5 电池和电池组试验 1.十只电池试验后吸水纸及试验样品表面无电解液痕迹； 2.电池通过了火花点燃试验； 3.十只电池最高表面温度为：88.4℃。 P",
                        "source_chunk_id":491,
                        "source_chunk_no":13
                    },
                    "severity":"high",
                    "finding_index":16,
                    "source_chunk_id":491
                },
                {
                    "type":"finding_filtered",
                    "title":"检验报告编号不一致",
                    "reason":"finding_quote_missing",
                    "content":"待审阅报告中出现编号'№：201854833'的页眉，与本报告主编号'№：2025520398FB'及知识库依据中的报告编号不一致，存在引用错误或页面混排。",
                    "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                    "location":{
                        "section":"第 16 页页眉",
                        "source_chunk_id":492,
                        "source_chunk_no":14
                    },
                    "severity":"high",
                    "finding_index":17,
                    "source_chunk_id":492
                },
                {
                    "type":"finding_filtered",
                    "title":"缺少签字盖章信息",
                    "reason":"finding_quote_missing",
                    "content":"报告片段中未体现主检、审核、批准人签字（章）及'检验检测专用章'，依据知识库注意事项第 3 条和第 1 条，无签字盖章的报告无效。",
                    "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                    "location":{
                        "section":"报告签署页/末页",
                        "source_chunk_id":492,
                        "source_chunk_no":14
                    },
                    "severity":"high",
                    "finding_index":18,
                    "source_chunk_id":492
                },
                {
                    "type":"finding_filtered",
                    "title":"受控部件明细表格式错乱",
                    "reason":"finding_quote_missing",
                    "content":"主要零 (元) 受控部件及重要原材料明细表中，'规格型号’、'生产单位’、'安标编号’等列内容错位（如电池容量'2500mAh'出现在生产单位列前，签发日期出现在安标编号列），且包含批注文字'Comment by 缘鱼曦望’，不符合正式报告规范。",
                    "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                    "location":{
                        "section":"主要零 (元) 受控部件及重要原材料明细表",
                        "source_chunk_id":492,
                        "source_chunk_no":14
                    },
                    "severity":"medium",
                    "finding_index":19,
                    "source_chunk_id":492
                },
                {
                    "type":"finding_filtered",
                    "title":"检验报告缺少必要签字",
                    "reason":"finding_quote_missing",
                    "content":"待审阅报告显示为煤科（北京）检测技术有限公司出具的检验报告，但内容中未见主检、审核、批准人签字（章）。依据知识库搜索结果，报告无主检、审核、批准人签字（章）无效。",
                    "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                    "location":{
                        "section":"检验报告正文及附页部分",
                        "source_chunk_id":493,
                        "source_chunk_no":15
                    },
                    "severity":"high",
                    "finding_index":20,
                    "source_chunk_id":493
                },
                {
                    "type":"finding_filtered",
                    "title":"检验报告缺少检验检测专用章",
                    "reason":"finding_quote_missing",
                    "content":"待审阅报告显示为正式检验报告，但内容中未体现加盖“检验检测专用章”。依据知识库搜索结果，检验报告无“检验检测专用章”无效。",
                    "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                    "location":{
                        "section":"检验报告封面或签署页",
                        "source_chunk_id":493,
                        "source_chunk_no":15
                    },
                    "severity":"high",
                    "finding_index":21,
                    "source_chunk_id":493
                }
            ],
            "valid_issue_count":0
        },
        "totalIssues":0,
        "audit_strategy":"chunk_then_merge",
        "partial_success":false,
        "chunk_failed_count":0,
        "chunk_success_count":15,
        "model_usage_summary":{
            "model":"qwen3.5-plus",
            "duration_ms":80729,
            "max_retries":2,
            "parallelism":5,
            "input_tokens":58040,
            "output_tokens":3221,
            "timeout_retries":0,
            "model_call_count":15,
            "chunk_failed_count":0,
            "chunk_success_count":15,
            "ai_audit_timeout_seconds":840
        },
        "validation_warnings":[
            {
                "type":"finding_filtered",
                "title":"委托编号存在比对性错误",
                "reason":"finding_quote_missing",
                "content":"报告页眉及正文中的委托编号'№：2025520398FB'与 LIMS 系统或任务单档案明细表右上角编号不一致，存在比对性错误。根据审核依据，防爆报告编号应严格遵循规则（加 FB），且每一页页眉需与此编号保持一致，当前报告显示编号有误。",
                "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                "location":{
                    "section":"报告页眉及委托编号栏",
                    "source_chunk_id":479,
                    "source_chunk_no":1
                },
                "severity":"high",
                "finding_index":1,
                "source_chunk_id":479
            },
            {
                "type":"finding_filtered",
                "title":"检验结论签发日期未填写",
                "reason":"finding_quote_missing",
                "content":"报告检验结论部分显示“签发日期：年 月 日”，具体日期内容为空，不符合检验报告完整性要求。",
                "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                "location":{
                    "section":"检验结论段落",
                    "source_chunk_id":480,
                    "source_chunk_no":2
                },
                "severity":"high",
                "finding_index":2,
                "source_chunk_id":480
            },
            {
                "type":"finding_filtered",
                "title":"关键人员签字缺失",
                "reason":"finding_quote_missing",
                "content":"报告批准、审核、主检栏目仅显示职务名称，缺少具体人员签字或电子签章，依据注意事项第 3 条，无签字（章）报告无效。",
                "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                "location":{
                    "section":"报告签署栏",
                    "source_chunk_id":480,
                    "source_chunk_no":2
                },
                "severity":"high",
                "finding_index":3,
                "source_chunk_id":480
            },
            {
                "type":"finding_filtered",
                "title":"试验环境条件与产品正常工作环境不一致",
                "reason":"finding_quote_missing",
                "content":"待审阅报告显示试验环境温度为 10℃～30℃，而知识库中该产品说明书规定的正常工作环境温度范围为 0℃～40℃，试验环境覆盖范围不足，未涵盖产品允许工作的最低和最高温度极限。",
                "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                "location":{
                    "section":"一、试验环境地点、条件描述",
                    "source_chunk_id":480,
                    "source_chunk_no":2
                },
                "severity":"medium",
                "finding_index":4,
                "source_chunk_id":480
            },
            {
                "type":"finding_filtered",
                "title":"产品名称与报告内容不一致",
                "reason":"finding_quote_missing",
                "content":"待审阅报告元信息显示产品名称为'本安 - 矿用本安型手机'，但报告正文及知识库依据（KXJ127 说明书、检验报告批注本）均指向'KXJ127 矿用隔爆兼本安型 PLC 控制箱'，产品名称存在严重不符。",
                "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                "location":{
                    "section":"待审阅报告片段元信息 - product_name 字段",
                    "source_chunk_id":481,
                    "source_chunk_no":3
                },
                "severity":"high",
                "finding_index":5,
                "source_chunk_id":481
            },
            {
                "type":"finding_filtered",
                "title":"缺少报告关键签署信息",
                "reason":"finding_quote_missing",
                "content":"待审阅报告片段中未包含主检、审核、批准人签字（章），依据知识库搜索结果中'注意事项'第 3 条规定，无签字（章）的报告无效。",
                "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                "location":{
                    "section":"待审阅报告全文",
                    "source_chunk_id":481,
                    "source_chunk_no":3
                },
                "severity":"high",
                "finding_index":6,
                "source_chunk_id":481
            },
            {
                "type":"finding_filtered",
                "title":"页码连续性异常",
                "reason":"finding_quote_missing",
                "content":"报告片段显示页码从'第 1 页 共 22 页'直接跳转至'第 4 页 共 22 页'，中间缺失第 2、3 页内容，且第 4 页标注'本页内以下空白'后直接接'三、检验结果及结论'，排版逻辑混乱。",
                "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                "location":{
                    "section":"待审阅报告页眉及页面内容",
                    "source_chunk_id":481,
                    "source_chunk_no":3
                },
                "severity":"medium",
                "finding_index":7,
                "source_chunk_id":481
            },
            {
                "type":"finding_filtered",
                "title":"报告章节标题缺失",
                "reason":"finding_quote_missing",
                "content":"待审阅报告中第 12 章仅有章节编号",
                "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                "location":{
                    "section":"7.1.2.4 粘结材料 符合GB/T 3836.4表5要求，排除本条款 N/A",
                    "source_chunk_id":482,
                    "source_chunk_no":4
                },
                "severity":"medium",
                "finding_index":8,
                "source_chunk_id":482
            },
            {
                "type":"finding_filtered",
                "title":"产品名称与审核依据不一致",
                "reason":"finding_content_incomplete",
                "content":"待审阅报告元信息显示产品名称为“本安 - 矿用本安型手机”，而知识库搜索返回的审核依据（产品说明书及检验报告）中明确记载的产品名称为",
                "message":"候选问题描述不完整，无法形成可保存的有效问题",
                "location":{
                    "section":"15.1.2 外部等电位联结 按照 GB/T 3836.4 规定，排除本要求 N/A",
                    "source_chunk_id":483,
                    "source_chunk_no":5
                },
                "severity":"medium",
                "finding_index":9,
                "source_chunk_id":483
            },
            {
                "type":"finding_filtered",
                "title":"产品名称与执行标准不匹配",
                "reason":"finding_quote_missing",
                "content":"待审阅报告元信息显示产品名称为'本安 - 矿用本安型手机'，但知识库搜索结果显示的依据文件（说明书及检验报告）均为'KXJ127 矿用隔爆兼本安型 PLC 控制箱'，且执行标准为 GB/T 3836.1/2/4-2021 等针对控制箱的标准。报告对象产品与审核依据中的产品完全不一致，导致依据无法支撑对该手机的合规性审查。",
                "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                "location":{
                    "section":"17.2.5.3 风扇和风扇罩 按照 GB/T 3836.4 规定，排除本要求 N/A",
                    "source_chunk_id":484,
                    "source_chunk_no":6
                },
                "severity":"high",
                "finding_index":10,
                "source_chunk_id":484
            },
            {
                "type":"finding_filtered",
                "title":"待审阅报告产品名称与知识库依据不一致",
                "reason":"finding_quote_missing",
                "content":"待审阅报告元信息显示产品名称为'本安 - 矿用本安型手机'，而知识库搜索结果中的执行标准、说明书及检验报告均指向'KXJ127 矿用隔爆兼本安型 PLC 控制箱'，两者产品名称及类型完全不符，无法确认报告内容的适用性。",
                "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                "location":{
                    "section":"待审阅报告片段元信息 - product_name",
                    "source_chunk_id":485,
                    "source_chunk_no":7
                },
                "severity":"high",
                "finding_index":11,
                "source_chunk_id":485
            },
            {
                "type":"finding_filtered",
                "title":"防爆标志与产品实际防爆型式不一致",
                "reason":"finding_quote_missing",
                "content":"待审阅报告第 29.4 条款中记录的爆炸性气体环境防爆标志为'Ex ib Ⅰ Mb'，表明产品仅为本质安全型。然而，知识库搜索结果显示该产品（KXJ127）的说明书及分类明确标注其防爆型式为'矿用隔爆兼本质安全型'，对应的防爆标志应为'Ex db [ib Mb]ⅠMb'。报告中的标志遗漏了隔爆部分'db'，与产品实际认证信息及说明书描述不符。",
                "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                "location":{
                    "section":"26.8 耐热试验 符合GB/T 3836.4表5要求，排除本条款 N/A",
                    "source_chunk_id":486,
                    "source_chunk_no":8
                },
                "severity":"high",
                "finding_index":12,
                "source_chunk_id":486
            },
            {
                "type":"finding_filtered",
                "title":"电缆引入装置要求排除依据不足",
                "reason":"finding_quote_missing",
                "content":"待审阅报告中附录 A 多项条款（A.1 至 A.4.2）均标注“按照 GB/T 3836.4 规定，排除本要求”，但知识库搜索结果显示该产品为“矿用隔爆兼本安型 PLC 控制箱”，其结构描述中明确包含",
                "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                "location":{
                    "source_chunk_id":487,
                    "source_chunk_no":9
                },
                "severity":"medium",
                "finding_index":13,
                "source_chunk_id":487
            },
            {
                "type":"finding_filtered",
                "title":"产品型号与名称不一致",
                "reason":"finding_quote_missing",
                "content":"待审阅报告元信息显示产品名称为'本安 - 矿用本安型手机'，但知识库搜索结果显示的说明书及检验报告均针对'KXJ127 矿用隔爆兼本安型 PLC 控制箱'，两者产品名称及类型完全不符。",
                "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                "location":{
                    "section":"待审阅报告片段元信息 - product_name",
                    "source_chunk_id":488,
                    "source_chunk_no":10
                },
                "severity":"high",
                "finding_index":14,
                "source_chunk_id":488
            },
            {
                "type":"finding_filtered",
                "title":"产品名称与审核依据不一致",
                "reason":"finding_quote_missing",
                "content":"待审阅报告元信息中产品名称为'本安 - 矿用本安型手机'，而知识库搜索结果显示的审核依据（产品说明书及检验报告）对应的产品均为'KXJ127 矿用隔爆兼本安型 PLC 控制箱'，两者产品名称及类型完全不符，无法基于当前依据对该手机产品报告进行合规性判定。",
                "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                "location":{
                    "section":"6.3.9 涂层下的间距 电路板涂两遍三防漆处理，涂层下的爬电距离满足要求。 P",
                    "source_chunk_id":489,
                    "source_chunk_no":11
                },
                "severity":"high",
                "finding_index":15,
                "source_chunk_id":489
            },
            {
                "type":"finding_filtered",
                "title":"防爆标志表述不一致",
                "reason":"finding_quote_missing",
                "content":"待审阅报告第 12.4 节中记录的防爆标志为'Ex ib I Mb'，而依据产品说明书（知识库搜索结果）第 2.2 节规定的防爆标志应为'Ex db [ib Mb]ⅠMb'。报告中的标志缺失了隔爆部分'db'及关联符号，与产品实际认证信息不符。",
                "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                "location":{
                    "section":"10.5 电池和电池组试验 1.十只电池试验后吸水纸及试验样品表面无电解液痕迹； 2.电池通过了火花点燃试验； 3.十只电池最高表面温度为：88.4℃。 P",
                    "source_chunk_id":491,
                    "source_chunk_no":13
                },
                "severity":"high",
                "finding_index":16,
                "source_chunk_id":491
            },
            {
                "type":"finding_filtered",
                "title":"检验报告编号不一致",
                "reason":"finding_quote_missing",
                "content":"待审阅报告中出现编号'№：201854833'的页眉，与本报告主编号'№：2025520398FB'及知识库依据中的报告编号不一致，存在引用错误或页面混排。",
                "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                "location":{
                    "section":"第 16 页页眉",
                    "source_chunk_id":492,
                    "source_chunk_no":14
                },
                "severity":"high",
                "finding_index":17,
                "source_chunk_id":492
            },
            {
                "type":"finding_filtered",
                "title":"缺少签字盖章信息",
                "reason":"finding_quote_missing",
                "content":"报告片段中未体现主检、审核、批准人签字（章）及'检验检测专用章'，依据知识库注意事项第 3 条和第 1 条，无签字盖章的报告无效。",
                "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                "location":{
                    "section":"报告签署页/末页",
                    "source_chunk_id":492,
                    "source_chunk_no":14
                },
                "severity":"high",
                "finding_index":18,
                "source_chunk_id":492
            },
            {
                "type":"finding_filtered",
                "title":"受控部件明细表格式错乱",
                "reason":"finding_quote_missing",
                "content":"主要零 (元) 受控部件及重要原材料明细表中，'规格型号’、'生产单位’、'安标编号’等列内容错位（如电池容量'2500mAh'出现在生产单位列前，签发日期出现在安标编号列），且包含批注文字'Comment by 缘鱼曦望’，不符合正式报告规范。",
                "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                "location":{
                    "section":"主要零 (元) 受控部件及重要原材料明细表",
                    "source_chunk_id":492,
                    "source_chunk_no":14
                },
                "severity":"medium",
                "finding_index":19,
                "source_chunk_id":492
            },
            {
                "type":"finding_filtered",
                "title":"检验报告缺少必要签字",
                "reason":"finding_quote_missing",
                "content":"待审阅报告显示为煤科（北京）检测技术有限公司出具的检验报告，但内容中未见主检、审核、批准人签字（章）。依据知识库搜索结果，报告无主检、审核、批准人签字（章）无效。",
                "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                "location":{
                    "section":"检验报告正文及附页部分",
                    "source_chunk_id":493,
                    "source_chunk_no":15
                },
                "severity":"high",
                "finding_index":20,
                "source_chunk_id":493
            },
            {
                "type":"finding_filtered",
                "title":"检验报告缺少检验检测专用章",
                "reason":"finding_quote_missing",
                "content":"待审阅报告显示为正式检验报告，但内容中未体现加盖“检验检测专用章”。依据知识库搜索结果，检验报告无“检验检测专用章”无效。",
                "message":"候选问题缺少报告原文 quote，无法定位问题原文",
                "location":{
                    "section":"检验报告封面或签署页",
                    "source_chunk_id":493,
                    "source_chunk_no":15
                },
                "severity":"high",
                "finding_index":21,
                "source_chunk_id":493
            }
        ],
        "retrieval_used_summary":{
            "reference_count":60,
            "retrieval_count":15,
            "source_chunk_count":15,
            "reference_truncated":false,
            "chunk_reference_usage":[
                {
                    "source_chunk_id":479,
                    "source_chunk_no":1,
                    "references_used_in_prompt":4,
                    "reference_count_before_prompt":4
                },
                {
                    "source_chunk_id":480,
                    "source_chunk_no":2,
                    "references_used_in_prompt":4,
                    "reference_count_before_prompt":4
                },
                {
                    "source_chunk_id":481,
                    "source_chunk_no":3,
                    "references_used_in_prompt":4,
                    "reference_count_before_prompt":4
                },
                {
                    "source_chunk_id":482,
                    "source_chunk_no":4,
                    "references_used_in_prompt":4,
                    "reference_count_before_prompt":4
                },
                {
                    "source_chunk_id":483,
                    "source_chunk_no":5,
                    "references_used_in_prompt":4,
                    "reference_count_before_prompt":4
                },
                {
                    "source_chunk_id":484,
                    "source_chunk_no":6,
                    "references_used_in_prompt":4,
                    "reference_count_before_prompt":4
                },
                {
                    "source_chunk_id":485,
                    "source_chunk_no":7,
                    "references_used_in_prompt":4,
                    "reference_count_before_prompt":4
                },
                {
                    "source_chunk_id":486,
                    "source_chunk_no":8,
                    "references_used_in_prompt":4,
                    "reference_count_before_prompt":4
                },
                {
                    "source_chunk_id":487,
                    "source_chunk_no":9,
                    "references_used_in_prompt":4,
                    "reference_count_before_prompt":4
                },
                {
                    "source_chunk_id":488,
                    "source_chunk_no":10,
                    "references_used_in_prompt":4,
                    "reference_count_before_prompt":4
                },
                {
                    "source_chunk_id":489,
                    "source_chunk_no":11,
                    "references_used_in_prompt":4,
                    "reference_count_before_prompt":4
                },
                {
                    "source_chunk_id":490,
                    "source_chunk_no":12,
                    "references_used_in_prompt":4,
                    "reference_count_before_prompt":4
                },
                {
                    "source_chunk_id":491,
                    "source_chunk_no":13,
                    "references_used_in_prompt":4,
                    "reference_count_before_prompt":4
                },
                {
                    "source_chunk_id":492,
                    "source_chunk_no":14,
                    "references_used_in_prompt":4,
                    "reference_count_before_prompt":4
                },
                {
                    "source_chunk_id":493,
                    "source_chunk_no":15,
                    "references_used_in_prompt":4,
                    "reference_count_before_prompt":4
                }
            ],
            "chunk_model_parallelism":5,
            "chunk_max_reference_chars":12000,
            "chunk_max_reference_count":4,
            "references_used_in_prompt":60,
            "covered_source_chunk_count":15,
            "uncovered_source_chunk_ids":[

            ],
            "reference_selection_strategy":"chunk_then_merge"
        }
    }
}
'''