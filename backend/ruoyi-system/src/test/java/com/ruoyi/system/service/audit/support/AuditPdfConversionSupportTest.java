package com.ruoyi.system.service.audit.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.ruoyi.common.config.RuoYiConfig;

class AuditPdfConversionSupportTest
{
    @TempDir
    Path tempDir;

    private final AuditPdfConversionSupport support = new AuditPdfConversionSupport();

    @Test
    void resolveProfilePathKeepsPlusInFileName()
    {
        new RuoYiConfig().setProfile(tempDir.toString());

        Path path = support.resolveProfilePath(
                "/profile/upload/2026/05/25/2025521166+技术审查报告_20260525191559A011.pdf");

        assertThat(path).isEqualTo(tempDir.resolve(
                "upload/2026/05/25/2025521166+技术审查报告_20260525191559A011.pdf").toAbsolutePath().normalize());
    }

    @Test
    void resolveProfilePathDecodesPercentEncodedSpace()
    {
        new RuoYiConfig().setProfile(tempDir.toString());

        Path path = support.resolveProfilePath("/profile/upload/2026/05/25/带%20空格%20的报告.pdf");

        assertThat(path).isEqualTo(tempDir.resolve(
                "upload/2026/05/25/带 空格 的报告.pdf").toAbsolutePath().normalize());
    }
}
