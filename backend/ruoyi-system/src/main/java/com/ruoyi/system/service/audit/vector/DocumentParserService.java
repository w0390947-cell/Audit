package com.ruoyi.system.service.audit.vector;

import com.ruoyi.system.domain.audit.AuditCommonResource;
import com.ruoyi.system.domain.audit.vector.DocumentParseResult;

public interface DocumentParserService
{
    DocumentParseResult parse(Long resourceId);

    DocumentParseResult parse(AuditCommonResource resource);
}
