package com.audit.workflow.service;

import com.audit.workflow.domain.ContentChunk;
import com.audit.workflow.domain.DocumentBlock;
import com.audit.workflow.domain.ParsedDocument;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TextChunkServiceTest {

    @Test
    void splitKeepsKnownPagesInSeparateChunksEvenWhenShorterThanMinChars() {
        TextChunkService service = new TextChunkService(200, 1200, 150);
        ParsedDocument document = new ParsedDocument();
        document.getBlocks().add(new DocumentBlock("page one text", 1, "", ""));
        document.getBlocks().add(new DocumentBlock("page two text", 2, "", ""));

        List<ContentChunk> chunks = service.split(document);

        assertThat(chunks).hasSize(2);
        assertThat(chunks).extracting(ContentChunk::getChunkNo).containsExactly(1, 2);
        assertThat(chunks).extracting(ContentChunk::getPageNo).containsExactly(1, 2);
        assertThat(chunks.get(0).getChunkText()).isEqualTo("page one text");
        assertThat(chunks.get(1).getChunkText()).isEqualTo("page two text");
    }

    @Test
    void splitBreaksLongKnownPageOnlyWithinThatPage() {
        TextChunkService service = new TextChunkService(5, 10, 2);
        ParsedDocument document = new ParsedDocument();
        document.getBlocks().add(new DocumentBlock("abcdefghijklmnopqrstuvwxy", 3, "", ""));

        List<ContentChunk> chunks = service.split(document);

        assertThat(chunks).hasSize(3);
        assertThat(chunks).extracting(ContentChunk::getPageNo).containsExactly(3, 3, 3);
        assertThat(chunks).extracting(ContentChunk::getChunkText)
                .containsExactly("abcdefghij", "ijklmnopqr", "qrstuvwxy");
    }

    @Test
    void splitKeepsExistingMergeBehaviorForUnpagedBlocks() {
        TextChunkService service = new TextChunkService(200, 1200, 150);
        ParsedDocument document = new ParsedDocument();
        document.getBlocks().add(new DocumentBlock("first paragraph", null, "", ""));
        document.getBlocks().add(new DocumentBlock("second paragraph", null, "", ""));

        List<ContentChunk> chunks = service.split(document);

        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0).getPageNo()).isNull();
        assertThat(chunks.get(0).getChunkText()).isEqualTo("first paragraph\n\nsecond paragraph");
    }
}
