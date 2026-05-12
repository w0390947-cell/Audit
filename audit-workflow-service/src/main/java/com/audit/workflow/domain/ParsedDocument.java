package com.audit.workflow.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParsedDocument {

    private String fileName;
    private String fileType;
    private String fullText;
    private String fileHash;
    private String textHash;
    private List<DocumentBlock> blocks = new ArrayList<>();
    private Map<String, Object> metadata = new LinkedHashMap<>();

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getTextHash() {
        return textHash;
    }

    public void setTextHash(String textHash) {
        this.textHash = textHash;
    }

    public List<DocumentBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<DocumentBlock> blocks) {
        this.blocks = blocks;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
