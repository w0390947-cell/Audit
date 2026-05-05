package com.ruoyi.system.domain.audit.vector;

import java.util.ArrayList;
import java.util.List;

public class DocumentParseResult
{
    public static final String TEXT_EMPTY_ERROR = "未识别到文本，需 OCR";

    private Long resourceId;

    private String fileName;

    private String fileUrl;

    private String fileType;

    private boolean success;

    private boolean textEmpty;

    private String errorMsg;

    private List<DocumentTextBlock> blocks = new ArrayList<>();

    public static DocumentParseResult success(Long resourceId, String fileName, String fileUrl, String fileType,
            List<DocumentTextBlock> blocks)
    {
        DocumentParseResult result = base(resourceId, fileName, fileUrl, fileType);
        result.setSuccess(true);
        result.setBlocks(blocks);
        return result;
    }

    public static DocumentParseResult textEmpty(Long resourceId, String fileName, String fileUrl, String fileType)
    {
        DocumentParseResult result = base(resourceId, fileName, fileUrl, fileType);
        result.setSuccess(false);
        result.setTextEmpty(true);
        result.setErrorMsg(TEXT_EMPTY_ERROR);
        return result;
    }

    public static DocumentParseResult failed(Long resourceId, String fileName, String fileUrl, String fileType, String errorMsg)
    {
        DocumentParseResult result = base(resourceId, fileName, fileUrl, fileType);
        result.setSuccess(false);
        result.setErrorMsg(errorMsg);
        return result;
    }

    private static DocumentParseResult base(Long resourceId, String fileName, String fileUrl, String fileType)
    {
        DocumentParseResult result = new DocumentParseResult();
        result.setResourceId(resourceId);
        result.setFileName(fileName);
        result.setFileUrl(fileUrl);
        result.setFileType(fileType);
        return result;
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

    public String getFileUrl()
    {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl)
    {
        this.fileUrl = fileUrl;
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

    public List<DocumentTextBlock> getBlocks()
    {
        return blocks;
    }

    public void setBlocks(List<DocumentTextBlock> blocks)
    {
        this.blocks = blocks == null ? new ArrayList<>() : blocks;
    }
}
