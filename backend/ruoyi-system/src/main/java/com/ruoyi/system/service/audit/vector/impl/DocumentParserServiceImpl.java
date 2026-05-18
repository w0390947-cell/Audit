package com.ruoyi.system.service.audit.vector.impl;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.audit.AuditCommonResource;
import com.ruoyi.system.domain.audit.vector.DocumentParseResult;
import com.ruoyi.system.mapper.audit.AuditLibraryMapper;
import com.ruoyi.system.service.audit.support.AuditPdfConversionSupport;
import com.ruoyi.system.service.audit.vector.DocumentParserService;
import com.ruoyi.system.service.audit.vector.parser.DocumentParser;
import com.ruoyi.system.service.audit.vector.support.AuditFileResolver;

@Service
public class DocumentParserServiceImpl implements DocumentParserService
{
    private static final String UNSUPPORTED_FORMAT = "暂不支持该文件格式";

    private final AuditLibraryMapper auditLibraryMapper;

    private final AuditFileResolver auditFileResolver;

    private final AuditPdfConversionSupport pdfConversionSupport;

    private final List<DocumentParser> parsers;

    public DocumentParserServiceImpl(AuditLibraryMapper auditLibraryMapper, AuditFileResolver auditFileResolver,
            AuditPdfConversionSupport pdfConversionSupport, List<DocumentParser> parsers)
    {
        this.auditLibraryMapper = auditLibraryMapper;
        this.auditFileResolver = auditFileResolver;
        this.pdfConversionSupport = pdfConversionSupport;
        this.parsers = parsers;
    }

    @Override
    public DocumentParseResult parse(Long resourceId)
    {
        AuditCommonResource resource = auditLibraryMapper.selectAuditCommonResourceById(resourceId);
        if (resource == null)
        {
            return DocumentParseResult.failed(resourceId, null, null, "", "文件资源不存在");
        }
        return parse(resource);
    }

    @Override
    public DocumentParseResult parse(AuditCommonResource resource)
    {
        if (resource == null)
        {
            return DocumentParseResult.failed(null, null, null, "", "文件资源不存在");
        }
        String fileName = StringUtils.defaultIfBlank(resource.getFileName(), resource.getDocumentName());
        String fileUrl = resource.getFileUrl();
        DocumentParser parser = selectParser(fileName);
        String fileType = resolveFileType(fileName);
        if (parser == null)
        {
            if (shouldConvertToPdf(fileType))
            {
                return parseConvertedPdf(resource, fileName, fileUrl, fileType);
            }
            return DocumentParseResult.failed(resource.getResourceId(), fileName, fileUrl, fileType, UNSUPPORTED_FORMAT);
        }
        try (InputStream inputStream = auditFileResolver.openFile(fileUrl))
        {
            return parser.parse(resource.getResourceId(), fileName, fileUrl, inputStream);
        }
        catch (Exception e)
        {
            return DocumentParseResult.failed(resource.getResourceId(), fileName, fileUrl, fileType, e.getMessage());
        }
    }

    private DocumentParser selectParser(String fileName)
    {
        for (DocumentParser parser : parsers)
        {
            if (parser.supports(fileName, null))
            {
                return parser;
            }
        }
        return null;
    }

    private DocumentParseResult parseConvertedPdf(AuditCommonResource resource, String fileName, String fileUrl,
            String fileType)
    {
        try
        {
            String previewPdfUrl = pdfConversionSupport.resolvePreviewPdfUrl(fileUrl);
            Path previewPdfPath = pdfConversionSupport.resolveProfilePath(previewPdfUrl);
            DocumentParser pdfParser = selectParser(previewPdfUrl);
            if (pdfParser == null)
            {
                return DocumentParseResult.failed(resource.getResourceId(), fileName, fileUrl, fileType,
                        "PDF解析器不可用");
            }
            try (InputStream inputStream = Files.newInputStream(previewPdfPath))
            {
                return pdfParser.parse(resource.getResourceId(), fileName, previewPdfUrl, inputStream);
            }
        }
        catch (Exception e)
        {
            return DocumentParseResult.failed(resource.getResourceId(), fileName, fileUrl, fileType,
                    AuditPdfConversionSupport.PDF_CONVERT_FAILED_MESSAGE);
        }
    }

    private boolean shouldConvertToPdf(String fileType)
    {
        String normalized = fileType == null ? "" : fileType.toLowerCase(Locale.ROOT);
        return switch (normalized)
        {
            case "xls", "xlsx", "ppt", "pptx", "html", "htm" -> true;
            default -> false;
        };
    }

    private String resolveFileType(String fileName)
    {
        if (StringUtils.isBlank(fileName) || !fileName.contains("."))
        {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}
