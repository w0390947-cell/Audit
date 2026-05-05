package com.ruoyi.system.service.audit.vector;

import java.util.List;

public interface EmbeddingClient
{
    List<float[]> embed(List<String> texts);
}
