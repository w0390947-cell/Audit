package com.ruoyi.system.service.audit.vector.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import com.ruoyi.system.domain.audit.vector.DocumentParseResult;
import com.ruoyi.system.domain.audit.vector.DocumentTextBlock;
import com.ruoyi.system.service.audit.vector.support.DocumentTextCleaner;

@Component
public class TextDocumentParser implements DocumentParser
{
    @Override
    public boolean supports(String fileName, String contentType)
    {
        return fileName != null && fileName.toLowerCase().endsWith(".txt");
    }

    @Override
    public DocumentParseResult parse(Long resourceId, String fileName, String fileUrl, InputStream inputStream)
    {
        try
        {
            byte[] bytes = readAll(inputStream);
            String text = decode(bytes, StandardCharsets.UTF_8);
            if (text.indexOf('\uFFFD') >= 0)
            {
                text = decode(bytes, Charset.forName("GBK"));
            }
            text = removeBom(text);
            List<DocumentTextBlock> blocks = toBlocks(text);
            if (blocks.isEmpty())
            {
                return DocumentParseResult.textEmpty(resourceId, fileName, fileUrl, "txt");
            }
            return DocumentParseResult.success(resourceId, fileName, fileUrl, "txt", blocks);
        }
        catch (Exception e)
        {
            return DocumentParseResult.failed(resourceId, fileName, fileUrl, "txt", "TXT 解析失败：" + e.getMessage());
        }
    }

    private List<DocumentTextBlock> toBlocks(String text)
    {
        List<DocumentTextBlock> blocks = new ArrayList<>();
        String cleaned = DocumentTextCleaner.clean(text);
        if (!DocumentTextCleaner.hasText(cleaned))
        {
            return blocks;
        }
        String[] paragraphs = cleaned.split("\\n\\s*\\n");
        int blockNo = 1;
        for (String paragraph : paragraphs)
        {
            String blockText = DocumentTextCleaner.clean(paragraph);
            if (DocumentTextCleaner.hasText(blockText))
            {
                DocumentTextBlock block = new DocumentTextBlock();
                block.setBlockNo(blockNo++);
                block.setSectionTitle("");
                block.setText(blockText);
                blocks.add(block);
            }
        }
        return blocks;
    }

    private byte[] readAll(InputStream inputStream) throws IOException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int length;
        while ((length = inputStream.read(buffer)) != -1)
        {
            outputStream.write(buffer, 0, length);
        }
        return outputStream.toByteArray();
    }

    private String decode(byte[] bytes, Charset charset)
    {
        return new String(bytes, charset);
    }

    private String removeBom(String text)
    {
        if (text != null && text.startsWith("\uFEFF"))
        {
            return text.substring(1);
        }
        return text;
    }
}
