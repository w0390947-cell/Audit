package com.audit.workflow.parser;

import com.audit.workflow.domain.ParsedDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class PdfDocumentParserTest {

    @Test
    void parseTextPdfWithoutOcr() throws Exception {
        AtomicInteger ocrCalls = new AtomicInteger();
        PdfDocumentParser parser = new PdfDocumentParser((image, pageNo) -> {
            ocrCalls.incrementAndGet();
            return "should not be used";
        });

        ParsedDocument document = parser.parse(request(pdfBytes("PDF text"), "text.pdf"));

        assertThat(document.getFullText()).contains("PDF text");
        assertThat(document.getBlocks()).hasSize(1);
        assertThat(document.getBlocks().get(0).getPageNo()).isEqualTo(1);
        assertThat(ocrCalls).hasValue(0);
    }

    @Test
    void parseScannedPdfWithOcrFallback() throws Exception {
        AtomicInteger ocrCalls = new AtomicInteger();
        PdfDocumentParser parser = new PdfDocumentParser((image, pageNo) -> {
            ocrCalls.incrementAndGet();
            assertThat(pageNo).isEqualTo(1);
            return "扫描 PDF OCR 文本";
        });

        ParsedDocument document = parser.parse(request(blankPdfBytes(), "scan.pdf"));

        assertThat(document.getFullText()).isEqualTo("扫描 PDF OCR 文本");
        assertThat(document.getBlocks()).hasSize(1);
        assertThat(document.getBlocks().get(0).getPageNo()).isEqualTo(1);
        assertThat(document.getMetadata()).containsEntry("page_location_source", "source_pdf_ocr");
        assertThat(ocrCalls).hasValue(1);
    }

    private DocumentParseRequest request(byte[] content, String fileName) {
        DocumentParseRequest request = new DocumentParseRequest();
        request.setFileName(fileName);
        request.setFileType("pdf");
        request.setContent(content);
        return request;
    }

    private byte[] pdfBytes(String text) throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText(text);
                contentStream.endText();
            }
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] blankPdfBytes() throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }
}
