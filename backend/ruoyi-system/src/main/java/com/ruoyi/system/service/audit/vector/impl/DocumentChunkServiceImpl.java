package com.ruoyi.system.service.audit.vector.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.config.VectorProperties;
import com.ruoyi.system.domain.audit.vector.DocumentChunk;
import com.ruoyi.system.domain.audit.vector.DocumentParseResult;
import com.ruoyi.system.domain.audit.vector.DocumentTextBlock;
import com.ruoyi.system.service.audit.vector.DocumentChunkService;
import com.ruoyi.system.service.audit.vector.support.DocumentTextCleaner;

@Service
public class DocumentChunkServiceImpl implements DocumentChunkService
{
    private final VectorProperties vectorProperties;

    public DocumentChunkServiceImpl(VectorProperties vectorProperties)
    {
        this.vectorProperties = vectorProperties;
    }

    @Override
    public List<DocumentChunk> chunk(DocumentParseResult parseResult)
    {
        List<DocumentChunk> chunks = new ArrayList<>();
        if (parseResult == null || !parseResult.isSuccess() || parseResult.getBlocks().isEmpty())
        {
            return chunks;
        }

        ChunkBuffer buffer = new ChunkBuffer();
        for (DocumentTextBlock block : parseResult.getBlocks())
        {
            List<String> pieces = splitBlock(block.getText());
            for (String piece : pieces)
            {
                String text = DocumentTextCleaner.clean(piece);
                if (StringUtils.isBlank(text))
                {
                    continue;
                }
                if (buffer.isEmpty())
                {
                    buffer.start(block, text);
                }
                else if (buffer.lengthWith(text) <= maxChars() || buffer.length() < minChars())
                {
                    buffer.append(text);
                }
                else
                {
                    chunks.add(toChunk(parseResult.getResourceId(), chunks.size() + 1, buffer));
                    buffer.restartWithOverlap(text, block, overlapChars());
                }
            }
        }
        if (!buffer.isEmpty())
        {
            chunks.add(toChunk(parseResult.getResourceId(), chunks.size() + 1, buffer));
        }
        mergeTinyTail(chunks);
        renumber(chunks);
        return chunks;
    }

    private List<String> splitBlock(String text)
    {
        List<String> result = new ArrayList<>();
        String cleaned = DocumentTextCleaner.clean(text);
        if (cleaned.length() <= maxChars())
        {
            result.add(cleaned);
            return result;
        }

        List<String> sentencePieces = splitBySentence(cleaned);
        StringBuilder current = new StringBuilder();
        for (String sentence : sentencePieces)
        {
            String part = DocumentTextCleaner.clean(sentence);
            if (part.length() > maxChars())
            {
                flush(result, current);
                hardSplit(result, part);
            }
            else if (current.length() == 0)
            {
                current.append(part);
            }
            else if (current.length() + 1 + part.length() <= maxChars())
            {
                current.append('\n').append(part);
            }
            else
            {
                flush(result, current);
                current.append(part);
            }
        }
        flush(result, current);
        return result;
    }

    private List<String> splitBySentence(String text)
    {
        List<String> result = new ArrayList<>();
        String[] parts = text.split("(?<=[。！？；;!?])|\\n+");
        for (String part : parts)
        {
            if (StringUtils.isNotBlank(part))
            {
                result.add(part);
            }
        }
        if (result.isEmpty())
        {
            result.add(text);
        }
        return result;
    }

    private void hardSplit(List<String> result, String text)
    {
        int start = 0;
        while (start < text.length())
        {
            int end = Math.min(start + maxChars(), text.length());
            result.add(text.substring(start, end));
            start = end;
        }
    }

    private void flush(List<String> result, StringBuilder current)
    {
        if (current.length() > 0)
        {
            result.add(current.toString());
            current.setLength(0);
        }
    }

    private DocumentChunk toChunk(Long resourceId, int chunkNo, ChunkBuffer buffer)
    {
        String text = DocumentTextCleaner.clean(buffer.text.toString());
        DocumentChunk chunk = new DocumentChunk();
        chunk.setResourceId(resourceId);
        chunk.setChunkNo(chunkNo);
        chunk.setChunkText(text);
        chunk.setPageNo(buffer.pageNo);
        chunk.setSectionTitle(StringUtils.defaultString(buffer.sectionTitle));
        chunk.setTokenCount(text.length());
        return chunk;
    }

    private void mergeTinyTail(List<DocumentChunk> chunks)
    {
        if (chunks.size() < 2)
        {
            return;
        }
        DocumentChunk tail = chunks.get(chunks.size() - 1);
        DocumentChunk previous = chunks.get(chunks.size() - 2);
        if (tail.getChunkText().length() < minChars()
                && previous.getChunkText().length() + 1 + tail.getChunkText().length() <= maxChars())
        {
            previous.setChunkText(previous.getChunkText() + "\n" + tail.getChunkText());
            previous.setTokenCount(previous.getChunkText().length());
            chunks.remove(chunks.size() - 1);
        }
    }

    private void renumber(List<DocumentChunk> chunks)
    {
        for (int i = 0; i < chunks.size(); i++)
        {
            chunks.get(i).setChunkNo(i + 1);
        }
    }

    private int maxChars()
    {
        return Math.max(1, vectorProperties.getChunk().getMaxChars());
    }

    private int overlapChars()
    {
        return Math.max(0, Math.min(vectorProperties.getChunk().getOverlapChars(), maxChars() / 2));
    }

    private int minChars()
    {
        return Math.max(0, vectorProperties.getChunk().getMinChars());
    }

    private static class ChunkBuffer
    {
        private final StringBuilder text = new StringBuilder();

        private Integer pageNo;

        private String sectionTitle;

        private boolean isEmpty()
        {
            return text.length() == 0;
        }

        private int length()
        {
            return text.length();
        }

        private int lengthWith(String value)
        {
            return text.length() + 1 + value.length();
        }

        private void start(DocumentTextBlock block, String value)
        {
            this.pageNo = block.getPageNo();
            this.sectionTitle = block.getSectionTitle();
            this.text.setLength(0);
            this.text.append(value);
        }

        private void append(String value)
        {
            if (text.length() > 0)
            {
                text.append('\n');
            }
            text.append(value);
        }

        private void restartWithOverlap(String value, DocumentTextBlock block, int overlapChars)
        {
            String overlap = tail(text.toString(), overlapChars);
            this.pageNo = block.getPageNo();
            this.sectionTitle = block.getSectionTitle();
            this.text.setLength(0);
            if (StringUtils.isNotBlank(overlap))
            {
                this.text.append(overlap).append('\n');
            }
            this.text.append(value);
        }

        private String tail(String value, int maxLength)
        {
            if (maxLength <= 0 || StringUtils.isBlank(value))
            {
                return "";
            }
            if (value.length() <= maxLength)
            {
                return value;
            }
            return value.substring(value.length() - maxLength);
        }
    }
}
