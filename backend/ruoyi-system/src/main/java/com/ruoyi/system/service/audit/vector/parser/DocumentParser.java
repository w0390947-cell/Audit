package com.ruoyi.system.service.audit.vector.parser;

import java.io.InputStream;
import com.ruoyi.system.domain.audit.vector.DocumentParseResult;

public interface DocumentParser
{
    boolean supports(String fileName, String contentType);

    DocumentParseResult parse(Long resourceId, String fileName, String fileUrl, InputStream inputStream);
}
