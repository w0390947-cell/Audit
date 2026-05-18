package com.audit.workflow.service;

import com.audit.workflow.domain.ContentChunk;
import com.audit.workflow.domain.DocumentBlock;
import com.audit.workflow.domain.ParsedDocument;
import com.audit.workflow.support.HashSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class TextChunkService {

    private final int minChars;
    private final int maxChars;
    private final int overlapChars;

    public TextChunkService(@Value("${audit.chunk.min-chars:200}") int minChars,
                            @Value("${audit.chunk.max-chars:1200}") int maxChars,
                            @Value("${audit.chunk.overlap-chars:150}") int overlapChars) {
        this.minChars = minChars;
        this.maxChars = maxChars;
        this.overlapChars = overlapChars;
    }

    public List<ContentChunk> split(ParsedDocument document) {
        List<ContentChunk> chunks = new ArrayList<>();
        StringBuilder pending = new StringBuilder();
        Integer pendingPageNo = null;
        String pendingSectionTitle = "";
        String pendingSectionPath = "";

        List<DocumentBlock> blocks = document.getBlocks();
        if (blocks == null || blocks.isEmpty()) {
            blocks = List.of(new DocumentBlock(document.getFullText(), null, "", ""));
        }

        for (DocumentBlock block : blocks) {
            String text = normalize(block.getText());
            if (text.isBlank()) {
                continue;
            }
            Integer blockPageNo = block.getPageNo();
            if (pending.length() > 0 && isPageBoundary(pendingPageNo, blockPageNo)) {
                addChunk(chunks, pending.toString(), pendingPageNo, pendingSectionTitle, pendingSectionPath);
                pending = new StringBuilder();
                pendingPageNo = null;
                pendingSectionTitle = "";
                pendingSectionPath = "";
            }
            if (pending.length() == 0) {
                pendingPageNo = blockPageNo;
                pendingSectionTitle = value(block.getSectionTitle());
                pendingSectionPath = value(block.getSectionPath());
            }
            if (pending.length() + text.length() + 1 <= maxChars) {
                append(pending, text);
                continue;
            }
            if (pending.length() >= minChars) {
                addChunk(chunks, pending.toString(), pendingPageNo, pendingSectionTitle, pendingSectionPath);
                pending = new StringBuilder(overlap(pending.toString()));
            }
            if (text.length() > maxChars) {
                pending = splitLongText(chunks, text, blockPageNo, value(block.getSectionTitle()), value(block.getSectionPath()), pending);
            } else {
                append(pending, text);
                pendingPageNo = blockPageNo;
                pendingSectionTitle = value(block.getSectionTitle());
                pendingSectionPath = value(block.getSectionPath());
            }
        }

        if (pending.length() > 0) {
            addChunk(chunks, pending.toString(), pendingPageNo, pendingSectionTitle, pendingSectionPath);
        }

        for (int i = 0; i < chunks.size(); i++) {
            chunks.get(i).setChunkNo(i + 1);
        }
        return chunks;
    }

    private boolean isPageBoundary(Integer pendingPageNo, Integer blockPageNo) {
        if (pendingPageNo == null && blockPageNo == null) {
            return false;
        }
        return !Objects.equals(pendingPageNo, blockPageNo);
    }

    private StringBuilder splitLongText(List<ContentChunk> chunks, String text, Integer pageNo, String sectionTitle,
                                        String sectionPath, StringBuilder pending) {
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxChars, text.length());
            addChunk(chunks, text.substring(start, end), pageNo, sectionTitle, sectionPath);
            if (end == text.length()) {
                break;
            }
            start = Math.max(end - overlapChars, start + 1);
        }
        return new StringBuilder();
    }

    private void addChunk(List<ContentChunk> chunks, String text, Integer pageNo, String sectionTitle, String sectionPath) {
        String normalized = normalize(text);
        if (normalized.isBlank()) {
            return;
        }
        ContentChunk chunk = new ContentChunk();
        chunk.setChunkText(normalized);
        chunk.setPageNo(pageNo);
        chunk.setSectionTitle(value(sectionTitle));
        chunk.setSectionPath(value(sectionPath));
        chunk.setCharCount(normalized.length());
        chunk.setTokenCount(estimateTokens(normalized));
        chunk.setContentHash(HashSupport.sha256(normalized));
        chunks.add(chunk);
    }

    private int estimateTokens(String text) {
        return Math.max(1, text.length() / 2);
    }

    private void append(StringBuilder builder, String text) {
        if (builder.length() > 0) {
            builder.append("\n\n");
        }
        builder.append(text);
    }

    private String overlap(String text) {
        if (overlapChars <= 0 || text.length() <= overlapChars) {
            return "";
        }
        return text.substring(text.length() - overlapChars);
    }

    private String normalize(String text) {
        return text == null ? "" : text.replace("\r\n", "\n").replace('\r', '\n').trim();
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
