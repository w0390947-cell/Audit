package com.audit.workflow.retrieval;

public interface KnowledgeRetrievalClient {

    RetrievalResponse search(RetrievalRequest request);

    BatchRetrievalResponse batchSearch(BatchRetrievalRequest request);
}
