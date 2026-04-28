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
import com.ruoyi.system.domain.audit.AuditAssetRecord;
import com.ruoyi.system.domain.audit.AuditAssetResubmitRecord;
import com.ruoyi.system.domain.audit.AuditAssetStats;
import com.ruoyi.system.service.audit.IAuditAssetService;

@RestController
@RequestMapping("/audit/asset")
public class AuditAssetController extends BaseController
{
    @Autowired
    private IAuditAssetService auditAssetService;

    @PreAuthorize("@ss.hasPermi('audit:asset:list')")
    @GetMapping("/list")
    public TableDataInfo list(AuditAssetRecord record)
    {
        startPage();
        List<AuditAssetRecord> list = auditAssetService.selectAuditAssetRecordList(record);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('audit:asset:query')")
    @GetMapping("/stats")
    public AjaxResult stats()
    {
        AuditAssetStats stats = auditAssetService.selectAuditAssetStats();
        return success(stats);
    }

    @PreAuthorize("@ss.hasPermi('audit:asset:query')")
    @GetMapping("/reviewers")
    public AjaxResult reviewers()
    {
        return success(auditAssetService.selectReviewerList());
    }

    @PreAuthorize("@ss.hasPermi('audit:asset:detail')")
    @GetMapping("/{assetId}")
    public AjaxResult getInfo(@PathVariable Long assetId)
    {
        return success(auditAssetService.selectAuditAssetRecordDetail(assetId));
    }

    @PreAuthorize("@ss.hasPermi('audit:asset:export')")
    @Log(title = "审核资源列表", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AuditAssetRecord record)
    {
        List<AuditAssetRecord> list = auditAssetService.selectAuditAssetRecordList(record);
        ExcelUtil<AuditAssetRecord> util = new ExcelUtil<>(AuditAssetRecord.class);
        util.exportExcel(response, list, "审核资源列表数据");
    }

    @PreAuthorize("@ss.hasPermi('audit:asset:assign')")
    @Log(title = "审核资源列表", businessType = BusinessType.UPDATE)
    @PutMapping("/assignPermission")
    public AjaxResult assignPermission(@RequestBody AuditAssetRecord record)
    {
        return toAjax(auditAssetService.updatePermissionOwner(record.getAssetId(), record.getPermissionOwner(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('audit:asset:batchDownload')")
    @PostMapping("/batchDownload")
    public AjaxResult batchDownload(@RequestBody AuditAssetRecord record)
    {
        return success(auditAssetService.batchDownload(record.getAssetIds()));
    }

    @PreAuthorize("@ss.hasPermi('audit:asset:batchPackage')")
    @PostMapping("/batchPackage")
    public AjaxResult batchPackage(@RequestBody AuditAssetRecord record)
    {
        return success(auditAssetService.batchPackage(record.getAssetIds()));
    }

    @PreAuthorize("@ss.hasPermi('audit:asset:reupload')")
    @Log(title = "审核资源列表", businessType = BusinessType.UPDATE)
    @PutMapping("/reupload")
    public AjaxResult reupload(@RequestBody AuditAssetResubmitRecord record)
    {
        return toAjax(auditAssetService.reuploadAuditAsset(record, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('audit:asset:remove')")
    @Log(title = "审核资源列表", businessType = BusinessType.DELETE)
    @DeleteMapping("/{assetIds}")
    public AjaxResult remove(@PathVariable Long[] assetIds)
    {
        return toAjax(auditAssetService.deleteAuditAssetRecordByIds(assetIds));
    }
}
