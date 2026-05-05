package com.ruoyi.system.service.audit.vector.support;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.StringUtils;

@Component
public class AuditFileResolver
{
    private static final String PROFILE_PREFIX = "/profile";

    private static final String ALLOWED_PREFIX = "/upload/";

    public InputStream openFile(String fileUrl) throws IOException
    {
        String relativePath = normalizeRelativePath(fileUrl);
        Path profilePath = Paths.get(RuoYiConfig.getProfile()).toAbsolutePath().normalize();
        Path localPath = profilePath.resolve(relativePath.substring(1)).normalize();
        if (!localPath.startsWith(profilePath))
        {
            throw new IOException("文件路径非法");
        }
        if (Files.isRegularFile(localPath))
        {
            return Files.newInputStream(localPath);
        }

        ClassPathResource resource = new ClassPathResource("profile" + relativePath);
        if (resource.exists())
        {
            return resource.getInputStream();
        }
        throw new IOException("文件不存在或无法访问");
    }

    private String normalizeRelativePath(String fileUrl) throws IOException
    {
        if (StringUtils.isBlank(fileUrl))
        {
            throw new IOException("文件地址为空");
        }
        String normalized = fileUrl.trim().replace('\\', '/');
        if (!normalized.startsWith(PROFILE_PREFIX + ALLOWED_PREFIX))
        {
            throw new IOException("文件路径非法");
        }
        String relativePath = normalized.substring(PROFILE_PREFIX.length());
        Path path = Paths.get(relativePath).normalize();
        String safePath = path.toString().replace('\\', '/');
        if (!safePath.startsWith(ALLOWED_PREFIX) || safePath.contains("../"))
        {
            throw new IOException("文件路径非法");
        }
        return safePath;
    }
}
