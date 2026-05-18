package com.ruoyi.system.service.audit.impl;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.audit.AuditReviewBasisFile;
import com.ruoyi.system.domain.audit.AuditReviewTask;
import com.ruoyi.system.domain.audit.AuditReviewVersion;
import com.ruoyi.system.mapper.audit.AuditReviewMapper;
import com.ruoyi.system.service.audit.support.AuditPdfConversionSupport;

@Service
public class AuditAiEstimatedDurationService
{
    private static final Logger log = LoggerFactory.getLogger(AuditAiEstimatedDurationService.class);

    private static final int BASE_SECONDS = 60;

    private static final int SECONDS_PER_PAGE = 8;

    private static final int SECONDS_PER_UPLOADED_BASIS_FILE = 30;

    private static final int SECONDS_PER_LIBRARY_BASIS_FILE = 5;

    private static final int NON_PDF_CONVERT_SECONDS = 30;

    private static final float DURATION_SCALE = 0.5F;

    private static final int MIN_SECONDS = 60;

    private static final int MAX_SECONDS = 900;

    private static final int DEFAULT_PAGE_COUNT = 15;

    private final AuditPdfConversionSupport pdfConversionSupport;

    private final AuditReviewMapper auditReviewMapper;

    public AuditAiEstimatedDurationService(AuditPdfConversionSupport pdfConversionSupport,
            AuditReviewMapper auditReviewMapper)
    {
        this.pdfConversionSupport = pdfConversionSupport;
        this.auditReviewMapper = auditReviewMapper;
    }

    public String estimate(AuditReviewTask reviewTask, AuditReviewVersion reviewVersion, String reportFileUrl,
            String reportFileName)
    {
        int pageCount = estimatePageCount(reportFileUrl);
        int uploadedBasisCount = 0;
        int libraryBasisCount = 0;
        List<AuditReviewBasisFile> basisFiles = basisFiles(reviewTask, reviewVersion);
        if (basisFiles.isEmpty())
        {
            uploadedBasisCount = splitCount(reviewVersion == null ? null : reviewVersion.getBasisFileUrls());
            if (uploadedBasisCount <= 0 && reviewTask != null)
            {
                uploadedBasisCount = splitCount(reviewTask.getBasisFileUrls());
            }
        }
        else
        {
            for (AuditReviewBasisFile file : basisFiles)
            {
                if (file == null)
                {
                    continue;
                }
                if (AuditReviewBasisFile.SOURCE_LIBRARY.equals(file.getSourceType()))
                {
                    libraryBasisCount++;
                }
                else
                {
                    uploadedBasisCount++;
                }
            }
        }

        int seconds = BASE_SECONDS + pageCount * SECONDS_PER_PAGE
                + uploadedBasisCount * SECONDS_PER_UPLOADED_BASIS_FILE
                + libraryBasisCount * SECONDS_PER_LIBRARY_BASIS_FILE;
        if (isNonPdf(reportFileName, reportFileUrl))
        {
            seconds += NON_PDF_CONVERT_SECONDS;
        }
        seconds = Math.round(seconds * DURATION_SCALE);
        seconds = Math.max(MIN_SECONDS, Math.min(MAX_SECONDS, seconds));
        int minutes = Math.max(1, Math.round(seconds / 60.0F));
        return "约 " + minutes + " 分钟";
    }

    private int estimatePageCount(String reportFileUrl)
    {
        if (StringUtils.isBlank(reportFileUrl))
        {
            return DEFAULT_PAGE_COUNT;
        }
        try
        {
            String previewPdfUrl = pdfConversionSupport.resolvePreviewPdfUrl(reportFileUrl);
            Path previewPdfPath = pdfConversionSupport.resolveProfilePath(previewPdfUrl);
            Integer pageCount = pdfConversionSupport.countPdfPages(previewPdfPath);
            return pageCount == null || pageCount <= 0 ? DEFAULT_PAGE_COUNT : pageCount;
        }
        catch (Exception e)
        {
            log.warn("Failed to estimate AI task page count, fileUrl={}", reportFileUrl, e);
            return DEFAULT_PAGE_COUNT;
        }
    }

    private List<AuditReviewBasisFile> basisFiles(AuditReviewTask reviewTask, AuditReviewVersion reviewVersion)
    {
        if (reviewVersion != null && reviewVersion.getBasisFileList() != null
                && !reviewVersion.getBasisFileList().isEmpty())
        {
            return reviewVersion.getBasisFileList();
        }
        if (reviewTask != null && reviewTask.getBasisFileList() != null && !reviewTask.getBasisFileList().isEmpty())
        {
            return reviewTask.getBasisFileList();
        }
        if (reviewVersion != null && reviewVersion.getVersionId() != null)
        {
            List<AuditReviewBasisFile> list =
                    auditReviewMapper.selectAuditReviewBasisFileListByVersionId(reviewVersion.getVersionId());
            return list == null ? Collections.emptyList() : list;
        }
        if (reviewTask != null && reviewTask.getTaskId() != null)
        {
            List<AuditReviewBasisFile> list =
                    auditReviewMapper.selectAuditReviewBasisFileListByTaskId(reviewTask.getTaskId());
            return list == null ? Collections.emptyList() : list;
        }
        return Collections.emptyList();
    }

    private boolean isNonPdf(String fileName, String fileUrl)
    {
        String extension = extensionOf(StringUtils.defaultIfBlank(fileName, fileUrl));
        return StringUtils.isNotBlank(extension) && !"pdf".equals(extension);
    }

    private String extensionOf(String value)
    {
        if (StringUtils.isBlank(value))
        {
            return "";
        }
        String normalized = value;
        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0)
        {
            normalized = normalized.substring(0, queryIndex);
        }
        return FilenameUtils.getExtension(normalized).toLowerCase(Locale.ROOT);
    }

    private int splitCount(String fileUrls)
    {
        if (StringUtils.isBlank(fileUrls))
        {
            return 0;
        }
        int count = 0;
        for (String part : fileUrls.split(","))
        {
            if (StringUtils.isNotBlank(part))
            {
                count++;
            }
        }
        return count;
    }
}
