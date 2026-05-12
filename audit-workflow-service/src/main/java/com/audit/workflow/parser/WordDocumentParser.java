package com.audit.workflow.parser;

import com.audit.workflow.common.BusinessException;
import com.audit.workflow.domain.DocumentBlock;
import com.audit.workflow.domain.ParsedDocument;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Locale;

@Component
public class WordDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String fileType) {
        String normalized = normalize(fileType);
        return "doc".equals(normalized) || "docx".equals(normalized);
    }

    @Override
    public ParsedDocument parse(DocumentParseRequest request) {
        String type = normalize(request.getFileType());
        String text;
        if ("docx".equals(type)) {
            text = parseDocx(request.getContent());
        } else {
            text = parseDoc(request.getContent());
        }
        text = TextDocumentParser.normalizeText(text);
        if (text.isBlank()) {
            throw new BusinessException("PARSE_NO_TEXT", "未识别到可解析文本");
        }
        ParsedDocument document = TextDocumentParser.baseDocument(request, text);
        document.getMetadata().putIfAbsent("page_location_source", "unsupported_word_pagination");
        document.getMetadata().put("page_location_support", false);
        Arrays.stream(text.split("\\n\\s*\\n|\\n"))
                .map(String::trim)
                .filter(block -> !block.isBlank())
                .forEach(block -> document.getBlocks().add(new DocumentBlock(block, null, TextDocumentParser.detectTitle(block), TextDocumentParser.detectTitle(block))));
        if (document.getBlocks().isEmpty()) {
            document.getBlocks().add(new DocumentBlock(text, null, "", ""));
        }
        return document;
    }

    private String parseDocx(byte[] content) {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(content));
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        } catch (Exception ex) {
            throw new BusinessException("PARSE_FAILED", "docx parse failed: " + ex.getMessage());
        }
    }

    private String parseDoc(byte[] content) {
        try (HWPFDocument document = new HWPFDocument(new ByteArrayInputStream(content));
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        } catch (Exception ex) {
            throw new BusinessException("PARSE_FAILED", "doc parse failed: " + ex.getMessage());
        }
    }

    private String normalize(String fileType) {
        return fileType == null ? "" : fileType.toLowerCase(Locale.ROOT).replace(".", "");
    }
}
