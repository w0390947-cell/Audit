package com.ruoyi.system.service.audit.vector.parser;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.audit.vector.DocumentParseResult;
import com.ruoyi.system.domain.audit.vector.DocumentTextBlock;
import com.ruoyi.system.service.audit.vector.OcrClient;
import com.ruoyi.system.service.audit.vector.support.DocumentTextCleaner;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImageDocumentParser implements DocumentParser
{
    private static final String OCR_DISABLED_ERROR = "图片 OCR 未启用";

    private final OcrClient ocrClient;

    @Autowired
    public ImageDocumentParser(ObjectProvider<OcrClient> ocrClientProvider)
    {
        this(ocrClientProvider == null ? null : ocrClientProvider.getIfAvailable());
    }

    ImageDocumentParser(OcrClient ocrClient)
    {
        this.ocrClient = ocrClient;
    }

    @Override
    public boolean supports(String fileName, String contentType)
    {
        String fileType = resolveFileType(fileName);
        return "jpg".equals(fileType) || "jpeg".equals(fileType) || "png".equals(fileType);
    }

    @Override
    public DocumentParseResult parse(Long resourceId, String fileName, String fileUrl, InputStream inputStream)
    {
        String fileType = resolveFileType(fileName);
        if (ocrClient == null)
        {
            return DocumentParseResult.failed(resourceId, fileName, fileUrl, fileType, OCR_DISABLED_ERROR);
        }
        try
        {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null)
            {
                return DocumentParseResult.failed(resourceId, fileName, fileUrl, fileType, "图片解析失败：无法读取图片内容");
            }
            String text = DocumentTextCleaner.clean(ocrClient.recognize(image, 1));
            if (!DocumentTextCleaner.hasText(text))
            {
                return DocumentParseResult.textEmpty(resourceId, fileName, fileUrl, fileType);
            }
            List<DocumentTextBlock> blocks = new ArrayList<>();
            DocumentTextBlock block = new DocumentTextBlock();
            block.setPageNo(1);
            block.setBlockNo(1);
            block.setSectionTitle("");
            block.setText(text);
            blocks.add(block);
            return DocumentParseResult.success(resourceId, fileName, fileUrl, fileType, blocks);
        }
        catch (Exception e)
        {
            return DocumentParseResult.failed(resourceId, fileName, fileUrl, fileType, "图片 OCR 失败：" + e.getMessage());
        }
    }

    private String resolveFileType(String fileName)
    {
        if (StringUtils.isBlank(fileName) || !fileName.contains("."))
        {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
