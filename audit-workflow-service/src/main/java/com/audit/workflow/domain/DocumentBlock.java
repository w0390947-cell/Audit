package com.audit.workflow.domain;

import java.util.LinkedHashMap;
import java.util.Map;

public class DocumentBlock {

    private String text;
    private Integer pageNo;
    private String sectionTitle;
    private String sectionPath;
    private Map<String, Object> metadata = new LinkedHashMap<>();

    public DocumentBlock() {
    }

    public DocumentBlock(String text, Integer pageNo, String sectionTitle, String sectionPath) {
        this.text = text;
        this.pageNo = pageNo;
        this.sectionTitle = sectionTitle;
        this.sectionPath = sectionPath;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

    public String getSectionPath() {
        return sectionPath;
    }

    public void setSectionPath(String sectionPath) {
        this.sectionPath = sectionPath;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
