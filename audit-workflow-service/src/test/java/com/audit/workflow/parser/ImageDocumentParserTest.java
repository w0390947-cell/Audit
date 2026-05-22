package com.audit.workflow.parser;

import com.audit.workflow.common.BusinessException;
import com.audit.workflow.domain.ParsedDocument;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageDocumentParserTest {

    @Test
    void supportsOnlyJpgJpegAndPng() {
        ImageDocumentParser parser = new ImageDocumentParser((image, pageNo) -> "ignored");

        assertThat(parser.supports("jpg")).isTrue();
        assertThat(parser.supports("jpeg")).isTrue();
        assertThat(parser.supports("png")).isTrue();
        assertThat(parser.supports("gif")).isFalse();
        assertThat(parser.supports("bmp")).isFalse();
    }

    @Test
    void parseImageWithOcrText() throws Exception {
        ImageDocumentParser parser = new ImageDocumentParser((image, pageNo) -> {
            assertThat(pageNo).isEqualTo(1);
            return "图片依据 OCR 文本";
        });

        DocumentParseRequest request = request("basis.png", "png", imageBytes("png"));
        ParsedDocument document = parser.parse(request);

        assertThat(document.getFullText()).isEqualTo("图片依据 OCR 文本");
        assertThat(document.getBlocks()).hasSize(1);
        assertThat(document.getBlocks().get(0).getPageNo()).isEqualTo(1);
        assertThat(document.getMetadata()).containsEntry("page_location_source", "image_ocr");
    }

    @Test
    void emptyOcrTextFailsAsNoText() throws Exception {
        ImageDocumentParser parser = new ImageDocumentParser((image, pageNo) -> "   ");

        DocumentParseRequest request = request("basis.jpg", "jpg", imageBytes("jpg"));

        assertThatThrownBy(() -> parser.parse(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo("PARSE_NO_TEXT");
    }

    private DocumentParseRequest request(String fileName, String fileType, byte[] content) {
        DocumentParseRequest request = new DocumentParseRequest();
        request.setFileName(fileName);
        request.setFileType(fileType);
        request.setContent(content);
        return request;
    }

    private byte[] imageBytes(String formatName) throws Exception {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, formatName, outputStream);
            return outputStream.toByteArray();
        }
    }
}
