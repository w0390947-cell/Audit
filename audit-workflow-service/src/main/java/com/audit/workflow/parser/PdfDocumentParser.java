package com.audit.workflow.parser;

import com.audit.workflow.common.BusinessException;
import com.audit.workflow.domain.DocumentBlock;
import com.audit.workflow.domain.ParsedDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.Locale;

@Component
public class PdfDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String fileType) {
        return "pdf".equals(normalize(fileType));
    }

    @Override
    public ParsedDocument parse(DocumentParseRequest request) {
        if (request.getContent() == null || request.getContent().length == 0) {
            throw new BusinessException("PARSE_FAILED", "pdf content is empty");
        }
        try (PDDocument pdDocument = PDDocument.load(new ByteArrayInputStream(request.getContent()))) {
            PDFTextStripper fullStripper = new PDFTextStripper();
            String fullText = TextDocumentParser.normalizeText(fullStripper.getText(pdDocument));
            if (fullText.isBlank()) {
                throw new BusinessException("PARSE_NO_TEXT", "未识别到可解析文本，可能需要 OCR");
            }
            ParsedDocument document = TextDocumentParser.baseDocument(request, fullText);
            int pages = pdDocument.getNumberOfPages();
            for (int i = 1; i <= pages; i++) {
                PDFTextStripper pageStripper = new PDFTextStripper();
                pageStripper.setStartPage(i);
                pageStripper.setEndPage(i);
                String pageText = TextDocumentParser.normalizeText(pageStripper.getText(pdDocument));
                if (!pageText.isBlank()) {
                    document.getBlocks().add(new DocumentBlock(pageText, i, "", ""));
                }
            }
            document.getMetadata().put("page_count", pages);
            document.getMetadata().putIfAbsent("page_location_source", "source_pdf");
            document.getMetadata().put("page_location_support", true);
            return document;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("PARSE_FAILED", "pdf parse failed: " + ex.getMessage());
        }
    }

    private String normalize(String fileType) {
        return fileType == null ? "" : fileType.toLowerCase(Locale.ROOT).replace(".", "");
    }
}
