package com.ruoyi.web.controller.audit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.audit.vector.AuditVectorTask;
import com.ruoyi.system.domain.audit.vector.DocumentChunk;
import com.ruoyi.system.domain.audit.vector.DocumentParseResult;
import com.ruoyi.system.mapper.audit.AuditLibraryMapper;
import com.ruoyi.system.service.audit.vector.AuditVectorLifecycleService;
import com.ruoyi.system.service.audit.vector.AuditVectorTaskService;
import com.ruoyi.system.service.audit.vector.DocumentChunkService;
import com.ruoyi.system.service.audit.vector.DocumentParserService;

@RestController
@RequestMapping("/audit/library/vector/dev")
public class AuditVectorDevController extends BaseController
{
    private final DocumentParserService documentParserService;

    private final DocumentChunkService documentChunkService;

    private final ObjectProvider<AuditVectorTaskService> auditVectorTaskServiceProvider;

    private final ObjectProvider<AuditVectorLifecycleService> auditVectorLifecycleServiceProvider;

    private final AuditLibraryMapper auditLibraryMapper;

    public AuditVectorDevController(DocumentParserService documentParserService,
            DocumentChunkService documentChunkService,
            ObjectProvider<AuditVectorTaskService> auditVectorTaskServiceProvider,
            ObjectProvider<AuditVectorLifecycleService> auditVectorLifecycleServiceProvider,
            AuditLibraryMapper auditLibraryMapper)
    {
        this.documentParserService = documentParserService;
        this.documentChunkService = documentChunkService;
        this.auditVectorTaskServiceProvider = auditVectorTaskServiceProvider;
        this.auditVectorLifecycleServiceProvider = auditVectorLifecycleServiceProvider;
        this.auditLibraryMapper = auditLibraryMapper;
    }

    @PreAuthorize("@ss.hasPermi('audit:library:common:query')")
    @GetMapping("/parse/{resourceId}")
    public AjaxResult parse(@PathVariable Long resourceId)
    {
        DocumentParseResult parseResult = documentParserService.parse(resourceId);
        List<DocumentChunk> chunks = documentChunkService.chunk(parseResult);
        return success(ParseDevResponse.of(parseResult, chunks));
    }

    @PreAuthorize("@ss.hasPermi('audit:library:common:query')")
    @PostMapping("/index/{resourceId}")
    public AjaxResult index(@PathVariable Long resourceId)
    {
        AuditVectorTaskService taskService = taskService();
        if (taskService == null)
        {
            return AjaxResult.error("向量任务服务不可用");
        }
        AuditVectorTask task = taskService.createIndexTask(resourceId, getUsername());
        return task == null ? AjaxResult.error("向量功能未启用或文件不存在") : success(task);
    }

    @PreAuthorize("@ss.hasPermi('audit:library:common:query')")
    @PostMapping("/run/{resourceId}")
    public AjaxResult run(@PathVariable Long resourceId)
    {
        AuditVectorTaskService taskService = taskService();
        if (taskService == null)
        {
            return AjaxResult.error("向量任务服务不可用");
        }
        try
        {
            return success(taskService.runResourceNow(resourceId, getUsername()));
        }
        catch (RuntimeException e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PreAuthorize("@ss.hasPermi('audit:library:common:query')")
    @PostMapping("/reindex/{resourceId}")
    public AjaxResult reindex(@PathVariable Long resourceId)
    {
        AuditVectorTaskService taskService = taskService();
        if (taskService == null)
        {
            return AjaxResult.error("向量任务服务不可用");
        }
        AuditVectorTask task = taskService.createReindexTask(resourceId, getUsername());
        return task == null ? AjaxResult.error("向量功能未启用或文件不存在") : success(task);
    }

    @PreAuthorize("@ss.hasPermi('audit:library:common:query')")
    @PostMapping("/sync-folder/{resourceId}")
    public AjaxResult syncFolder(@PathVariable Long resourceId)
    {
        AuditVectorLifecycleService lifecycleService = lifecycleService();
        if (lifecycleService == null)
        {
            return AjaxResult.error("向量生命周期服务不可用");
        }
        com.ruoyi.system.domain.audit.AuditCommonResource resource =
                auditLibraryMapper.selectAuditCommonResourceById(resourceId);
        if (resource == null)
        {
            return AjaxResult.error("文件不存在");
        }
        lifecycleService.onCommonResourceMoved(resourceId, resource.getFolderId());
        return success();
    }

    @PreAuthorize("@ss.hasPermi('audit:library:common:query')")
    @DeleteMapping("/index/{resourceId}")
    public AjaxResult deleteIndex(@PathVariable Long resourceId)
    {
        AuditVectorLifecycleService lifecycleService = lifecycleService();
        if (lifecycleService == null)
        {
            return AjaxResult.error("向量生命周期服务不可用");
        }
        lifecycleService.onCommonResourcesDeleted(Arrays.asList(resourceId));
        return success();
    }

    @PreAuthorize("@ss.hasPermi('audit:library:common:query')")
    @PostMapping("/bootstrap")
    public AjaxResult bootstrap()
    {
        AuditVectorTaskService taskService = taskService();
        if (taskService == null)
        {
            return AjaxResult.error("向量任务服务不可用");
        }
        return success(taskService.bootstrapCommonResources(getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('audit:library:common:query')")
    @GetMapping("/task/{resourceId}")
    public AjaxResult task(@PathVariable Long resourceId)
    {
        AuditVectorTaskService taskService = taskService();
        if (taskService == null)
        {
            return AjaxResult.error("向量任务服务不可用");
        }
        return success(taskService.selectLatestTask(resourceId));
    }

    private AuditVectorTaskService taskService()
    {
        return auditVectorTaskServiceProvider.getIfAvailable();
    }

    private AuditVectorLifecycleService lifecycleService()
    {
        return auditVectorLifecycleServiceProvider.getIfAvailable();
    }

    public static class ParseDevResponse
    {
        private Long resourceId;

        private String fileName;

        private String fileType;

        private boolean success;

        private boolean textEmpty;

        private String errorMsg;

        private int blockCount;

        private int chunkCount;

        private List<ChunkPreview> chunks = new ArrayList<>();

        public static ParseDevResponse of(DocumentParseResult parseResult, List<DocumentChunk> chunks)
        {
            ParseDevResponse response = new ParseDevResponse();
            response.setResourceId(parseResult.getResourceId());
            response.setFileName(parseResult.getFileName());
            response.setFileType(parseResult.getFileType());
            response.setSuccess(parseResult.isSuccess());
            response.setTextEmpty(parseResult.isTextEmpty());
            response.setErrorMsg(parseResult.getErrorMsg());
            response.setBlockCount(parseResult.getBlocks().size());
            response.setChunkCount(chunks.size());
            List<ChunkPreview> previews = new ArrayList<>();
            for (DocumentChunk chunk : chunks)
            {
                previews.add(ChunkPreview.of(chunk));
            }
            response.setChunks(previews);
            return response;
        }

        public Long getResourceId()
        {
            return resourceId;
        }

        public void setResourceId(Long resourceId)
        {
            this.resourceId = resourceId;
        }

        public String getFileName()
        {
            return fileName;
        }

        public void setFileName(String fileName)
        {
            this.fileName = fileName;
        }

        public String getFileType()
        {
            return fileType;
        }

        public void setFileType(String fileType)
        {
            this.fileType = fileType;
        }

        public boolean isSuccess()
        {
            return success;
        }

        public void setSuccess(boolean success)
        {
            this.success = success;
        }

        public boolean isTextEmpty()
        {
            return textEmpty;
        }

        public void setTextEmpty(boolean textEmpty)
        {
            this.textEmpty = textEmpty;
        }

        public String getErrorMsg()
        {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg)
        {
            this.errorMsg = errorMsg;
        }

        public int getBlockCount()
        {
            return blockCount;
        }

        public void setBlockCount(int blockCount)
        {
            this.blockCount = blockCount;
        }

        public int getChunkCount()
        {
            return chunkCount;
        }

        public void setChunkCount(int chunkCount)
        {
            this.chunkCount = chunkCount;
        }

        public List<ChunkPreview> getChunks()
        {
            return chunks;
        }

        public void setChunks(List<ChunkPreview> chunks)
        {
            this.chunks = chunks;
        }
    }

    public static class ChunkPreview
    {
        private Integer chunkNo;

        private Integer pageNo;

        private String sectionTitle;

        private Integer tokenCount;

        private String chunkText;

        public static ChunkPreview of(DocumentChunk chunk)
        {
            ChunkPreview preview = new ChunkPreview();
            preview.setChunkNo(chunk.getChunkNo());
            preview.setPageNo(chunk.getPageNo());
            preview.setSectionTitle(chunk.getSectionTitle());
            preview.setTokenCount(chunk.getTokenCount());
            preview.setChunkText(preview(chunk.getChunkText()));
            return preview;
        }

        private static String preview(String text)
        {
            if (text == null || text.length() <= 300)
            {
                return text;
            }
            return text.substring(0, 300);
        }

        public Integer getChunkNo()
        {
            return chunkNo;
        }

        public void setChunkNo(Integer chunkNo)
        {
            this.chunkNo = chunkNo;
        }

        public Integer getPageNo()
        {
            return pageNo;
        }

        public void setPageNo(Integer pageNo)
        {
            this.pageNo = pageNo;
        }

        public String getSectionTitle()
        {
            return sectionTitle;
        }

        public void setSectionTitle(String sectionTitle)
        {
            this.sectionTitle = sectionTitle;
        }

        public Integer getTokenCount()
        {
            return tokenCount;
        }

        public void setTokenCount(Integer tokenCount)
        {
            this.tokenCount = tokenCount;
        }

        public String getChunkText()
        {
            return chunkText;
        }

        public void setChunkText(String chunkText)
        {
            this.chunkText = chunkText;
        }
    }
}
