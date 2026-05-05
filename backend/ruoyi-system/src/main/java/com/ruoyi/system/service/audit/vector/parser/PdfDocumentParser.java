package com.ruoyi.system.service.audit.vector.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import com.ruoyi.system.domain.audit.vector.DocumentParseResult;
import com.ruoyi.system.domain.audit.vector.DocumentTextBlock;
import com.ruoyi.system.service.audit.vector.support.DocumentTextCleaner;

@Component
public class PdfDocumentParser implements DocumentParser
{
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
            List<DocumentTextBlock> blocks = new ArrayList<>();
            int blockNo = 1;
            for (int page = 1; page <= document.getNumberOfPages(); page++)
            {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String text = DocumentTextCleaner.clean(stripper.getText(document));
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
}
