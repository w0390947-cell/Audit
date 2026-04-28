package com.ruoyi.web.controller.audit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.audit.AuditAiStats;
import com.ruoyi.system.domain.audit.AuditAiTask;
import com.ruoyi.system.service.audit.IAuditAiService;

@RestController
@RequestMapping("/audit/ai")
public class AuditAiController extends BaseController
{
    @Autowired
    private IAuditAiService auditAiService;

    @PreAuthorize("@ss.hasPermi('audit:ai:list')")
    @GetMapping("/list")
    public TableDataInfo list(AuditAiTask task)
    {
        startPage();
        List<AuditAiTask> list = auditAiService.selectAuditAiTaskList(task);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('audit:ai:query')")
    @GetMapping("/stats")
    public AjaxResult stats()
    {
        AuditAiStats stats = auditAiService.selectAuditAiStats();
        return success(stats);
    }

    @PreAuthorize("@ss.hasPermi('audit:ai:query')")
    @GetMapping("/submitters")
    public AjaxResult submitters()
    {
        return success(auditAiService.selectSubmitterList());
    }

    @PreAuthorize("@ss.hasPermi('audit:ai:detail')")
    @GetMapping("/{aiTaskId}")
    public AjaxResult getInfo(@PathVariable Long aiTaskId)
    {
        return success(auditAiService.selectAuditAiTaskDetail(aiTaskId));
    }

    @PreAuthorize("@ss.hasPermi('audit:ai:export')")
    @Log(title = "AI任务队列", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AuditAiTask task)
    {
        List<AuditAiTask> list = auditAiService.selectAuditAiTaskList(task);
        ExcelUtil<AuditAiTask> util = new ExcelUtil<>(AuditAiTask.class);
        util.exportExcel(response, list, "AI任务队列数据");
    }

    @PreAuthorize("@ss.hasPermi('audit:ai:changeStatus')")
    @Log(title = "AI任务队列", businessType = BusinessType.UPDATE)
    @PutMapping("/changeTaskStatus")
    public AjaxResult changeTaskStatus(@RequestBody AuditAiTask task)
    {
        return toAjax(auditAiService.updateTaskStatus(task.getAiTaskIds(), task.getTaskStatus(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('audit:ai:raisePriority')")
    @Log(title = "AI任务队列", businessType = BusinessType.UPDATE)
    @PutMapping("/raisePriority")
    public AjaxResult raisePriority(@RequestBody AuditAiTask task)
    {
        return toAjax(auditAiService.raisePriority(task.getAiTaskIds(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('audit:ai:review')")
    @Log(title = "AI任务队列", businessType = BusinessType.UPDATE)
    @PutMapping("/reviewDecision")
    public AjaxResult reviewDecision(@RequestBody AuditAiTask task)
    {
        task.setUpdateBy(getUsername());
        return toAjax(auditAiService.updateReviewDecision(task));
    }

    /**
     * 手动触发 AI 分析
     * 将任务提交到队列或立即执行分析
     */
    @PreAuthorize("@ss.hasPermi('audit:ai:analyze')")
    @Log(title = "AI任务队列", businessType = BusinessType.UPDATE)
    @PostMapping("/{aiTaskId}/analyze")
    public AjaxResult analyze(@PathVariable Long aiTaskId)
    {
        int result = auditAiService.triggerAiAnalysis(aiTaskId, getUsername());
        return toAjax(result);
    }

    @PreAuthorize("@ss.hasPermi('audit:ai:remove')")
    @Log(title = "AI任务队列", businessType = BusinessType.DELETE)
    @DeleteMapping("/{aiTaskIds}")
    public AjaxResult remove(@PathVariable Long[] aiTaskIds)
    {
        return toAjax(auditAiService.deleteAuditAiTaskByIds(aiTaskIds));
    }
}
