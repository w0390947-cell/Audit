package com.ruoyi.system.service.audit.vector.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import com.ruoyi.system.domain.audit.vector.DocumentParseResult;

class TextDocumentParserTest
{
    private final TextDocumentParser parser = new TextDocumentParser();

    @Test
    void parseUtf8Text()
    {
        DocumentParseResult result = parser.parse(1L, "sample.txt", "/profile/upload/sample.txt",
                new ByteArrayInputStream("第一段文本\n\n第二段文本".getBytes(StandardCharsets.UTF_8)));

        assertTrue(result.isSuccess());
        assertFalse(result.isTextEmpty());
        assertEquals(2, result.getBlocks().size());
        assertEquals("第一段文本", result.getBlocks().get(0).getText());
    }

    @Test
    void parseEmptyText()
    {
        DocumentParseResult result = parser.parse(1L, "empty.txt", "/profile/upload/empty.txt",
                new ByteArrayInputStream("   \n\n\t".getBytes(StandardCharsets.UTF_8)));

        assertFalse(result.isSuccess());
        assertTrue(result.isTextEmpty());
        assertEquals(DocumentParseResult.TEXT_EMPTY_ERROR, result.getErrorMsg());
    }
}
