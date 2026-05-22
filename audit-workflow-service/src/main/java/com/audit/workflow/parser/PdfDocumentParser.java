package com.audit.workflow.parser;

import com.audit.workflow.common.BusinessException;
import com.audit.workflow.domain.DocumentBlock;
import com.audit.workflow.domain.ParsedDocument;
import com.audit.workflow.service.OcrClient;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class PdfDocumentParser implements DocumentParser {

    private static final int OCR_DPI = 220;

    private final OcrClient ocrClient;

    public PdfDocumentParser() {
        this((OcrClient) null);
    }

    @Autowired
    public PdfDocumentParser(ObjectProvider<OcrClient> ocrClientProvider) {
        this(ocrClientProvider == null ? null : ocrClientProvider.getIfAvailable());
    }

    PdfDocumentParser(OcrClient ocrClient) {
        this.ocrClient = ocrClient;
    }

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
            PDFRenderer renderer = null;
            List<DocumentBlock> blocks = new ArrayList<>();
            StringBuilder fullTextBuilder = new StringBuilder();
            boolean usedOcr = false;
            int pages = pdDocument.getNumberOfPages();
            for (int i = 1; i <= pages; i++) {
                PDFTextStripper pageStripper = new PDFTextStripper();
                pageStripper.setStartPage(i);
                pageStripper.setEndPage(i);
                String pageText = TextDocumentParser.normalizeText(pageStripper.getText(pdDocument));
                if (pageText.isBlank() && ocrClient != null) {
                    if (renderer == null) {
                        renderer = new PDFRenderer(pdDocument);
                    }
                    pageText = TextDocumentParser.normalizeText(recognizePage(renderer, i));
                    usedOcr = usedOcr || !pageText.isBlank();
                }
                if (!pageText.isBlank()) {
                    if (fullTextBuilder.length() > 0) {
                        fullTextBuilder.append("\n\n");
                    }
                    fullTextBuilder.append(pageText);
                    blocks.add(new DocumentBlock(pageText, i, "", ""));
                }
            }
            String fullText = TextDocumentParser.normalizeText(fullTextBuilder.toString());
            if (fullText.isBlank()) {
                throw new BusinessException("PARSE_NO_TEXT", "未识别到可解析文本，可能需要 OCR");
            }
            ParsedDocument document = TextDocumentParser.baseDocument(request, fullText);
            document.getBlocks().addAll(blocks);
            document.getMetadata().put("page_count", pages);
            document.getMetadata().putIfAbsent("page_location_source", usedOcr ? "source_pdf_ocr" : "source_pdf");
            document.getMetadata().put("page_location_support", true);
            if (usedOcr) {
                document.getMetadata().put("ocr_enabled", true);
            }
            return document;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("PARSE_FAILED", "pdf parse failed: " + ex.getMessage());
        }
    }

    private String recognizePage(PDFRenderer renderer, int pageNo) {
        try {
            BufferedImage image = renderer.renderImageWithDPI(pageNo - 1, OCR_DPI, ImageType.RGB);
            return ocrClient.recognize(image, pageNo);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("OCR_FAILED", "pdf page OCR failed: " + ex.getMessage());
        }
    }

    private String normalize(String fileType) {
        return fileType == null ? "" : fileType.toLowerCase(Locale.ROOT).replace(".", "");
    }
}
