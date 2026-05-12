package com.audit.workflow.service;

import com.audit.workflow.common.BusinessException;
import com.audit.workflow.domain.ParsedDocument;
import com.audit.workflow.parser.DocumentParseRequest;
import com.audit.workflow.parser.DocumentParser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentParserService {

    private final List<DocumentParser> parsers;

    public DocumentParserService(List<DocumentParser> parsers) {
        this.parsers = parsers;
    }

    public ParsedDocument parse(AuditInputFetchResult input) {
        DocumentParser parser = parsers.stream()
                .filter(candidate -> candidate.supports(input.getFileType()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("INPUT_FILE_TYPE_UNSUPPORTED", "unsupported file type: " + input.getFileType()));

        DocumentParseRequest request = new DocumentParseRequest();
        request.setFileName(input.getFileName());
        request.setFileType(input.getFileType());
        request.setContent(input.getContent());
        request.setText(input.getText());
        request.setFileHash(input.getFileHash());
        request.setMetadata(input.getMetadata());
        ParsedDocument document = parser.parse(request);
        if (document.getFullText() == null || document.getFullText().isBlank()) {
            throw new BusinessException("PARSE_NO_TEXT", "未识别到可解析文本");
        }
        return document;
    }
}
