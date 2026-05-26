package com.audit.workflow.support;

import java.util.regex.Pattern;

public final class TextSanitizer {

    private static final Pattern UNSUPPORTED_CONTROL_CHARS =
            Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]");

    private TextSanitizer() {
    }

    public static String cleanForStorage(String text) {
        if (text == null) {
            return "";
        }
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
        return UNSUPPORTED_CONTROL_CHARS.matcher(normalized).replaceAll("").trim();
    }
}
