package com.audit.workflow.parser;

import com.audit.workflow.domain.DocumentBlock;
import com.audit.workflow.domain.ParsedDocument;
import com.audit.workflow.support.HashSupport;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

@Component
public class TextDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String fileType) {
        String normalized = normalize(fileType);
        return normalized.isBlank()
                || "txt".equals(normalized)
                || "md".equals(normalized)
                || "text".equals(normalized);
    }

    @Override
    public ParsedDocument parse(DocumentParseRequest request) {
        String text = request.getText();
        if (text == null && request.getContent() != null) {
            text = new String(request.getContent(), StandardCharsets.UTF_8);
        }
        text = normalizeText(text);
        ParsedDocument document = baseDocument(request, text);
        Arrays.stream(text.split("\\n\\s*\\n"))
                .map(String::trim)
                .filter(block -> !block.isBlank())
                .forEach(block -> document.getBlocks().add(new DocumentBlock(block, null, detectTitle(block), detectTitle(block))));
        if (document.getBlocks().isEmpty() && !text.isBlank()) {
            document.getBlocks().add(new DocumentBlock(text, null, "", ""));
        }
        return document;
    }

    static ParsedDocument baseDocument(DocumentParseRequest request, String text) {
        ParsedDocument document = new ParsedDocument();
        document.setFileName(request.getFileName());
        document.setFileType(request.getFileType());
        document.setFullText(text == null ? "" : text);
        document.setFileHash(request.getFileHash());
        document.setTextHash(HashSupport.sha256(document.getFullText()));
        if (request.getMetadata() != null) {
            document.getMetadata().putAll(request.getMetadata());
        }
        return document;
    }

    static String normalizeText(String text) {
        return text == null ? "" : text.replace("\r\n", "\n").replace('\r', '\n').trim();
    }

    static String detectTitle(String block) {
        if (block == null) {
            return "";
        }
        String firstLine = block.lines().findFirst().orElse("").trim();
        if (firstLine.length() <= 80 && (firstLine.startsWith("#") || firstLine.matches("^[0-9一二三四五六七八九十]+[、.．].*"))) {
            return firstLine.replaceFirst("^#+\\s*", "");
        }
        return "";
    }

    private String normalize(String fileType) {
        return fileType == null ? "" : fileType.toLowerCase(Locale.ROOT).replace(".", "");
    }
}
