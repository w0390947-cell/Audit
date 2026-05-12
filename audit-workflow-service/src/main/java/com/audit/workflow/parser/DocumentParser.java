package com.audit.workflow.parser;

import com.audit.workflow.domain.ParsedDocument;

public interface DocumentParser {

    boolean supports(String fileType);

    ParsedDocument parse(DocumentParseRequest request);
}
