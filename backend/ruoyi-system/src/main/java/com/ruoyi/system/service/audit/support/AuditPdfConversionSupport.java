package com.ruoyi.system.service.audit.support;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Component
public class AuditPdfConversionSupport
{
    public static final String PDF_CONVERT_FAILED_MESSAGE = "文件无法转换为 PDF，请检查文件格式或重新上传 PDF";

    private static final long CONVERT_TIMEOUT_SECONDS = Duration.ofMinutes(2).toSeconds();

    public String resolvePreviewPdfUrl(String sourceFileUrl)
    {
        Path sourcePath = resolveProfilePath(sourceFileUrl);
        if (!Files.exists(sourcePath) || !Files.isRegularFile(sourcePath))
        {
            throw new ServiceException("报告源文件不存在");
        }

        String fileType = FilenameUtils.getExtension(sourcePath.getFileName().toString()).toLowerCase(Locale.ROOT);
        if ("pdf".equals(fileType))
        {
            return toProfileUrl(sourcePath);
        }
        if (isConvertibleToPdf(fileType))
        {
            return convertOfficeToPdf(sourcePath);
        }
        throw new ServiceException(PDF_CONVERT_FAILED_MESSAGE);
    }

    public boolean isConvertibleToPdf(String fileType)
    {
        if (StringUtils.isBlank(fileType))
        {
            return false;
        }
        return switch (fileType.toLowerCase(Locale.ROOT))
        {
            case "doc", "docx", "xls", "xlsx", "ppt", "pptx", "html", "htm", "txt" -> true;
            default -> false;
        };
    }

    public Path resolveProfilePath(String fileUrl)
    {
        String path = extractRequestPath(fileUrl);
        int resourceIndex = path.indexOf(Constants.RESOURCE_PREFIX + "/");
        if (resourceIndex > 0)
        {
            path = path.substring(resourceIndex);
        }
        if (!path.startsWith(Constants.RESOURCE_PREFIX + "/"))
        {
            throw new ServiceException("报告文件地址不是本地资源地址");
        }
        if (path.contains(".."))
        {
            throw new ServiceException("报告文件地址非法");
        }
        Path profilePath = Path.of(RuoYiConfig.getProfile()).toAbsolutePath().normalize();
        String relativePath = path.substring((Constants.RESOURCE_PREFIX + "/").length());
        Path resolvedPath = profilePath.resolve(relativePath).normalize();
        if (!resolvedPath.startsWith(profilePath))
        {
            throw new ServiceException("报告文件地址非法");
        }
        return resolvedPath;
    }

    public String toProfileUrl(Path path)
    {
        Path profilePath = Path.of(RuoYiConfig.getProfile()).toAbsolutePath().normalize();
        Path relativePath = profilePath.relativize(path.toAbsolutePath().normalize());
        return Constants.RESOURCE_PREFIX + "/" + relativePath.toString().replace(File.separatorChar, '/');
    }

    public Integer countPdfPages(Path pdfPath)
    {
        if (pdfPath == null || !Files.exists(pdfPath))
        {
            return null;
        }
        try (PDDocument document = PDDocument.load(pdfPath.toFile()))
        {
            return document.getNumberOfPages();
        }
        catch (IOException e)
        {
            return null;
        }
    }

    private String extractRequestPath(String fileUrl)
    {
        try
        {
            String rawPath;
            if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://"))
            {
                rawPath = URI.create(fileUrl).getRawPath();
            }
            else
            {
                rawPath = fileUrl;
            }
            return URLDecoder.decode(rawPath.replace("+", "%2B"), StandardCharsets.UTF_8);
        }
        catch (IllegalArgumentException e)
        {
            throw new ServiceException("报告文件地址非法");
        }
    }

    private String convertOfficeToPdf(Path sourcePath)
    {
        Path targetPath = buildPreviewPath(sourcePath);
        if (Files.exists(targetPath))
        {
            return toProfileUrl(targetPath);
        }

        try
        {
            Files.createDirectories(targetPath.getParent());
            Path convertedPath = runLibreOfficeConvert(sourcePath, targetPath.getParent());
            Files.move(convertedPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return toProfileUrl(targetPath);
        }
        catch (IOException e)
        {
            throw new ServiceException("报告预览文件生成失败：" + e.getMessage());
        }
    }

    private Path buildPreviewPath(Path sourcePath)
    {
        String baseName = FilenameUtils.getBaseName(sourcePath.getFileName().toString());
        String fingerprint = fingerprint(sourcePath);
        return Path.of(RuoYiConfig.getProfile(), "audit", "preview", fingerprint.substring(0, 2),
                baseName + "_" + fingerprint + ".pdf").toAbsolutePath().normalize();
    }

    private String fingerprint(Path sourcePath)
    {
        try
        {
            String text = sourcePath.toAbsolutePath().normalize() + ":" + Files.getLastModifiedTime(sourcePath).toMillis();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 6; i++)
            {
                builder.append(String.format("%02x", bytes[i]));
            }
            return builder.toString();
        }
        catch (IOException | NoSuchAlgorithmException e)
        {
            return String.valueOf(Math.abs(sourcePath.toAbsolutePath().normalize().toString().hashCode()));
        }
    }

    private Path runLibreOfficeConvert(Path sourcePath, Path outputDir) throws IOException
    {
        IOException lastException = null;
        for (String command : new String[] { "soffice", "libreoffice" })
        {
            try
            {
                Process process = new ProcessBuilder(command, "--headless", "--convert-to", "pdf", "--outdir",
                        outputDir.toString(), sourcePath.toString()).redirectErrorStream(true)
                        .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
                boolean finished = process.waitFor(CONVERT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (!finished)
                {
                    process.destroyForcibly();
                    throw new ServiceException("报告转换超时，请稍后重试");
                }
                if (process.exitValue() != 0)
                {
                    continue;
                }
                Path convertedPath = outputDir.resolve(FilenameUtils.getBaseName(sourcePath.getFileName().toString()) + ".pdf");
                if (Files.exists(convertedPath))
                {
                    return convertedPath;
                }
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                throw new ServiceException("报告转换被中断");
            }
            catch (IOException e)
            {
                lastException = e;
            }
        }
        if (lastException != null)
        {
            throw new ServiceException("未找到可用的LibreOffice/soffice转换工具");
        }
        throw new ServiceException("报告转换失败，请检查源文件是否可正常打开");
    }
}
