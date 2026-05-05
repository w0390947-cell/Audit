package com.ruoyi.system.service.audit.vector.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import com.ruoyi.system.domain.audit.vector.DocumentParseResult;

class WordDocumentParserTest
{
    private final WordDocumentParser parser = new WordDocumentParser();

    @Test
    void parseDocxParagraphs() throws Exception
    {
        DocumentParseResult result = parser.parse(1L, "sample.docx", "/profile/upload/sample.docx",
                new ByteArrayInputStream(docxBytes("第一段 Word 文本", "第二段 Word 文本")));

        assertTrue(result.isSuccess());
        assertFalse(result.isTextEmpty());
        assertEquals(2, result.getBlocks().size());
        assertEquals("第一段 Word 文本", result.getBlocks().get(0).getText());
    }

    private byte[] docxBytes(String... paragraphs) throws Exception
    {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
        {
            for (String paragraph : paragraphs)
            {
                document.createParagraph().createRun().setText(paragraph);
            }
            document.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
