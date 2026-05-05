package com.ruoyi.system.service.audit.vector.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;
import com.ruoyi.system.domain.audit.vector.DocumentParseResult;

class PdfDocumentParserTest
{
    private final PdfDocumentParser parser = new PdfDocumentParser();

    @Test
    void parsePdfWithPageNo() throws Exception
    {
        DocumentParseResult result = parser.parse(1L, "sample.pdf", "/profile/upload/sample.pdf",
                new ByteArrayInputStream(pdfBytes("PDF parser smoke test")));

        assertTrue(result.isSuccess());
        assertFalse(result.isTextEmpty());
        assertEquals(1, result.getBlocks().get(0).getPageNo());
        assertTrue(result.getBlocks().get(0).getText().contains("PDF parser smoke test"));
    }

    private byte[] pdfBytes(String text) throws Exception
    {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
        {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page))
            {
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
}
