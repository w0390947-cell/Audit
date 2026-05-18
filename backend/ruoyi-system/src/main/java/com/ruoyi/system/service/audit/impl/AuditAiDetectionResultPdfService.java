package com.ruoyi.system.service.audit.impl;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.audit.AuditAiFinding;
import com.ruoyi.system.domain.audit.AuditAiTask;
import com.ruoyi.system.mapper.audit.AuditAiMapper;

@Service
public class AuditAiDetectionResultPdfService
{
    private static final String COMPLETED_STATUS = "completed";
    private static final String REQUIRED_FONT_SAMPLE = "中文ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            + "4mm²≤≥ⅠⅡⅢ℃μΩ±×÷·—–（）()[]{}《》“”‘’：；，。,.%";

    private final AuditAiMapper auditAiMapper;

    @Value("${audit.pdf.font-path:}")
    private String configuredFontPath;

    public AuditAiDetectionResultPdfService(AuditAiMapper auditAiMapper)
    {
        this.auditAiMapper = auditAiMapper;
    }

    public byte[] exportPdf(Long aiTaskId)
    {
        AuditAiTask task = auditAiMapper.selectAuditAiTaskById(aiTaskId);
        if (task == null)
        {
            throw new ServiceException("AI任务不存在");
        }
        if (!COMPLETED_STATUS.equals(task.getTaskStatus()))
        {
            throw new ServiceException("AI审核分析完成后才可以下载检测结果");
        }
        task.setFindingList(auditAiMapper.selectAuditAiFindingListByTaskId(aiTaskId));

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream())
        {
            try (LoadedFont loadedFont = loadChineseFont(document))
            {
                PdfReportWriter writer = new PdfReportWriter(document, loadedFont.getFont());
                writer.writeTitle("AI审核检测结果报告");
                writer.writeMutedLine("生成时间：" + formatDate(new Date()));
                writer.writeSection("一、任务基础信息");
                writer.writeInfoTable(Arrays.asList(
                        new String[] { "任务编号", value(task.getTaskNo()), "产品名称", value(task.getProductName()) },
                        new String[] { "送检单位", value(task.getDeliveryUnit()), "报告文件", value(task.getReportFileName()) },
                        new String[] { "提交人", value(task.getSubmitter()), "提交时间", formatDate(task.getSubmitTime()) },
                        new String[] { "AI任务状态", "已完成", "AI分析次数", value(task.getAiAnalysisCount()) }));

                writer.writeSection("二、AI总结");
                writer.writeParagraph(defaultText(task.getAiSummary(), "暂无AI总结。"));

                writer.writeSection("三、审核建议");
                writer.writeParagraph(defaultText(task.getReviewOpinion(), "暂无审核建议。"));

                writer.writeSection("四、最终问题清单");
                List<AuditAiFinding> findings = task.getFindingList();
                if (findings == null || findings.isEmpty())
                {
                    writer.writeEmptyBox("本次AI审核未形成最终问题。");
                }
                else
                {
                    for (int i = 0; i < findings.size(); i++)
                    {
                        writer.writeFinding(i + 1, findings.get(i));
                    }
                }

                writer.close();
                document.save(output);
            }
            return output.toByteArray();
        }
        catch (IOException e)
        {
            throw new ServiceException("检测结果PDF生成失败：" + e.getMessage());
        }
    }

    private LoadedFont loadChineseFont(PDDocument document) throws IOException
    {
        List<String> fontPaths = new ArrayList<>();
        if (StringUtils.isNotBlank(configuredFontPath))
        {
            fontPaths.add(configuredFontPath);
        }
        fontPaths.addAll(Arrays.asList(
                "C:/Windows/Fonts/msyh.ttc",
                "C:/Windows/Fonts/simsun.ttc",
                "/mnt/c/Windows/Fonts/msyh.ttc",
                "/mnt/c/Windows/Fonts/simsun.ttc",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.otf",
                "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc",
                "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc",
                "C:/Windows/Fonts/simhei.ttf",
                "/mnt/c/Windows/Fonts/simhei.ttf",
                "/mnt/c/Windows/Fonts/simfang.ttf"));
        List<String> loadErrors = new ArrayList<>();
        for (String fontPath : fontPaths)
        {
            File file = new File(fontPath);
            if (!file.isFile())
            {
                continue;
            }
            try
            {
                LoadedFont loadedFont = loadFont(document, file);
                try
                {
                    validateFont(loadedFont.getFont());
                    return loadedFont;
                }
                catch (IOException e)
                {
                    loadedFont.close();
                    throw e;
                }
            }
            catch (IOException e)
            {
                loadErrors.add(file.getAbsolutePath() + "：" + e.getMessage());
            }
        }
        throw new IOException("未找到可用中文字体，请配置 audit.pdf.font-path 为覆盖完整中文和常用符号的字体"
                + (loadErrors.isEmpty() ? "" : "；已尝试：" + String.join("；", loadErrors)));
    }

    private LoadedFont loadFont(PDDocument document, File file) throws IOException
    {
        String fileName = file.getName().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".ttc"))
        {
            return loadFontFromCollection(document, file);
        }
        return new LoadedFont(PDType0Font.load(document, file), null);
    }

    private LoadedFont loadFontFromCollection(PDDocument document, File file) throws IOException
    {
        TrueTypeCollection collection = new TrueTypeCollection(file);
        List<PDFont> supportedFonts = new ArrayList<>();
        List<String> loadErrors = new ArrayList<>();
        try
        {
            collection.processAllFonts(ttf -> {
                if (!supportedFonts.isEmpty())
                {
                    ttf.close();
                    return;
                }
                try
                {
                    PDFont candidate = PDType0Font.load(document, ttf, true);
                    validateFont(candidate);
                    supportedFonts.add(candidate);
                }
                catch (IOException e)
                {
                    loadErrors.add(e.getMessage());
                    ttf.close();
                }
            });
            if (!supportedFonts.isEmpty())
            {
                return new LoadedFont(supportedFonts.get(0), collection);
            }
            throw new IOException("字体集合中没有可用字体"
                    + (loadErrors.isEmpty() ? "" : "：" + String.join("；", loadErrors)));
        }
        catch (IOException e)
        {
            collection.close();
            throw e;
        }
    }

    private void validateFont(PDFont font) throws IOException
    {
        try
        {
            font.getStringWidth(REQUIRED_FONT_SAMPLE);
        }
        catch (IllegalArgumentException e)
        {
            throw new IOException(e.getMessage(), e);
        }
    }

    private static class LoadedFont implements Closeable
    {
        private final PDFont font;
        private final Closeable closeable;

        private LoadedFont(PDFont font, Closeable closeable)
        {
            this.font = font;
            this.closeable = closeable;
        }

        private PDFont getFont()
        {
            return font;
        }

        @Override
        public void close() throws IOException
        {
            if (closeable != null)
            {
                closeable.close();
            }
        }
    }

    private static String formatDate(Date date)
    {
        if (date == null)
        {
            return "--";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    private static String value(Object value)
    {
        if (value == null || StringUtils.isBlank(String.valueOf(value)))
        {
            return "--";
        }
        return String.valueOf(value);
    }

    private static String defaultText(String value, String defaultText)
    {
        return StringUtils.isBlank(value) ? defaultText : value;
    }

    private static List<BasisReference> issueBasisReferences(AuditAiFinding finding)
    {
        List<BasisReference> references = new ArrayList<>();
        if (finding == null || StringUtils.isBlank(finding.getLocationJson()))
        {
            return references;
        }
        try
        {
            JSONObject location = JSON.parseObject(finding.getLocationJson());
            Object rawBasis = firstObjectValue(location, "basis_references", "basisReferences", "basis");
            if (rawBasis instanceof JSONArray)
            {
                JSONArray array = (JSONArray) rawBasis;
                for (Object item : array)
                {
                    BasisReference reference = normalizeBasisReference(item);
                    if (reference.hasContent())
                    {
                        references.add(reference);
                    }
                }
                return references;
            }
            BasisReference reference = normalizeBasisReference(rawBasis);
            if (reference.hasContent())
            {
                references.add(reference);
            }
            return references;
        }
        catch (Exception e)
        {
            return references;
        }
    }

    private static Object firstObjectValue(JSONObject object, String... keys)
    {
        if (object == null)
        {
            return null;
        }
        for (String key : keys)
        {
            Object value = object.get(key);
            if (value != null)
            {
                return value;
            }
        }
        return null;
    }

    private static BasisReference normalizeBasisReference(Object value)
    {
        if (!(value instanceof JSONObject))
        {
            return new BasisReference();
        }
        JSONObject item = (JSONObject) value;
        BasisReference reference = new BasisReference();
        reference.fileName = firstTextValue(item, "file_name", "fileName", "source", "basis_file_name", "basisFileName");
        reference.versionNo = firstTextValue(item, "version_no", "versionNo", "version");
        reference.pageNo = normalizePageNo(firstTextValue(item, "page", "pageNo", "page_no"));
        reference.sectionTitle = firstTextValue(item, "section", "section_title", "sectionTitle");
        reference.ruleCode = firstTextValue(item, "rule_code", "ruleCode");
        reference.quoteText = firstTextValue(item, "quote", "quote_text", "basis_quote", "basisQuote", "content");
        reference.conflictDescription = firstTextValue(item, "conflict_description", "conflictDescription",
                "conflict_note", "conflictNote", "basis_conflict", "basisConflict");
        return reference;
    }

    private static String firstTextValue(JSONObject object, String... keys)
    {
        if (object == null)
        {
            return "";
        }
        for (String key : keys)
        {
            Object value = object.get(key);
            if (value != null && StringUtils.isNotBlank(String.valueOf(value)))
            {
                return String.valueOf(value).trim();
            }
        }
        return "";
    }

    private static Integer normalizePageNo(String value)
    {
        if (StringUtils.isBlank(value))
        {
            return null;
        }
        try
        {
            int pageNo = Integer.parseInt(value.trim());
            return pageNo > 0 ? pageNo : null;
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private static class BasisReference
    {
        private String fileName;
        private String versionNo;
        private Integer pageNo;
        private String sectionTitle;
        private String ruleCode;
        private String quoteText;
        private String conflictDescription;

        private boolean hasContent()
        {
            return StringUtils.isNotBlank(fileName) || StringUtils.isNotBlank(versionNo) || pageNo != null
                    || StringUtils.isNotBlank(sectionTitle) || StringUtils.isNotBlank(ruleCode)
                    || StringUtils.isNotBlank(quoteText) || StringUtils.isNotBlank(conflictDescription);
        }
    }

    private static class PdfReportWriter
    {
        private static final float MARGIN = 48F;
        private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
        private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
        private static final float CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2;
        private static final Color TITLE_COLOR = new Color(35, 47, 62);
        private static final Color TEXT_COLOR = new Color(48, 49, 51);
        private static final Color MUTED_COLOR = new Color(96, 98, 102);
        private static final Color LINE_COLOR = new Color(220, 223, 230);
        private static final Color SECTION_BG = new Color(242, 246, 252);
        private static final Color ISSUE_BG = new Color(253, 246, 236);

        private final PDDocument document;
        private final PDFont font;
        private PDPage page;
        private PDPageContentStream content;
        private float y;

        PdfReportWriter(PDDocument document, PDFont font) throws IOException
        {
            this.document = document;
            this.font = font;
            addPage();
        }

        void close() throws IOException
        {
            if (content != null)
            {
                content.close();
                content = null;
            }
        }

        void writeTitle(String title) throws IOException
        {
            ensureSpace(48F);
            float fontSize = 22F;
            float width = textWidth(title, fontSize);
            drawText(title, (PAGE_WIDTH - width) / 2F, y, fontSize, TITLE_COLOR);
            y -= 30F;
            drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, LINE_COLOR);
            y -= 20F;
        }

        void writeMutedLine(String text) throws IOException
        {
            drawText(text, MARGIN, y, 10F, MUTED_COLOR);
            y -= 18F;
        }

        void writeSection(String title) throws IOException
        {
            ensureSpace(38F);
            content.setNonStrokingColor(SECTION_BG);
            content.addRect(MARGIN, y - 18F, CONTENT_WIDTH, 24F);
            content.fill();
            drawText(title, MARGIN + 10F, y - 10F, 13F, TITLE_COLOR);
            y -= 38F;
        }

        void writeInfoTable(List<String[]> rows) throws IOException
        {
            float rowHeight = 26F;
            float labelWidth = 72F;
            float valueWidth = (CONTENT_WIDTH - labelWidth * 2) / 2F;
            for (String[] row : rows)
            {
                ensureSpace(rowHeight + 2F);
                float top = y;
                drawLine(MARGIN, top, PAGE_WIDTH - MARGIN, top, LINE_COLOR);
                drawText(row[0], MARGIN + 8F, top - 17F, 10F, MUTED_COLOR);
                drawText(row[1], MARGIN + labelWidth + 8F, top - 17F, 10F, TEXT_COLOR, valueWidth - 16F);
                drawText(row[2], MARGIN + labelWidth + valueWidth + 8F, top - 17F, 10F, MUTED_COLOR);
                drawText(row[3], MARGIN + labelWidth * 2 + valueWidth + 8F, top - 17F, 10F, TEXT_COLOR, valueWidth - 16F);
                y -= rowHeight;
            }
            drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, LINE_COLOR);
            y -= 18F;
        }

        void writeParagraph(String text) throws IOException
        {
            List<String> paragraphs = String.valueOf(text).replace("\r", "").split("\n", -1).length == 0
                    ? Arrays.asList(text) : Arrays.asList(String.valueOf(text).replace("\r", "").split("\n"));
            for (String paragraph : paragraphs)
            {
                List<String> lines = wrap(paragraph, 11F, CONTENT_WIDTH);
                if (lines.isEmpty())
                {
                    y -= 10F;
                    continue;
                }
                for (String line : lines)
                {
                    ensureSpace(18F);
                    drawText(line, MARGIN, y, 11F, TEXT_COLOR);
                    y -= 18F;
                }
            }
            y -= 8F;
        }

        void writeEmptyBox(String text) throws IOException
        {
            ensureSpace(46F);
            content.setNonStrokingColor(new Color(248, 250, 252));
            content.addRect(MARGIN, y - 32F, CONTENT_WIDTH, 38F);
            content.fill();
            content.setStrokingColor(LINE_COLOR);
            content.addRect(MARGIN, y - 32F, CONTENT_WIDTH, 38F);
            content.stroke();
            drawText(text, MARGIN + 12F, y - 17F, 11F, MUTED_COLOR);
            y -= 54F;
        }

        void writeFinding(int index, AuditAiFinding finding) throws IOException
        {
            ensureSpace(76F);
            content.setNonStrokingColor(ISSUE_BG);
            content.addRect(MARGIN, y - 24F, CONTENT_WIDTH, 30F);
            content.fill();
            drawText("问题 " + index + "：" + value(finding.getFindingTitle()), MARGIN + 10F, y - 14F, 12F, TITLE_COLOR);
            y -= 36F;
            writeField("异常类型", value(finding.getFindingType()));
            if (finding.getPageNo() != null)
            {
                writeField("定位页码", "第" + finding.getPageNo() + "页");
            }
            writeField("问题描述", value(finding.getFindingContent()));
            if (StringUtils.isNotBlank(finding.getQuoteText()))
            {
                writeField("报告原文引用", finding.getQuoteText());
            }
            writeBasisReferences(issueBasisReferences(finding));
            y -= 8F;
        }

        private void writeBasisReferences(List<BasisReference> references) throws IOException
        {
            if (references == null || references.isEmpty())
            {
                writeField("审核依据引用", "暂无审核依据引用");
                return;
            }
            writeField("审核依据引用", "共" + references.size() + "条");
            for (int i = 0; i < references.size(); i++)
            {
                BasisReference reference = references.get(i);
                ensureSpace(22F);
                drawText("依据" + (i + 1), MARGIN + 78F, y, 10.5F, TITLE_COLOR);
                y -= 17F;
                writeBasisField("标准文件", reference.fileName);
                writeBasisField("版本", reference.versionNo);
                if (reference.pageNo != null)
                {
                    writeBasisField("页码", "第" + reference.pageNo + "页");
                }
                writeBasisField("章节", reference.sectionTitle);
                writeBasisField("条款/规则", reference.ruleCode);
                writeBasisField("依据原文", reference.quoteText);
                writeBasisField("依据冲突", reference.conflictDescription);
                y -= 4F;
            }
        }

        private void writeBasisField(String label, String value) throws IOException
        {
            if (StringUtils.isBlank(value))
            {
                return;
            }
            List<String> lines = wrap(value, 10F, CONTENT_WIDTH - 136F);
            for (int i = 0; i < lines.size(); i++)
            {
                ensureSpace(16F);
                if (i == 0)
                {
                    drawText(label + "：", MARGIN + 78F, y, 10F, MUTED_COLOR);
                }
                drawText(lines.get(i), MARGIN + 136F, y, 10F, TEXT_COLOR);
                y -= 16F;
            }
        }

        private void writeField(String label, String value) throws IOException
        {
            List<String> lines = wrap(value, 10.5F, CONTENT_WIDTH - 78F);
            if (lines.isEmpty())
            {
                lines.add("--");
            }
            for (int i = 0; i < lines.size(); i++)
            {
                ensureSpace(17F);
                if (i == 0)
                {
                    drawText(label + "：", MARGIN, y, 10.5F, MUTED_COLOR);
                }
                drawText(lines.get(i), MARGIN + 78F, y, 10.5F, TEXT_COLOR);
                y -= 17F;
            }
        }

        private void addPage() throws IOException
        {
            if (content != null)
            {
                content.close();
            }
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            content = new PDPageContentStream(document, page);
            y = PAGE_HEIGHT - MARGIN;
        }

        private void ensureSpace(float requiredHeight) throws IOException
        {
            if (y - requiredHeight < MARGIN)
            {
                addPage();
            }
        }

        private void drawText(String text, float x, float y, float fontSize, Color color) throws IOException
        {
            content.beginText();
            content.setNonStrokingColor(color);
            content.setFont(font, fontSize);
            content.newLineAtOffset(x, y);
            content.showText(clean(text));
            content.endText();
        }

        private void drawText(String text, float x, float y, float fontSize, Color color, float maxWidth) throws IOException
        {
            List<String> lines = wrap(text, fontSize, maxWidth);
            drawText(lines.isEmpty() ? "--" : lines.get(0), x, y, fontSize, color);
        }

        private void drawLine(float startX, float startY, float endX, float endY, Color color) throws IOException
        {
            content.setStrokingColor(color);
            content.moveTo(startX, startY);
            content.lineTo(endX, endY);
            content.stroke();
        }

        private List<String> wrap(String text, float fontSize, float maxWidth) throws IOException
        {
            List<String> lines = new ArrayList<>();
            String cleanText = clean(text);
            if (StringUtils.isBlank(cleanText))
            {
                return lines;
            }
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < cleanText.length(); i++)
            {
                char ch = cleanText.charAt(i);
                String candidate = line.toString() + ch;
                if (textWidth(candidate, fontSize) > maxWidth && line.length() > 0)
                {
                    lines.add(line.toString());
                    line.setLength(0);
                }
                line.append(ch);
            }
            if (line.length() > 0)
            {
                lines.add(line.toString());
            }
            return lines;
        }

        private float textWidth(String text, float fontSize) throws IOException
        {
            return font.getStringWidth(clean(text)) / 1000F * fontSize;
        }

        private String clean(String text)
        {
            if (text == null)
            {
                return "";
            }
            return text.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ').trim();
        }
    }
}
