package com.ruoyi.system.domain.audit.vector;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RerankRequest
{
    private String model;

    private String query;

    private List<String> documents;

    @JsonProperty("top_n")
    private Integer topN;

    @JsonProperty("return_documents")
    private Boolean returnDocuments;

    private String instruct;

    public RerankRequest()
    {
    }

    public RerankRequest(String model, String query, List<String> documents, Integer topN,
            Boolean returnDocuments, String instruct)
    {
        this.model = model;
        this.query = query;
        this.documents = documents;
        this.topN = topN;
        this.returnDocuments = returnDocuments;
        this.instruct = instruct;
    }

    public String getModel()
    {
        return model;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }

    public List<String> getDocuments()
    {
        return documents;
    }

    public void setDocuments(List<String> documents)
    {
        this.documents = documents;
    }

    public Integer getTopN()
    {
        return topN;
    }

    public void setTopN(Integer topN)
    {
        this.topN = topN;
    }

    public Boolean getReturnDocuments()
    {
        return returnDocuments;
    }

    public void setReturnDocuments(Boolean returnDocuments)
    {
        this.returnDocuments = returnDocuments;
    }

    public String getInstruct()
    {
        return instruct;
    }

    public void setInstruct(String instruct)
    {
        this.instruct = instruct;
    }
}
