package com.ruoyi.system.service.audit.vector;

import java.util.List;
import com.ruoyi.system.domain.audit.vector.DocumentChunk;
import com.ruoyi.system.domain.audit.vector.DocumentParseResult;

public interface DocumentChunkService
{
    List<DocumentChunk> chunk(DocumentParseResult parseResult);
}
