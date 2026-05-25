package com.ruoyi.web.controller.audit;

import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.audit.AuditCommonResource;
import com.ruoyi.system.domain.audit.AuditLibraryFolder;
import com.ruoyi.system.domain.audit.AuditTaskResource;
import com.ruoyi.system.service.audit.IAuditLibraryService;

@RestController
@RequestMapping("/audit/library")
public class AuditLibraryController extends BaseController
{
    @Autowired
    private IAuditLibraryService auditLibraryService;

    @PreAuthorize("@ss.hasPermi('audit:library:folder:list')")
    @GetMapping("/folder/list")
    public AjaxResult folderList(AuditLibraryFolder folder)
    {
        return success(auditLibraryService.selectAuditLibraryFolderList(folder));
    }

    @PreAuthorize("@ss.hasAnyPermi('audit:library:folder:query,audit:library:common:query,audit:library:task:query')")
    @GetMapping("/folder/options")
    public AjaxResult folderOptions(AuditLibraryFolder folder)
    {
        return success(auditLibraryService.selectAuditLibraryFolderList(folder));
    }

    @PreAuthorize("@ss.hasAnyPermi('audit:library:folder:add,audit:library:common:add')")
    @Log(title = "审核文件库", businessType = BusinessType.INSERT)
    @PostMapping("/folder")
    public AjaxResult addFolder(@Validated @RequestBody AuditLibraryFolder folder)
    {
        folder.setCreateBy(getUsername());
        folder.setUpdateBy(getUsername());
        return toAjax(auditLibraryService.insertAuditLibraryFolder(folder));
    }

    @PreAuthorize("@ss.hasAnyPermi('audit:library:folder:edit,audit:library:common:edit,audit:library:common:assignFolder')")
    @Log(title = "审核文件库", businessType = BusinessType.UPDATE)
    @PutMapping("/folder")
    public AjaxResult editFolder(@Validated @RequestBody AuditLibraryFolder folder)
    {
        folder.setUpdateBy(getUsername());
        return toAjax(auditLibraryService.updateAuditLibraryFolder(folder));
    }

    @PreAuthorize("@ss.hasAnyPermi('audit:library:folder:remove,audit:library:common:remove')")
    @Log(title = "审核文件库", businessType = BusinessType.DELETE)
    @DeleteMapping("/folder/{folderIds}")
    public AjaxResult removeFolder(@PathVariable Long[] folderIds)
    {
        return toAjax(auditLibraryService.deleteAuditLibraryFolderByIds(folderIds));
    }

    @PreAuthorize("@ss.hasPermi('audit:library:common:list')")
    @GetMapping("/common/list")
    public TableDataInfo commonList(AuditCommonResource resource)
    {
        startPage();
        List<AuditCommonResource> list = auditLibraryService.selectAuditCommonResourceList(resource);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('audit:library:common:query')")
    @GetMapping("/common/{resourceId}")
    public AjaxResult getCommonInfo(@PathVariable Long resourceId)
    {
        return success(auditLibraryService.selectAuditCommonResourceDetail(resourceId));
    }

    @PreAuthorize("@ss.hasPermi('audit:library:common:add')")
    @Log(title = "常用文件资源", businessType = BusinessType.INSERT)
    @PostMapping("/common")
    public AjaxResult addCommon(@Validated @RequestBody AuditCommonResource resource)
    {
        resource.setCreateBy(getUsername());
        resource.setUpdateBy(getUsername());
        return toAjax(auditLibraryService.insertAuditCommonResource(resource));
    }

    @PreAuthorize("@ss.hasPermi('audit:library:common:edit')")
    @Log(title = "常用文件资源", businessType = BusinessType.UPDATE)
    @PutMapping("/common")
    public AjaxResult editCommon(@Validated @RequestBody AuditCommonResource resource)
    {
        resource.setUpdateBy(getUsername());
        return toAjax(auditLibraryService.updateAuditCommonResource(resource));
    }

    @PreAuthorize("@ss.hasPermi('audit:library:common:assignFolder')")
    @Log(title = "常用文件资源", businessType = BusinessType.UPDATE)
    @PutMapping("/common/assignFolder")
    public AjaxResult assignCommonFolder(@RequestBody AuditCommonResource resource)
    {
        resource.setUpdateBy(getUsername());
        return toAjax(auditLibraryService.assignAuditCommonResourceFolder(resource));
    }

    @PreAuthorize("@ss.hasPermi('audit:library:task:edit')")
    @Log(title = "任务文件资源", businessType = BusinessType.UPDATE)
    @PutMapping("/task/assignFolder")
    public AjaxResult assignTaskFolder(@RequestBody AuditTaskResource resource)
    {
        resource.setUpdateBy(getUsername());
        return toAjax(auditLibraryService.assignAuditTaskResourceFolder(resource));
    }

    @PreAuthorize("@ss.hasPermi('audit:library:common:remove')")
    @Log(title = "常用文件资源", businessType = BusinessType.DELETE)
    @DeleteMapping("/common/{resourceIds}")
    public AjaxResult removeCommon(@PathVariable Long[] resourceIds)
    {
        return toAjax(auditLibraryService.deleteAuditCommonResourceByIds(resourceIds));
    }

    @PreAuthorize("@ss.hasPermi('audit:library:common:export')")
    @Log(title = "常用文件资源", businessType = BusinessType.EXPORT)
    @PostMapping("/common/export")
    public void exportCommon(HttpServletResponse response, AuditCommonResource resource)
    {
        List<AuditCommonResource> list = auditLibraryService.selectAuditCommonResourceList(resource);
        ExcelUtil<AuditCommonResource> util = new ExcelUtil<>(AuditCommonResource.class);
        util.exportExcel(response, list, "常用文件资源数据");
    }

    @PreAuthorize("@ss.hasPermi('audit:library:task:list')")
    @GetMapping("/task/common/list")
    public TableDataInfo taskCommonList(AuditCommonResource resource)
    {
        startPage();
        List<AuditCommonResource> list = auditLibraryService.selectAuditTaskCommonResourceList(resource);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('audit:library:task:query')")
    @GetMapping("/task/common/{resourceId}")
    public AjaxResult getTaskCommonInfo(@PathVariable Long resourceId)
    {
        return success(auditLibraryService.selectAuditTaskCommonResourceDetail(resourceId));
    }

    @PreAuthorize("@ss.hasPermi('audit:library:task:edit')")
    @Log(title = "任务文件资源", businessType = BusinessType.UPDATE)
    @PutMapping("/task/common/assignFolder")
    public AjaxResult assignTaskCommonFolder(@RequestBody AuditCommonResource resource)
    {
        resource.setUpdateBy(getUsername());
        return toAjax(auditLibraryService.assignAuditTaskCommonResourceFolder(resource));
    }

    @PreAuthorize("@ss.hasPermi('audit:library:task:remove')")
    @Log(title = "任务文件资源", businessType = BusinessType.DELETE)
    @DeleteMapping("/task/common/{resourceIds}")
    public AjaxResult removeTaskCommon(@PathVariable Long[] resourceIds)
    {
        return toAjax(auditLibraryService.deleteAuditTaskCommonResourceByIds(resourceIds));
    }

    @PreAuthorize("@ss.hasPermi('audit:library:task:list')")
    @GetMapping("/task/list")
    public TableDataInfo taskList(AuditTaskResource resource)
    {
        startPage();
        List<AuditTaskResource> list = auditLibraryService.selectAuditTaskResourceList(resource);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('audit:library:task:edit')")
    @Log(title = "任务文件资源", businessType = BusinessType.UPDATE)
    @PutMapping("/task/reupload")
    public AjaxResult reuploadTask(@RequestBody AuditTaskResource resource)
    {
        resource.setUpdateBy(getUsername());
        return toAjax(auditLibraryService.reuploadAuditTaskResource(resource));
    }

    @PreAuthorize("@ss.hasPermi('audit:library:task:remove')")
    @Log(title = "任务文件资源", businessType = BusinessType.DELETE)
    @DeleteMapping("/task/{resourceIds}")
    public AjaxResult removeTask(@PathVariable Long[] resourceIds)
    {
        return toAjax(auditLibraryService.deleteAuditTaskResourceByIds(resourceIds));
    }

    @PreAuthorize("@ss.hasPermi('audit:library:task:export')")
    @Log(title = "任务文件资源", businessType = BusinessType.EXPORT)
    @PostMapping("/task/export")
    public void exportTask(HttpServletResponse response, AuditTaskResource resource)
    {
        List<AuditTaskResource> list = auditLibraryService.selectAuditTaskResourceList(resource);
        ExcelUtil<AuditTaskResource> util = new ExcelUtil<>(AuditTaskResource.class);
        util.exportExcel(response, list, "任务文件资源数据");
    }
}
