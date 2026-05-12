package com.audit.workflow.support;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class HashSupport {

    private HashSupport() {
    }

    public static String sha256(String text) {
        return sha256(text == null ? new byte[0] : text.getBytes(StandardCharsets.UTF_8));
    }

    public static String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(bytes == null ? new byte[0] : bytes);
            StringBuilder builder = new StringBuilder();
            for (byte b : hashed) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to calculate sha256", ex);
        }
    }
}
