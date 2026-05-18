package com.ruoyi.web.controller.audit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.audit.AuditReviewTask;
import com.ruoyi.system.domain.audit.AuditReviewVersion;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.audit.IAuditReviewService;
import com.ruoyi.system.service.audit.support.AuditBusinessPermissionUtils;

@RestController
@RequestMapping("/audit/review")
public class AuditReviewController extends BaseController
{
    @Autowired
    private IAuditReviewService auditReviewService;

    @Autowired
    private ISysUserService userService;

    @PreAuthorize("@ss.hasPermi('audit:review:list')")
    @GetMapping("/list")
    public TableDataInfo list(AuditReviewTask task)
    {
        startPage();
        List<AuditReviewTask> list = auditReviewService.selectAuditReviewTaskList(task);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('audit:review:query')")
    @GetMapping("/stats")
    public AjaxResult stats()
    {
        return success(auditReviewService.selectAuditReviewStats());
    }

    @PreAuthorize("@ss.hasPermi('audit:review:export')")
    @Log(title = "审核列表管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AuditReviewTask task)
    {
        List<AuditReviewTask> list = auditReviewService.selectAuditReviewTaskList(task);
        ExcelUtil<AuditReviewTask> util = new ExcelUtil<>(AuditReviewTask.class);
        util.exportExcel(response, list, "审核列表数据");
    }

    @PreAuthorize("@ss.hasPermi('audit:review:query')")
    @GetMapping("/{taskId}")
    public AjaxResult getInfo(@PathVariable Long taskId, @RequestParam(required = false) Long versionId)
    {
        return success(auditReviewService.selectAuditReviewTaskDetail(taskId, versionId));
    }

    @PreAuthorize("@ss.hasPermi('audit:review:history')")
    @GetMapping("/version/list/{taskId}")
    public AjaxResult getVersionList(@PathVariable Long taskId)
    {
        List<AuditReviewVersion> versionList = auditReviewService.selectAuditReviewVersionListByTaskId(taskId);
        return success(versionList);
    }

    @PreAuthorize("@ss.hasPermi('audit:review:query')")
    @GetMapping("/operators")
    public AjaxResult getOperators()
    {
        if (AuditBusinessPermissionUtils.isCommonSubmitterOnly())
        {
            Map<String, Object> item = new HashMap<>();
            item.put("userId", getUserId());
            item.put("userName", getUsername());
            item.put("nickName", getLoginUser().getUser().getNickName());
            return success(java.util.Collections.singletonList(item));
        }
        SysUser query = new SysUser();
        query.setStatus("0");
        List<Map<String, Object>> list = userService.selectUserList(query).stream()
                .map(user -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("userId", user.getUserId());
                    item.put("userName", user.getUserName());
                    item.put("nickName", user.getNickName());
                    return item;
                })
                .collect(Collectors.toList());
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('audit:review:add')")
    @Log(title = "审核列表管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AuditReviewTask task)
    {
        task.setCreateBy(getUsername());
        return toAjax(auditReviewService.insertAuditReviewTask(task));
    }

    @PreAuthorize("@ss.hasPermi('audit:review:edit')")
    @Log(title = "审核列表管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AuditReviewTask task)
    {
        task.setUpdateBy(getUsername());
        return toAjax(auditReviewService.updateAuditReviewTask(task));
    }

    @PreAuthorize("@ss.hasPermi('audit:review:changeStatus')")
    @Log(title = "审核列表管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeProcessFlag")
    public AjaxResult changeProcessFlag(@RequestBody AuditReviewTask task)
    {
        return toAjax(auditReviewService.updateProcessFlag(task.getTaskId(), task.getProcessFlag(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('audit:review:remove')")
    @Log(title = "审核列表管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds)
    {
        return toAjax(auditReviewService.deleteAuditReviewTaskByIds(taskIds));
    }
}
