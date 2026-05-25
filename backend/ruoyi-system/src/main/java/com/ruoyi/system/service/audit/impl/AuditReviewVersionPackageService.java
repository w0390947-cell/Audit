package com.ruoyi.system.service.audit.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.audit.AuditAiTask;
import com.ruoyi.system.domain.audit.AuditReviewBasisFile;
import com.ruoyi.system.domain.audit.AuditReviewTask;
import com.ruoyi.system.domain.audit.AuditReviewVersion;
import com.ruoyi.system.mapper.audit.AuditAiMapper;
import com.ruoyi.system.mapper.audit.AuditReviewMapper;
import com.ruoyi.system.service.audit.IAuditReviewService;
import com.ruoyi.system.service.audit.support.AuditPdfConversionSupport;

@Service
public class AuditReviewVersionPackageService
{
    private static final String MAIN_REPORT_DIR = "待审核文件/";
    private static final String BASIS_FILE_DIR = "审核依据文件/";
    private static final String AI_RESULT_DIR = "AI审核结果/";

    @Autowired
    private IAuditReviewService auditReviewService;

    @Autowired
    private AuditReviewMapper auditReviewMapper;

    @Autowired
    private AuditAiMapper auditAiMapper;

    @Autowired
    private AuditAiDetectionResultPdfService auditAiDetectionResultPdfService;

    @Autowired
    private AuditPdfConversionSupport auditPdfConversionSupport;

    public PackageFile buildPackage(Long versionId)
    {
        AuditReviewVersion version = auditReviewMapper.selectAuditReviewVersionById(versionId);
        if (version == null)
        {
            throw new ServiceException("审核版本不存在");
        }
        AuditReviewTask task = auditReviewService.selectAuditReviewTaskDetail(version.getTaskId(), versionId);
        if (task == null || task.getCurrentVersion() == null)
        {
            throw new ServiceException("审核版本不存在");
        }
        AuditReviewVersion currentVersion = task.getCurrentVersion();
        byte[] bytes = buildZipBytes(task, currentVersion);
        return new PackageFile(buildPackageFileName(task, currentVersion), bytes);
    }

    private byte[] buildZipBytes(AuditReviewTask task, AuditReviewVersion version)
    {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
                ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8))
        {
            Set<String> entryNames = new HashSet<>();
            addDirectory(zip, MAIN_REPORT_DIR, entryNames);
            addDirectory(zip, BASIS_FILE_DIR, entryNames);
            addDirectory(zip, AI_RESULT_DIR, entryNames);

            addMainReportFiles(zip, entryNames, version);
            addBasisFiles(zip, entryNames, version);
            addAiDetectionResultPdf(zip, entryNames, version);

            zip.finish();
            return output.toByteArray();
        }
        catch (IOException e)
        {
            throw new ServiceException("审核版本资料包生成失败：" + e.getMessage());
        }
    }

    private void addMainReportFiles(ZipOutputStream zip, Set<String> entryNames, AuditReviewVersion version)
            throws IOException
    {
        List<String> urls = splitFileUrlList(version.getMainReportUrls());
        if (urls.isEmpty() && StringUtils.isNotBlank(version.getReportFileUrl()))
        {
            urls.add(version.getReportFileUrl());
        }
        if (urls.isEmpty())
        {
            addTextEntry(zip, MAIN_REPORT_DIR + "说明.txt", "该版本没有待审核文件。", entryNames);
            return;
        }

        List<String> missingMessages = new ArrayList<>();
        int added = 0;
        for (String fileUrl : urls)
        {
            String fileName = resolveMainReportFileName(version, fileUrl);
            if (addProfileFile(zip, entryNames, MAIN_REPORT_DIR, fileUrl, fileName, missingMessages))
            {
                added++;
            }
        }
        if (added == 0)
        {
            missingMessages.add(0, "该版本待审核文件未能写入压缩包。");
        }
        if (!missingMessages.isEmpty())
        {
            addTextEntry(zip, MAIN_REPORT_DIR + "缺失文件说明.txt", String.join(System.lineSeparator(), missingMessages),
                    entryNames);
        }
    }

    private void addBasisFiles(ZipOutputStream zip, Set<String> entryNames, AuditReviewVersion version)
            throws IOException
    {
        List<AuditReviewBasisFile> basisFiles = version.getBasisFileList();
        if (basisFiles == null || basisFiles.isEmpty())
        {
            addTextEntry(zip, BASIS_FILE_DIR + "说明.txt", "该版本没有审核依据文件。", entryNames);
            return;
        }

        List<String> missingMessages = new ArrayList<>();
        int added = 0;
        for (AuditReviewBasisFile basisFile : basisFiles)
        {
            if (basisFile == null || StringUtils.isBlank(basisFile.getFileUrl()))
            {
                continue;
            }
            String fileName = StringUtils.defaultIfBlank(basisFile.getOriginalFilename(),
                    StringUtils.defaultIfBlank(basisFile.getFileName(), extractFileName(basisFile.getFileUrl())));
            if (addProfileFile(zip, entryNames, BASIS_FILE_DIR, basisFile.getFileUrl(), fileName, missingMessages))
            {
                added++;
            }
        }
        if (added == 0)
        {
            missingMessages.add(0, "该版本审核依据文件未能写入压缩包。");
        }
        if (!missingMessages.isEmpty())
        {
            addTextEntry(zip, BASIS_FILE_DIR + "缺失文件说明.txt", String.join(System.lineSeparator(), missingMessages),
                    entryNames);
        }
    }

    private void addAiDetectionResultPdf(ZipOutputStream zip, Set<String> entryNames, AuditReviewVersion version)
            throws IOException
    {
        AuditAiTask aiTask = auditAiMapper.selectAuditAiTaskByReviewVersion(version.getTaskId(), version.getVersionId());
        if (aiTask == null)
        {
            addTextEntry(zip, AI_RESULT_DIR + "说明.txt", "该版本尚未生成 AI 审核任务，暂无检测结果 PDF。", entryNames);
            return;
        }
        try
        {
            byte[] pdfBytes = auditAiDetectionResultPdfService.exportPdf(aiTask.getAiTaskId());
            addBytesEntry(zip, AI_RESULT_DIR + "检测结果.pdf", pdfBytes, entryNames);
        }
        catch (ServiceException e)
        {
            addTextEntry(zip, AI_RESULT_DIR + "说明.txt",
                    "该版本 AI 审核结果 PDF 暂不可下载：" + e.getMessage(), entryNames);
        }
    }

    private boolean addProfileFile(ZipOutputStream zip, Set<String> entryNames, String directory, String fileUrl,
            String fileName, List<String> missingMessages) throws IOException
    {
        try
        {
            Path filePath = auditPdfConversionSupport.resolveProfilePath(fileUrl);
            if (!Files.isRegularFile(filePath))
            {
                missingMessages.add("文件不存在：" + StringUtils.defaultIfBlank(fileName, fileUrl));
                return false;
            }
            String entryName = uniqueEntryName(directory + safeFileName(StringUtils.defaultIfBlank(fileName,
                    filePath.getFileName().toString())), entryNames);
            zip.putNextEntry(new ZipEntry(entryName));
            Files.copy(filePath, zip);
            zip.closeEntry();
            return true;
        }
        catch (ServiceException e)
        {
            missingMessages.add(StringUtils.defaultIfBlank(fileName, fileUrl) + "：" + e.getMessage());
            return false;
        }
    }

    private void addDirectory(ZipOutputStream zip, String directory, Set<String> entryNames) throws IOException
    {
        if (entryNames.add(directory))
        {
            zip.putNextEntry(new ZipEntry(directory));
            zip.closeEntry();
        }
    }

    private void addTextEntry(ZipOutputStream zip, String entryName, String content, Set<String> entryNames)
            throws IOException
    {
        addBytesEntry(zip, entryName, content.getBytes(StandardCharsets.UTF_8), entryNames);
    }

    private void addBytesEntry(ZipOutputStream zip, String entryName, byte[] bytes, Set<String> entryNames)
            throws IOException
    {
        zip.putNextEntry(new ZipEntry(uniqueEntryName(entryName, entryNames)));
        zip.write(bytes);
        zip.closeEntry();
    }

    private String uniqueEntryName(String entryName, Set<String> entryNames)
    {
        String normalized = normalizeEntryName(entryName);
        if (entryNames.add(normalized))
        {
            return normalized;
        }
        int dotIndex = normalized.lastIndexOf('.');
        int slashIndex = normalized.lastIndexOf('/');
        String prefix = dotIndex > slashIndex ? normalized.substring(0, dotIndex) : normalized;
        String suffix = dotIndex > slashIndex ? normalized.substring(dotIndex) : "";
        int index = 2;
        String candidate;
        do
        {
            candidate = prefix + "_" + index + suffix;
            index++;
        }
        while (!entryNames.add(candidate));
        return candidate;
    }

    private String normalizeEntryName(String entryName)
    {
        return entryName.replace('\\', '/').replaceAll("/+", "/");
    }

    private String resolveMainReportFileName(AuditReviewVersion version, String fileUrl)
    {
        if (StringUtils.isNotBlank(version.getReportFileUrl()) && version.getReportFileUrl().equals(fileUrl)
                && StringUtils.isNotBlank(version.getReportFileName()))
        {
            return version.getReportFileName();
        }
        return extractFileName(fileUrl);
    }

    private List<String> splitFileUrlList(String fileUrls)
    {
        Set<String> result = new LinkedHashSet<>();
        if (StringUtils.isBlank(fileUrls))
        {
            return new ArrayList<>();
        }
        for (String fileUrl : fileUrls.split(","))
        {
            if (StringUtils.isNotBlank(fileUrl))
            {
                result.add(fileUrl.trim());
            }
        }
        return new ArrayList<>(result);
    }

    private String extractFileName(String fileUrl)
    {
        if (StringUtils.isBlank(fileUrl))
        {
            return "未命名文件";
        }
        int index = Math.max(fileUrl.lastIndexOf('/'), fileUrl.lastIndexOf('\\'));
        return index > -1 ? fileUrl.substring(index + 1) : fileUrl;
    }

    private String buildPackageFileName(AuditReviewTask task, AuditReviewVersion version)
    {
        String taskNo = task == null ? "" : task.getTaskNo();
        return "审核版本资料包_" + safeFileName(StringUtils.defaultIfBlank(taskNo, "任务"))
                + "_" + safeFileName(StringUtils.defaultIfBlank(version.getVersionNo(), String.valueOf(version.getVersionId())))
                + ".zip";
    }

    private String safeFileName(String fileName)
    {
        if (StringUtils.isBlank(fileName))
        {
            return "未命名文件";
        }
        return fileName.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}\\s]+", "_");
    }

    public static class PackageFile
    {
        private final String fileName;

        private final byte[] bytes;

        public PackageFile(String fileName, byte[] bytes)
        {
            this.fileName = fileName;
            this.bytes = bytes;
        }

        public String getFileName()
        {
            return fileName;
        }

        public byte[] getBytes()
        {
            return bytes;
        }
    }
}
