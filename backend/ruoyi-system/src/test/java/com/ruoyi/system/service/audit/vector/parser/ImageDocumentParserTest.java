package com.ruoyi.system.service.audit.vector.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.ruoyi.system.domain.audit.vector.DocumentParseResult;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ImageDocumentParserTest
{
    @Test
    void parsePngWithOcrText() throws Exception
    {
        ImageDocumentParser parser = new ImageDocumentParser((image, pageNo) -> {
            assertEquals(1, pageNo);
            return "图片 OCR 文本";
        });

        DocumentParseResult result = parser.parse(1L, "sample.png", "/profile/upload/sample.png",
                new ByteArrayInputStream(imageBytes("png")));

        assertTrue(result.isSuccess());
        assertFalse(result.isTextEmpty());
        assertEquals("png", result.getFileType());
        assertEquals(1, result.getBlocks().size());
        assertEquals(1, result.getBlocks().get(0).getPageNo());
        assertEquals("图片 OCR 文本", result.getBlocks().get(0).getText());
    }

    @Test
    void parseJpgWithEmptyOcrTextReturnsTextEmpty() throws Exception
    {
        ImageDocumentParser parser = new ImageDocumentParser((image, pageNo) -> "   ");

        DocumentParseResult result = parser.parse(1L, "sample.jpg", "/profile/upload/sample.jpg",
                new ByteArrayInputStream(imageBytes("jpg")));

        assertFalse(result.isSuccess());
        assertTrue(result.isTextEmpty());
        assertEquals(DocumentParseResult.TEXT_EMPTY_ERROR, result.getErrorMsg());
    }

    @Test
    void unsupportedImageTypesAreRejected()
    {
        ImageDocumentParser parser = new ImageDocumentParser((image, pageNo) -> "ignored");

        assertTrue(parser.supports("sample.jpeg", null));
        assertFalse(parser.supports("sample.gif", null));
        assertFalse(parser.supports("sample.bmp", null));
    }

    @Test
    void parseWithoutOcrClientReturnsFailed()
    {
        ImageDocumentParser parser = new ImageDocumentParser((com.ruoyi.system.service.audit.vector.OcrClient) null);

        DocumentParseResult result = parser.parse(1L, "sample.png", "/profile/upload/sample.png",
                new ByteArrayInputStream(new byte[0]));

        assertFalse(result.isSuccess());
        assertEquals("图片 OCR 未启用", result.getErrorMsg());
    }

    private byte[] imageBytes(String formatName) throws Exception
    {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
        {
            ImageIO.write(image, formatName, outputStream);
            return outputStream.toByteArray();
        }
    }
}
