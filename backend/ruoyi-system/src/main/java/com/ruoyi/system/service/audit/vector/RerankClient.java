package com.ruoyi.system.service.audit.vector;

import com.ruoyi.system.domain.audit.vector.RerankResult;
import java.util.List;

public interface RerankClient
{
    List<RerankResult> rerank(String query, List<String> documents, int topN);
}
