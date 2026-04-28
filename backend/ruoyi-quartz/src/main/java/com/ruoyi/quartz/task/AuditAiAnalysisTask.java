package com.ruoyi.quartz.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.system.service.audit.IAuditAiService;

/**
 * AI 审核分析定时任务
 *
 * @author ruoyi
 */
@Component("auditAiAnalysisTask")
public class AuditAiAnalysisTask
{
    @Autowired
    private IAuditAiService auditAiService;

    /**
     * 执行 AI 分析任务
     * 扫描等待中的任务并调用 FastGPT 分析
     */
    public void run()
    {
        auditAiService.runWaitingAiAnalysis("quartz");
    }
}
