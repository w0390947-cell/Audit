package com.ruoyi.system.service.audit.vector.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import com.ruoyi.system.domain.audit.vector.DocumentParseResult;
import com.ruoyi.system.domain.audit.vector.DocumentTextBlock;
import com.ruoyi.system.service.audit.vector.support.DocumentTextCleaner;

@Component
public class WordDocumentParser implements DocumentParser
{
    @Override
    public boolean supports(String fileName, String contentType)
    {
        if (fileName == null)
        {
            return false;
        }
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".doc") || lowerName.endsWith(".docx");
    }

    @Override
    public DocumentParseResult parse(Long resourceId, String fileName, String fileUrl, InputStream inputStream)
    {
        String fileType = resolveFileType(fileName);
        try
        {
            List<DocumentTextBlock> blocks = "docx".equals(fileType) ? parseDocx(inputStream) : parseDoc(inputStream);
            if (blocks.isEmpty())
            {
                return DocumentParseResult.textEmpty(resourceId, fileName, fileUrl, fileType);
            }
            return DocumentParseResult.success(resourceId, fileName, fileUrl, fileType, blocks);
        }
        catch (Exception e)
        {
            return DocumentParseResult.failed(resourceId, fileName, fileUrl, fileType, "Word 解析失败：" + e.getMessage());
        }
    }

    private List<DocumentTextBlock> parseDocx(InputStream inputStream) throws Exception
    {
        List<DocumentTextBlock> blocks = new ArrayList<>();
        try (XWPFDocument document = new XWPFDocument(inputStream))
        {
            int blockNo = 1;
            for (XWPFParagraph paragraph : document.getParagraphs())
            {
                String text = DocumentTextCleaner.clean(paragraph.getText());
                if (DocumentTextCleaner.hasText(text))
                {
                    blocks.add(block(blockNo++, text));
                }
            }
        }
        return blocks;
    }

    private List<DocumentTextBlock> parseDoc(InputStream inputStream) throws Exception
    {
        List<DocumentTextBlock> blocks = new ArrayList<>();
        try (HWPFDocument document = new HWPFDocument(inputStream))
        {
            Range range = document.getRange();
            int blockNo = 1;
            for (int i = 0; i < range.numParagraphs(); i++)
            {
                String text = DocumentTextCleaner.clean(range.getParagraph(i).text());
                if (DocumentTextCleaner.hasText(text))
                {
                    blocks.add(block(blockNo++, text));
                }
            }
        }
        return blocks;
    }

    private DocumentTextBlock block(int blockNo, String text)
    {
        DocumentTextBlock block = new DocumentTextBlock();
        block.setBlockNo(blockNo);
        block.setSectionTitle("");
        block.setText(text);
        return block;
    }

    private String resolveFileType(String fileName)
    {
        return fileName != null && fileName.toLowerCase().endsWith(".docx") ? "docx" : "doc";
    }
}
