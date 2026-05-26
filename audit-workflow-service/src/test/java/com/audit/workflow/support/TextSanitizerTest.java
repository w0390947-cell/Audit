package com.audit.workflow.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextSanitizerTest {

    @Test
    void cleanForStorageRemovesPostgresUnsupportedControlCharacters() {
        String cleaned = TextSanitizer.cleanForStorage(" abc\u0000def\u0007ghi ");

        assertThat(cleaned).isEqualTo("abcdefghi");
    }

    @Test
    void cleanForStorageNormalizesLineBreaksAndKeepsTabs() {
        String cleaned = TextSanitizer.cleanForStorage("a\r\nb\rc\td");

        assertThat(cleaned).isEqualTo("a\nb\nc\td");
    }
}
