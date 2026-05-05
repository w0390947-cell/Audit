package com.ruoyi.system.domain.audit.vector;

import java.util.List;

public class EmbeddingRequest
{
    private String model;

    private List<String> input;

    private Integer dimensions;

    public EmbeddingRequest()
    {
    }

    public EmbeddingRequest(String model, List<String> input, Integer dimensions)
    {
        this.model = model;
        this.input = input;
        this.dimensions = dimensions;
    }

    public String getModel()
    {
        return model;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public List<String> getInput()
    {
        return input;
    }

    public void setInput(List<String> input)
    {
        this.input = input;
    }

    public Integer getDimensions()
    {
        return dimensions;
    }

    public void setDimensions(Integer dimensions)
    {
        this.dimensions = dimensions;
    }
}
