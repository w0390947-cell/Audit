package com.ruoyi.system.domain.audit;

import java.util.List;

public class AuditAssetStats
{
    private String yearLabel;

    private Integer totalCount;

    private Integer currentCategoryCount;

    private List<String> monthLabels;

    private List<Integer> yearApprovedData;

    private List<Integer> yearReturnedData;

    private List<Integer> monthApprovedData;

    private List<Integer> monthReturnedData;

    private List<String> pieLabels;

    private List<Integer> pieData;

    public String getYearLabel()
    {
        return yearLabel;
    }

    public void setYearLabel(String yearLabel)
    {
        this.yearLabel = yearLabel;
    }

    public Integer getTotalCount()
    {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount)
    {
        this.totalCount = totalCount;
    }

    public Integer getCurrentCategoryCount()
    {
        return currentCategoryCount;
    }

    public void setCurrentCategoryCount(Integer currentCategoryCount)
    {
        this.currentCategoryCount = currentCategoryCount;
    }

    public List<String> getMonthLabels()
    {
        return monthLabels;
    }

    public void setMonthLabels(List<String> monthLabels)
    {
        this.monthLabels = monthLabels;
    }

    public List<Integer> getYearApprovedData()
    {
        return yearApprovedData;
    }

    public void setYearApprovedData(List<Integer> yearApprovedData)
    {
        this.yearApprovedData = yearApprovedData;
    }

    public List<Integer> getYearReturnedData()
    {
        return yearReturnedData;
    }

    public void setYearReturnedData(List<Integer> yearReturnedData)
    {
        this.yearReturnedData = yearReturnedData;
    }

    public List<Integer> getMonthApprovedData()
    {
        return monthApprovedData;
    }

    public void setMonthApprovedData(List<Integer> monthApprovedData)
    {
        this.monthApprovedData = monthApprovedData;
    }

    public List<Integer> getMonthReturnedData()
    {
        return monthReturnedData;
    }

    public void setMonthReturnedData(List<Integer> monthReturnedData)
    {
        this.monthReturnedData = monthReturnedData;
    }

    public List<String> getPieLabels()
    {
        return pieLabels;
    }

    public void setPieLabels(List<String> pieLabels)
    {
        this.pieLabels = pieLabels;
    }

    public List<Integer> getPieData()
    {
        return pieData;
    }

    public void setPieData(List<Integer> pieData)
    {
        this.pieData = pieData;
    }
}
