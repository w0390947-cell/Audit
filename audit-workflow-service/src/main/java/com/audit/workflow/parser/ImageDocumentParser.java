package com.audit.workflow.parser;

import com.audit.workflow.common.BusinessException;
import com.audit.workflow.domain.DocumentBlock;
import com.audit.workflow.domain.ParsedDocument;
import com.audit.workflow.service.OcrClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Locale;

@Component
public class ImageDocumentParser implements DocumentParser {

    private final OcrClient ocrClient;

    @Autowired
    public ImageDocumentParser(ObjectProvider<OcrClient> ocrClientProvider) {
        this(ocrClientProvider == null ? null : ocrClientProvider.getIfAvailable());
    }

    ImageDocumentParser(OcrClient ocrClient) {
        this.ocrClient = ocrClient;
    }

    @Override
    public boolean supports(String fileType) {
        String normalized = normalize(fileType);
        return "jpg".equals(normalized) || "jpeg".equals(normalized) || "png".equals(normalized);
    }

    @Override
    public ParsedDocument parse(DocumentParseRequest request) {
        if (ocrClient == null) {
            throw new BusinessException("OCR_NOT_CONFIGURED", "OCR endpoint 未配置");
        }
        if (request.getContent() == null || request.getContent().length == 0) {
            throw new BusinessException("PARSE_FAILED", "image content is empty");
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(request.getContent()));
            if (image == null) {
                throw new BusinessException("PARSE_FAILED", "image parse failed: unsupported image content");
            }
            String text = TextDocumentParser.normalizeText(ocrClient.recognize(image, 1));
            if (text.isBlank()) {
                throw new BusinessException("PARSE_NO_TEXT", "未识别到可解析文本");
            }
            ParsedDocument document = TextDocumentParser.baseDocument(request, text);
            document.getBlocks().add(new DocumentBlock(text, 1, "", ""));
            document.getMetadata().put("page_location_source", "image_ocr");
            document.getMetadata().put("page_location_support", true);
            document.getMetadata().put("ocr_enabled", true);
            return document;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("PARSE_FAILED", "image parse failed: " + ex.getMessage());
        }
    }

    private String normalize(String fileType) {
        return fileType == null ? "" : fileType.toLowerCase(Locale.ROOT).replace(".", "");
    }
}
