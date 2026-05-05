package com.ruoyi.system.service.audit.vector.support;

import com.ruoyi.common.utils.StringUtils;

public final class DocumentTextCleaner
{
    private DocumentTextCleaner()
    {
    }

    public static String clean(String text)
    {
        if (text == null)
        {
            return "";
        }
        String cleaned = text.replace("\r\n", "\n").replace('\r', '\n');
        cleaned = cleaned.replaceAll("[\\t\\x0B\\f]+", " ");
        cleaned = cleaned.replaceAll(" {2,}", " ");
        cleaned = cleaned.replaceAll("(?m)^[ \\t\\x0B\\f]+", "");
        cleaned = cleaned.replaceAll("(?m)[ \\t\\x0B\\f]+$", "");
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n");
        return cleaned.trim();
    }

    public static boolean hasText(String text)
    {
        return StringUtils.isNotBlank(clean(text));
    }
}
