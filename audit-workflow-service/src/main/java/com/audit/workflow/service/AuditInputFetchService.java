package com.audit.workflow.service;

import com.audit.workflow.common.BusinessException;
import com.audit.workflow.support.HashSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;

@Service
public class AuditInputFetchService {

    private final HttpClient httpClient;
    private final int maxFileBytes;
    private final int timeoutSeconds;

    public AuditInputFetchService(@Value("${audit.input.max-file-bytes:52428800}") int maxFileBytes,
                                  @Value("${audit.input.download-timeout-seconds:30}") int timeoutSeconds) {
        this.maxFileBytes = maxFileBytes;
        this.timeoutSeconds = timeoutSeconds;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public AuditInputFetchResult fetch(Map<String, Object> input) {
        if (input == null || input.isEmpty()) {
            throw new BusinessException("INPUT_EMPTY", "input is empty");
        }

        AuditInputFetchResult result = new AuditInputFetchResult();
        result.setFileId(asString(input.get("file_id"), asString(input.get("fileId"), "")));
        result.setFileUrl(asString(input.get("file_url"), asString(input.get("fileUrl"), "")));
        result.setFileName(asString(input.get("file_name"), asString(input.get("fileName"), "")));
        result.setFileType(resolveFileType(asString(input.get("file_type"), asString(input.get("fileType"), "")), result.getFileName(), result.getFileUrl()));
        Object metadata = input.get("metadata");
        if (metadata instanceof Map<?, ?> metadataMap) {
            for (Map.Entry<?, ?> entry : metadataMap.entrySet()) {
                if (entry.getKey() != null) {
                    result.getMetadata().put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
        }

        String text = asString(input.get("text"), "");
        if (!text.isBlank()) {
            result.setInputType("text");
            result.setText(text);
            result.setFileHash(HashSupport.sha256(text));
            if (result.getFileType() == null || result.getFileType().isBlank()) {
                result.setFileType("text");
            }
            return result;
        }

        if (result.getFileUrl() != null && !result.getFileUrl().isBlank()) {
            result.setInputType("file_url");
            String parseFileUrl = result.getFileUrl();
            String parseFileType = result.getFileType();
            String previewPdfUrl = previewPdfUrl(input, result.getMetadata());
            if (isConvertibleSourceFile(result.getFileType()) && !previewPdfUrl.isBlank()) {
                result.getMetadata().putIfAbsent("original_file_url", result.getFileUrl());
                result.getMetadata().putIfAbsent("original_file_name", result.getFileName());
                result.getMetadata().putIfAbsent("original_file_type", result.getFileType());
                result.getMetadata().put("parsed_file_url", previewPdfUrl);
                result.getMetadata().put("parsed_file_type", "pdf");
                result.getMetadata().put("page_location_source", "preview_pdf");
                parseFileUrl = previewPdfUrl;
                parseFileType = "pdf";
            } else if (isWordFile(result.getFileType())) {
                result.getMetadata().put("page_location_source", "unsupported_word_pagination");
                result.getMetadata().put("page_location_support", false);
            } else if ("pdf".equals(result.getFileType())) {
                result.getMetadata().put("page_location_source", "source_pdf");
                result.getMetadata().put("page_location_support", true);
            }
            byte[] content = download(parseFileUrl);
            result.setContent(content);
            result.setFileHash(HashSupport.sha256(content));
            result.setFileType(parseFileType);
            return result;
        }

        if (result.getFileId() != null && !result.getFileId().isBlank()) {
            throw new BusinessException("FILE_FETCH_UNAUTHORIZED", "file_id requires business file service integration or file_url");
        }

        throw new BusinessException("INPUT_EMPTY", "text or file_url is required");
    }

    private byte[] download(String fileUrl) {
        URI uri = normalizeFileUri(fileUrl);
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new BusinessException("FILE_DOWNLOAD_FAILED", "only http/https file_url is supported");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .GET()
                    .build();
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException("FILE_DOWNLOAD_FAILED", "download failed, http status " + response.statusCode());
            }
            try (InputStream inputStream = response.body(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int total = 0;
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    total += read;
                    if (total > maxFileBytes) {
                        throw new BusinessException("INPUT_FILE_TOO_LARGE", "file exceeds max size " + maxFileBytes);
                    }
                    outputStream.write(buffer, 0, read);
                }
                return outputStream.toByteArray();
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("FILE_DOWNLOAD_FAILED", "download failed: " + ex.getMessage());
        }
    }

    private URI normalizeFileUri(String fileUrl) {
        try {
            return URI.create(fileUrl);
        } catch (Exception ignored) {
            try {
                URL url = new URL(fileUrl);
                return new URI(
                        url.getProtocol(),
                        url.getUserInfo(),
                        url.getHost(),
                        url.getPort(),
                        url.getPath(),
                        url.getQuery(),
                        url.getRef());
            } catch (Exception ex) {
                throw new BusinessException("FILE_DOWNLOAD_FAILED", "invalid file_url: " + abbreviate(fileUrl, 200));
            }
        }
    }

    private String resolveFileType(String explicitType, String fileName, String fileUrl) {
        if (explicitType != null && !explicitType.isBlank()) {
            return explicitType.toLowerCase(Locale.ROOT).replace(".", "");
        }
        String name = fileName;
        if ((name == null || name.isBlank()) && fileUrl != null) {
            int slash = fileUrl.lastIndexOf('/');
            name = slash >= 0 ? fileUrl.substring(slash + 1) : fileUrl;
        }
        if (name == null) {
            return "";
        }
        int query = name.indexOf('?');
        if (query >= 0) {
            name = name.substring(0, query);
        }
        int fragment = name.indexOf('#');
        if (fragment >= 0) {
            name = name.substring(0, fragment);
        }
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String previewPdfUrl(Map<String, Object> input, Map<String, Object> metadata) {
        return firstNotBlank(
                input.get("preview_pdf_url"),
                input.get("previewPdfUrl"),
                input.get("pdf_url"),
                input.get("pdfUrl"),
                metadata.get("preview_pdf_url"),
                metadata.get("previewPdfUrl"),
                metadata.get("pdf_url"),
                metadata.get("pdfUrl"));
    }

    private boolean isWordFile(String fileType) {
        String normalized = fileType == null ? "" : fileType.toLowerCase(Locale.ROOT).replace(".", "");
        return "doc".equals(normalized) || "docx".equals(normalized);
    }

    private boolean isConvertibleSourceFile(String fileType) {
        String normalized = fileType == null ? "" : fileType.toLowerCase(Locale.ROOT).replace(".", "");
        return switch (normalized) {
            case "doc", "docx", "xls", "xlsx", "ppt", "pptx", "html", "htm", "txt" -> true;
            default -> false;
        };
    }

    private String firstNotBlank(Object... values) {
        for (Object value : values) {
            String text = asString(value, "").trim();
            if (!text.isBlank()) {
                return text;
            }
        }
        return "";
    }

    private String asString(Object value, String defaultValue) {
        return value == null ? defaultValue : String.valueOf(value);
    }

    private String abbreviate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
