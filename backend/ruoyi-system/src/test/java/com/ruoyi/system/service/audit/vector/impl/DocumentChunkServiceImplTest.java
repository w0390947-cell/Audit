package com.ruoyi.system.service.audit.vector.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.ruoyi.system.config.VectorProperties;
import com.ruoyi.system.domain.audit.vector.DocumentChunk;
import com.ruoyi.system.domain.audit.vector.DocumentParseResult;
import com.ruoyi.system.domain.audit.vector.DocumentTextBlock;

class DocumentChunkServiceImplTest
{
    @Test
    void chunkResultIsStable()
    {
        DocumentChunkServiceImpl service = service(80, 10, 5);
        DocumentParseResult result = parseResult("第一段内容。第二段内容。第三段内容。", "第四段内容。第五段内容。");

        List<DocumentChunk> first = service.chunk(result);
        List<DocumentChunk> second = service.chunk(result);

        assertEquals(first.size(), second.size());
        for (int i = 0; i < first.size(); i++)
        {
            assertEquals(first.get(i).getChunkNo(), second.get(i).getChunkNo());
            assertEquals(first.get(i).getChunkText(), second.get(i).getChunkText());
        }
    }

    @Test
    void longParagraphCanBeSplit()
    {
        DocumentChunkServiceImpl service = service(120, 20, 10);
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 20; i++)
        {
            text.append("这是一个用于测试超长段落切分的句子").append(i).append("。");
        }

        List<DocumentChunk> chunks = service.chunk(parseResult(text.toString()));

        assertTrue(chunks.size() > 1);
        assertEquals(1, chunks.get(0).getChunkNo());
        for (DocumentChunk chunk : chunks)
        {
            assertFalse(chunk.getChunkText().isBlank());
            assertTrue(chunk.getChunkText().length() <= 140);
        }
    }

    private DocumentChunkServiceImpl service(int maxChars, int overlapChars, int minChars)
    {
        VectorProperties properties = new VectorProperties();
        properties.getChunk().setMaxChars(maxChars);
        properties.getChunk().setOverlapChars(overlapChars);
        properties.getChunk().setMinChars(minChars);
        return new DocumentChunkServiceImpl(properties);
    }

    private DocumentParseResult parseResult(String... texts)
    {
        List<DocumentTextBlock> blocks = new ArrayList<>();
        for (int i = 0; i < texts.length; i++)
        {
            DocumentTextBlock block = new DocumentTextBlock();
            block.setBlockNo(i + 1);
            block.setPageNo(i + 1);
            block.setSectionTitle("");
            block.setText(texts[i]);
            blocks.add(block);
        }
        return DocumentParseResult.success(1L, "sample.txt", "/profile/upload/sample.txt", "txt", blocks);
    }
}
