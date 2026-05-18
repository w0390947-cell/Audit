package com.ruoyi.system.service.audit.vector.parser;

import com.ruoyi.system.config.VectorProperties;
import java.io.InputStream;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.system.domain.audit.vector.DocumentParseResult;
import com.ruoyi.system.domain.audit.vector.DocumentTextBlock;
import com.ruoyi.system.service.audit.vector.OcrClient;
import com.ruoyi.system.service.audit.vector.support.DocumentTextCleaner;

@Component
public class PdfDocumentParser implements DocumentParser
{
    private static final int DEFAULT_OCR_DPI = 220;

    private final VectorProperties properties;

    private final OcrClient ocrClient;

    public PdfDocumentParser()
    {
        this(new VectorProperties(), (OcrClient) null);
    }

    @Autowired
    public PdfDocumentParser(VectorProperties properties, ObjectProvider<OcrClient> ocrClientProvider)
    {
        this(properties, ocrClientProvider == null ? null : ocrClientProvider.getIfAvailable());
    }

    PdfDocumentParser(VectorProperties properties, OcrClient ocrClient)
    {
        this.properties = properties == null ? new VectorProperties() : properties;
        this.ocrClient = ocrClient;
    }

    @Override
    public boolean supports(String fileName, String contentType)
    {
        return fileName != null && fileName.toLowerCase().endsWith(".pdf");
    }

    @Override
    public DocumentParseResult parse(Long resourceId, String fileName, String fileUrl, InputStream inputStream)
    {
        try (PDDocument document = PDDocument.load(inputStream))
        {
            PDFTextStripper stripper = new PDFTextStripper();
            PDFRenderer renderer = null;
            List<DocumentTextBlock> blocks = new ArrayList<>();
            int blockNo = 1;
            for (int page = 1; page <= document.getNumberOfPages(); page++)
            {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String text = DocumentTextCleaner.clean(stripper.getText(document));
                if (!DocumentTextCleaner.hasText(text) && shouldOcr(page))
                {
                    if (renderer == null)
                    {
                        renderer = new PDFRenderer(document);
                    }
                    text = DocumentTextCleaner.clean(recognizePage(renderer, page));
                }
                if (DocumentTextCleaner.hasText(text))
                {
                    DocumentTextBlock block = new DocumentTextBlock();
                    block.setPageNo(page);
                    block.setBlockNo(blockNo++);
                    block.setSectionTitle("");
                    block.setText(text);
                    blocks.add(block);
                }
            }
            if (blocks.isEmpty())
            {
                return DocumentParseResult.textEmpty(resourceId, fileName, fileUrl, "pdf");
            }
            return DocumentParseResult.success(resourceId, fileName, fileUrl, "pdf", blocks);
        }
        catch (Exception e)
        {
            return DocumentParseResult.failed(resourceId, fileName, fileUrl, "pdf", "PDF 解析失败：" + e.getMessage());
        }
    }

    private boolean shouldOcr(int pageNo)
    {
        VectorProperties.Ocr ocr = properties.getOcr();
        if (ocrClient == null || ocr == null || !ocr.isEnabled())
        {
            return false;
        }
        return ocr.getMaxPages() <= 0 || pageNo <= ocr.getMaxPages();
    }

    private String recognizePage(PDFRenderer renderer, int pageNo)
    {
        try
        {
            int dpi = Math.max(72, properties.getOcr().getDpi() <= 0 ? DEFAULT_OCR_DPI : properties.getOcr().getDpi());
            BufferedImage image = renderer.renderImageWithDPI(pageNo - 1, dpi, ImageType.RGB);
            return ocrClient.recognize(image, pageNo);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("PDF 第 " + pageNo + " 页 OCR 失败：" + e.getMessage(), e);
        }
    }
}
