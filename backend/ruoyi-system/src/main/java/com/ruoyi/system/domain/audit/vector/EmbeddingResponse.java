package com.ruoyi.system.domain.audit.vector;

import java.util.ArrayList;
import java.util.List;

public class EmbeddingResponse
{
    private List<Data> data = new ArrayList<>();

    public List<Data> getData()
    {
        return data;
    }

    public void setData(List<Data> data)
    {
        this.data = data;
    }

    public static class Data
    {
        private Integer index;

        private List<Double> embedding = new ArrayList<>();

        public Integer getIndex()
        {
            return index;
        }

        public void setIndex(Integer index)
        {
            this.index = index;
        }

        public List<Double> getEmbedding()
        {
            return embedding;
        }

        public void setEmbedding(List<Double> embedding)
        {
            this.embedding = embedding;
        }
    }
}
