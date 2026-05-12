package com.ruoyi.system.service.audit.vector.impl;

import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.config.VectorProperties;
import com.ruoyi.system.domain.audit.AuditCommonResource;
import com.ruoyi.system.domain.audit.vector.AuditVectorSearchHit;
import com.ruoyi.system.domain.audit.vector.AuditVectorSearchRequest;
import com.ruoyi.system.domain.audit.vector.AuditVectorSearchResult;
import com.ruoyi.system.domain.audit.vector.RerankResult;
import com.ruoyi.system.mapper.audit.AuditLibraryMapper;
import com.ruoyi.system.service.audit.vector.AuditVectorSearchService;
import com.ruoyi.system.service.audit.vector.EmbeddingClient;
import com.ruoyi.system.service.audit.vector.RerankClient;
import com.ruoyi.system.service.audit.vector.VectorStoreRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@ConditionalOnProperty(prefix = "vector", name = "enabled", havingValue = "true")
public class AuditVectorSearchServiceImpl implements AuditVectorSearchService
{
    private static final int DEFAULT_TOP_K = 8;

    private static final int MAX_TOP_K = 20;

    private static final int DEFAULT_MAX_CHUNK_CHARS = 1800;

    private static final int MAX_CHUNK_CHARS = 3000;

    private static final int MAX_RESOURCE_IDS = 200;

    private static final int MAX_QUERY_LENGTH = 1000;

    private static final Pattern RULE_CODE_PATTERN = Pattern.compile(
            "(?i)(?:第\\s*)?([A-Z]{1,10}-?\\d+(?:\\.\\d+)*|\\d+(?:\\.\\d+)+)(?:\\s*条)?");

    private static final Pattern DATE_AMOUNT_MODEL_PATTERN = Pattern.compile(
            "(\\d{4}[-/.年]\\d{1,2}(?:[-/.月]\\d{1,2}日?)?|\\d+(?:\\.\\d+)?\\s*(?:万元|元|%|％)|[A-Za-z]{1,8}[-_]?\\d{2,})");

    private final AuditLibraryMapper auditLibraryMapper;

    private final EmbeddingClient embeddingClient;

    private final VectorStoreRepository vectorStoreRepository;

    private final RerankClient rerankClient;

    private final VectorProperties vectorProperties;

    public AuditVectorSearchServiceImpl(AuditLibraryMapper auditLibraryMapper, EmbeddingClient embeddingClient,
            VectorStoreRepository vectorStoreRepository, RerankClient rerankClient, VectorProperties vectorProperties)
    {
        this.auditLibraryMapper = auditLibraryMapper;
        this.embeddingClient = embeddingClient;
        this.vectorStoreRepository = vectorStoreRepository;
        this.rerankClient = rerankClient;
        this.vectorProperties = vectorProperties;
    }

    @Override
    public AuditVectorSearchResult search(AuditVectorSearchRequest request, LoginUser loginUser)
    {
        SearchOptions options = validateAndNormalize(request);
        Set<Long> readableResourceIds = resolveReadableResourceIds(request);
        AuditVectorSearchResult result = new AuditVectorSearchResult();
        result.setQuery(options.query);
        result.setTopK(options.topK);
        if (readableResourceIds.isEmpty())
        {
            result.setTotal(0);
            result.setHits(new ArrayList<>());
            return result;
        }

        List<float[]> embeddings = embeddingClient.embed(Arrays.asList(options.query));
        if (embeddings.isEmpty())
        {
            throw new IllegalStateException("Embedding 返回为空");
        }
        List<AuditVectorSearchHit> hits = executeSearch(request, options, readableResourceIds, embeddings.get(0));
        result.setHits(hits);
        result.setTotal(hits.size());
        return result;
    }

    @Override
    public List<AuditVectorSearchResult> batchSearch(List<AuditVectorSearchRequest> requests, LoginUser loginUser)
    {
        if (requests == null || requests.isEmpty())
        {
            return new ArrayList<>();
        }

        List<SearchOptions> optionsList = new ArrayList<>();
        List<String> queries = new ArrayList<>();
        for (AuditVectorSearchRequest request : requests)
        {
            SearchOptions options = validateAndNormalize(request);
            optionsList.add(options);
            queries.add(options.query);
        }

        Set<Long> readableResourceIds = resolveReadableResourceIds(requests.get(0));
        List<AuditVectorSearchResult> results = new ArrayList<>();
        for (SearchOptions options : optionsList)
        {
            AuditVectorSearchResult result = new AuditVectorSearchResult();
            result.setQuery(options.query);
            result.setTopK(options.topK);
            result.setTotal(0);
            result.setHits(new ArrayList<>());
            results.add(result);
        }
        if (readableResourceIds.isEmpty())
        {
            return results;
        }

        List<float[]> embeddings = embeddingClient.embed(queries);
        if (embeddings.size() < requests.size())
        {
            throw new IllegalStateException("Embedding 返回数量不足");
        }
        for (int i = 0; i < requests.size(); i++)
        {
            AuditVectorSearchRequest request = requests.get(i);
            SearchOptions options = optionsList.get(i);
            List<AuditVectorSearchHit> hits = executeSearch(request, options, readableResourceIds, embeddings.get(i));
            AuditVectorSearchResult result = results.get(i);
            result.setHits(hits);
            result.setTotal(hits.size());
        }
        return results;
    }

    private SearchOptions validateAndNormalize(AuditVectorSearchRequest request)
    {
        if (request == null)
        {
            throw new IllegalArgumentException("检索请求不能为空");
        }
        String query = StringUtils.trim(request.getQuery());
        if (StringUtils.isBlank(query))
        {
            throw new IllegalArgumentException("query 不能为空");
        }
        if (query.length() > MAX_QUERY_LENGTH)
        {
            throw new IllegalArgumentException("query 长度不能超过 " + MAX_QUERY_LENGTH + " 字");
        }
        int topK = request.getTopK() == null ? DEFAULT_TOP_K : request.getTopK();
        if (topK <= 0)
        {
            topK = DEFAULT_TOP_K;
        }
        topK = Math.min(topK, resolveMaxTopK());

        int maxChunkChars = request.getMaxChunkChars() == null ? DEFAULT_MAX_CHUNK_CHARS : request.getMaxChunkChars();
        if (maxChunkChars <= 0)
        {
            maxChunkChars = DEFAULT_MAX_CHUNK_CHARS;
        }
        maxChunkChars = Math.min(maxChunkChars, MAX_CHUNK_CHARS);

        SearchOptions options = new SearchOptions();
        options.query = query;
        options.topK = topK;
        options.minScore = normalizeMinScore(request.getMinScore());
        options.includeChunkText = request.getIncludeChunkText() == null || request.getIncludeChunkText();
        options.maxChunkChars = maxChunkChars;
        options.hybrid = request.getHybrid() == null ? vectorProperties.getSearch().isHybridEnabled()
                : request.getHybrid();
        options.rerank = request.getRerank() == null || request.getRerank();
        options.rerankEnabled = options.rerank && rerankClient != null;
        options.rerankMaxDocumentChars = resolveRerankMaxDocumentChars();
        options.vectorCandidateK = resolveCandidateK(topK, vectorProperties.getSearch().getVectorCandidateMultiplier(),
                30);
        options.keywordCandidateK = resolveCandidateK(topK,
                vectorProperties.getSearch().getKeywordCandidateMultiplier(), 20);
        if (options.hybrid && options.minScore == null && vectorProperties.getSearch().getDefaultMinScore() > 0)
        {
            options.minScore = BigDecimal.valueOf(vectorProperties.getSearch().getDefaultMinScore());
        }
        return options;
    }

    private BigDecimal normalizeMinScore(BigDecimal minScore)
    {
        if (minScore == null)
        {
            return null;
        }
        if (minScore.compareTo(BigDecimal.ZERO) < 0 || minScore.compareTo(BigDecimal.ONE) > 0)
        {
            throw new IllegalArgumentException("minScore 必须在 0 到 1 之间");
        }
        return minScore;
    }

    private int resolveMaxTopK()
    {
        int configured = vectorProperties.getSearch().getMaxTopK();
        return configured > 0 ? configured : MAX_TOP_K;
    }

    private int resolveCandidateK(int topK, int multiplier, int minimum)
    {
        int safeMultiplier = Math.max(1, multiplier);
        int candidateK = Math.max(topK * safeMultiplier, minimum);
        int maxCandidateK = vectorProperties.getSearch().getMaxCandidateK();
        if (maxCandidateK > 0)
        {
            candidateK = Math.min(candidateK, maxCandidateK);
        }
        return Math.max(candidateK, topK);
    }

    private int resolveRerankMaxDocumentChars()
    {
        int configured = vectorProperties.getReranker().getMaxDocumentChars();
        if (configured <= 0)
        {
            return DEFAULT_MAX_CHUNK_CHARS;
        }
        return Math.min(configured, MAX_CHUNK_CHARS);
    }

    private List<AuditVectorSearchHit> executeSearch(AuditVectorSearchRequest request, SearchOptions options,
            Set<Long> readableResourceIds, float[] embedding)
    {
        boolean includeCandidateText = options.includeChunkText || options.rerankEnabled;
        if (!options.hybrid)
        {
            List<AuditVectorSearchHit> hits = vectorStoreRepository.searchChunks(embedding, readableResourceIds,
                    options.rerankEnabled ? options.vectorCandidateK : options.topK,
                    options.rerankEnabled ? null : options.minScore, includeCandidateText, options.maxChunkChars,
                    request.getKnowledgeBaseCodes(), request.getCategoryCodes(), request.getBusinessType(),
                    request.getEffectiveOnly(), request.getAsOfDate());
            for (AuditVectorSearchHit hit : hits)
            {
                hit.setVectorScore(hit.getScore());
                hit.setKeywordScore(BigDecimal.ZERO);
                hit.setMatchReason("vector");
            }
            enrichFolderName(hits);
            return finalizeCandidates(options, hits);
        }

        List<String> keywords = extractKeywords(options.query);
        List<AuditVectorSearchHit> vectorHits = vectorStoreRepository.searchChunks(embedding, readableResourceIds,
                options.vectorCandidateK, null, includeCandidateText, options.maxChunkChars,
                request.getKnowledgeBaseCodes(), request.getCategoryCodes(), request.getBusinessType(),
                request.getEffectiveOnly(), request.getAsOfDate());
        List<AuditVectorSearchHit> keywordHits = keywords.isEmpty() ? new ArrayList<>()
                : vectorStoreRepository.searchKeywordChunks(keywords, readableResourceIds, options.keywordCandidateK,
                        includeCandidateText, options.maxChunkChars, request.getKnowledgeBaseCodes(),
                        request.getCategoryCodes(), request.getBusinessType(), request.getEffectiveOnly(),
                        request.getAsOfDate());

        Map<String, AuditVectorSearchHit> candidates = new HashMap<>();
        for (AuditVectorSearchHit hit : vectorHits)
        {
            hit.setVectorScore(defaultScore(hit.getScore()));
            hit.setKeywordScore(BigDecimal.ZERO);
            hit.setMatchReason("vector");
            mergeCandidate(candidates, hit);
        }
        for (AuditVectorSearchHit hit : keywordHits)
        {
            KeywordScore keywordScore = calculateKeywordScore(hit, keywords);
            hit.setVectorScore(BigDecimal.ZERO);
            hit.setKeywordScore(keywordScore.score);
            hit.setMatchReason(keywordScore.reason);
            mergeCandidate(candidates, hit);
        }

        List<AuditVectorSearchHit> merged = new ArrayList<>(candidates.values());
        for (AuditVectorSearchHit hit : merged)
        {
            BigDecimal finalScore = calculateFinalScore(hit, options);
            hit.setScore(finalScore);
        }
        enrichFolderName(merged);
        return finalizeCandidates(options, merged);
    }

    private List<AuditVectorSearchHit> finalizeCandidates(SearchOptions options, List<AuditVectorSearchHit> candidates)
    {
        if (CollectionUtils.isEmpty(candidates))
        {
            return new ArrayList<>();
        }
        List<AuditVectorSearchHit> ranked = new ArrayList<>(candidates);
        if (options.rerankEnabled && ranked.size() > 1)
        {
            ranked = rerankCandidates(options, ranked);
        }
        else
        {
            ranked.sort(Comparator.comparing(AuditVectorSearchHit::getScore,
                    Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(hit -> hit.getChunkId() == null ? Long.MAX_VALUE : hit.getChunkId()));
            if (options.rerank)
            {
                for (AuditVectorSearchHit hit : ranked)
                {
                    hit.setRankScore(hit.getScore());
                }
            }
        }

        List<AuditVectorSearchHit> filtered = new ArrayList<>();
        for (AuditVectorSearchHit hit : ranked)
        {
            if (options.minScore != null && (hit.getScore() == null || hit.getScore().compareTo(options.minScore) < 0))
            {
                continue;
            }
            hit.setMetadata(withRetrievalMetadata(hit.getMetadata(), hit.getVectorScore(), hit.getKeywordScore(),
                    hit.getRankScore(), hit.getMatchReason()));
            if (!options.includeChunkText)
            {
                hit.setChunkText("");
            }
            filtered.add(hit);
            if (filtered.size() >= options.topK)
            {
                break;
            }
        }
        return filtered;
    }

    private List<AuditVectorSearchHit> rerankCandidates(SearchOptions options, List<AuditVectorSearchHit> candidates)
    {
        List<String> documents = new ArrayList<>();
        for (AuditVectorSearchHit hit : candidates)
        {
            documents.add(buildRerankDocument(hit, options.rerankMaxDocumentChars));
        }

        List<RerankResult> rerankResults = rerankClient.rerank(options.query, documents, candidates.size());
        if (CollectionUtils.isEmpty(rerankResults))
        {
            candidates.sort(Comparator.comparing(AuditVectorSearchHit::getScore,
                    Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(hit -> hit.getChunkId() == null ? Long.MAX_VALUE : hit.getChunkId()));
            for (AuditVectorSearchHit hit : candidates)
            {
                hit.setRankScore(hit.getScore());
            }
            return candidates;
        }

        Set<Integer> usedIndexes = new LinkedHashSet<>();
        List<AuditVectorSearchHit> ranked = new ArrayList<>();
        for (RerankResult result : rerankResults)
        {
            if (result == null || result.getIndex() == null)
            {
                continue;
            }
            int index = result.getIndex();
            if (index < 0 || index >= candidates.size() || !usedIndexes.add(index))
            {
                continue;
            }
            AuditVectorSearchHit hit = candidates.get(index);
            BigDecimal rankScore = normalizeRerankScore(result.getScore());
            hit.setRankScore(rankScore);
            hit.setScore(rankScore);
            hit.setMatchReason(mergeReason(hit.getMatchReason(), "rerank"));
            ranked.add(hit);
        }

        List<AuditVectorSearchHit> missing = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++)
        {
            if (!usedIndexes.contains(i))
            {
                AuditVectorSearchHit hit = candidates.get(i);
                hit.setRankScore(hit.getScore());
                missing.add(hit);
            }
        }
        missing.sort(Comparator.comparing(AuditVectorSearchHit::getScore,
                Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(hit -> hit.getChunkId() == null ? Long.MAX_VALUE : hit.getChunkId()));
        ranked.addAll(missing);
        return ranked;
    }

    private String buildRerankDocument(AuditVectorSearchHit hit, int maxChars)
    {
        StringBuilder builder = new StringBuilder();
        appendRerankField(builder, "文件", hit.getFileName());
        appendRerankField(builder, "章节", StringUtils.defaultIfBlank(hit.getSectionPath(), hit.getSectionTitle()));
        appendRerankField(builder, "条款", hit.getRuleCode());
        appendRerankField(builder, "正文", hit.getChunkText());
        String document = builder.toString();
        if (StringUtils.isBlank(document))
        {
            document = StringUtils.defaultString(hit.getFileName());
        }
        int safeMaxChars = Math.max(100, maxChars);
        return document.length() > safeMaxChars ? document.substring(0, safeMaxChars) : document;
    }

    private void appendRerankField(StringBuilder builder, String name, String value)
    {
        if (StringUtils.isBlank(value))
        {
            return;
        }
        if (builder.length() > 0)
        {
            builder.append('\n');
        }
        builder.append(name).append("：").append(value.trim());
    }

    private BigDecimal normalizeRerankScore(BigDecimal score)
    {
        if (score == null)
        {
            return BigDecimal.ZERO;
        }
        BigDecimal normalized = score;
        if (normalized.compareTo(BigDecimal.ZERO) < 0)
        {
            normalized = BigDecimal.ZERO;
        }
        if (normalized.compareTo(BigDecimal.ONE) > 0)
        {
            normalized = BigDecimal.ONE;
        }
        return normalized.setScale(6, RoundingMode.HALF_UP);
    }

    private void mergeCandidate(Map<String, AuditVectorSearchHit> candidates, AuditVectorSearchHit candidate)
    {
        String key = deduplicateKey(candidate);
        AuditVectorSearchHit existing = candidates.get(key);
        if (existing == null)
        {
            candidates.put(key, candidate);
            return;
        }
        existing.setVectorScore(maxScore(existing.getVectorScore(), candidate.getVectorScore()));
        existing.setKeywordScore(maxScore(existing.getKeywordScore(), candidate.getKeywordScore()));
        existing.setMatchReason(mergeReason(existing.getMatchReason(), candidate.getMatchReason()));
        if (StringUtils.isBlank(existing.getChunkText()) && StringUtils.isNotBlank(candidate.getChunkText()))
        {
            existing.setChunkText(candidate.getChunkText());
        }
    }

    private String deduplicateKey(AuditVectorSearchHit hit)
    {
        if (StringUtils.isNotBlank(hit.getChunkUid()))
        {
            return "uid:" + hit.getChunkUid();
        }
        if (hit.getDocumentId() != null && hit.getChunkNo() != null)
        {
            return "doc:" + hit.getDocumentId() + ":" + hit.getChunkNo();
        }
        if (StringUtils.isNotBlank(hit.getContentHash()))
        {
            return "hash:" + hit.getContentHash();
        }
        return "chunk:" + hit.getChunkId();
    }

    private BigDecimal calculateFinalScore(AuditVectorSearchHit hit, SearchOptions options)
    {
        double vectorScore = toDouble(hit.getVectorScore());
        double keywordScore = toDouble(hit.getKeywordScore());
        double finalScore = vectorScore * safeWeight(vectorProperties.getSearch().getVectorWeight(), 0.75D)
                + keywordScore * safeWeight(vectorProperties.getSearch().getKeywordWeight(), 0.25D)
                + metadataBoost(hit);
        return toScore(finalScore);
    }

    private double metadataBoost(AuditVectorSearchHit hit)
    {
        double boost = 0D;
        String reason = StringUtils.defaultString(hit.getMatchReason());
        if (reason.contains("rule_exact"))
        {
            boost += 0.15D;
        }
        if (reason.contains("section"))
        {
            boost += 0.08D;
        }
        if (reason.contains("file_name"))
        {
            boost += 0.05D;
        }
        if ("effective".equalsIgnoreCase(StringUtils.defaultString(hit.getStatus())))
        {
            boost += 0.03D;
        }
        return boost;
    }

    private double safeWeight(double value, double defaultValue)
    {
        return value < 0D ? defaultValue : value;
    }

    private List<String> extractKeywords(String query)
    {
        List<String> keywords = new ArrayList<>();
        addKeyword(keywords, query);
        Matcher ruleMatcher = RULE_CODE_PATTERN.matcher(query);
        while (ruleMatcher.find())
        {
            addKeyword(keywords, ruleMatcher.group(1));
        }
        Matcher valueMatcher = DATE_AMOUNT_MODEL_PATTERN.matcher(query);
        while (valueMatcher.find())
        {
            addKeyword(keywords, valueMatcher.group(1));
        }
        String[] parts = query.split("[\\s,，。；;：:、()（）\\[\\]【】]+");
        for (String part : parts)
        {
            addKeyword(keywords, part);
        }
        return keywords;
    }

    private void addKeyword(List<String> keywords, String value)
    {
        if (StringUtils.isBlank(value))
        {
            return;
        }
        String keyword = value.trim();
        if (keyword.length() < 2 || keyword.length() > 80)
        {
            return;
        }
        for (String existing : keywords)
        {
            if (existing.equalsIgnoreCase(keyword))
            {
                return;
            }
        }
        if (keywords.size() < 20)
        {
            keywords.add(keyword);
        }
    }

    private KeywordScore calculateKeywordScore(AuditVectorSearchHit hit, List<String> keywords)
    {
        double score = 0D;
        Set<String> reasons = new LinkedHashSet<>();
        for (String keyword : keywords)
        {
            String normalizedKeyword = normalizeText(keyword);
            if (normalizedKeyword.isEmpty())
            {
                continue;
            }
            if (equalsNormalized(hit.getRuleCode(), normalizedKeyword))
            {
                score = Math.max(score, 0.95D);
                reasons.add("rule_exact");
                continue;
            }
            if (containsNormalized(hit.getRuleCode(), normalizedKeyword))
            {
                score = Math.max(score, 0.85D);
                reasons.add("rule_code");
            }
            if (containsNormalized(hit.getSectionPath(), normalizedKeyword)
                    || containsNormalized(hit.getSectionTitle(), normalizedKeyword))
            {
                score = Math.max(score, 0.80D);
                reasons.add("section");
            }
            if (containsNormalized(hit.getFileName(), normalizedKeyword))
            {
                score = Math.max(score, 0.90D);
                reasons.add("file_name");
            }
            if (containsNormalized(hit.getChunkText(), normalizedKeyword))
            {
                score = Math.max(score, 0.55D);
                reasons.add("chunk_text");
            }
        }
        if (reasons.size() > 1)
        {
            score += Math.min(0.15D, (reasons.size() - 1) * 0.03D);
        }
        return new KeywordScore(toScore(score), reasons.isEmpty() ? "keyword" : String.join(",", reasons));
    }

    private String normalizeText(String value)
    {
        return StringUtils.defaultString(value).trim().toLowerCase(Locale.ROOT);
    }

    private boolean equalsNormalized(String value, String normalizedKeyword)
    {
        return normalizeText(value).equals(normalizedKeyword);
    }

    private boolean containsNormalized(String value, String normalizedKeyword)
    {
        return normalizeText(value).contains(normalizedKeyword);
    }

    private String mergeReason(String first, String second)
    {
        Set<String> reasons = new LinkedHashSet<>();
        addReasons(reasons, first);
        addReasons(reasons, second);
        return String.join(",", reasons);
    }

    private void addReasons(Set<String> reasons, String reason)
    {
        if (StringUtils.isBlank(reason))
        {
            return;
        }
        for (String item : reason.split(","))
        {
            if (StringUtils.isNotBlank(item))
            {
                reasons.add(item.trim());
            }
        }
    }

    private BigDecimal defaultScore(BigDecimal score)
    {
        return score == null ? BigDecimal.ZERO : score;
    }

    private BigDecimal maxScore(BigDecimal first, BigDecimal second)
    {
        if (first == null)
        {
            return defaultScore(second);
        }
        if (second == null)
        {
            return first;
        }
        return first.max(second);
    }

    private double toDouble(BigDecimal value)
    {
        return value == null ? 0D : value.doubleValue();
    }

    private BigDecimal toScore(double value)
    {
        double clamped = Math.max(0D, Math.min(1D, value));
        return BigDecimal.valueOf(clamped).setScale(6, RoundingMode.HALF_UP);
    }

    private String withRetrievalMetadata(String metadata, BigDecimal vectorScore, BigDecimal keywordScore,
            BigDecimal rankScore, String matchReason)
    {
        String retrieval = "\"retrieval\":{\"vectorScore\":\"" + defaultScore(vectorScore) + "\",\"keywordScore\":\""
                + defaultScore(keywordScore) + "\",\"matchReason\":\"" + escapeJson(matchReason) + "\""
                + (rankScore == null ? "" : ",\"rankScore\":\"" + rankScore + "\"") + "}";
        if (StringUtils.isBlank(metadata) || "{}".equals(metadata.trim()))
        {
            return "{" + retrieval + "}";
        }
        String trimmed = metadata.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}"))
        {
            return trimmed.substring(0, trimmed.length() - 1) + "," + retrieval + "}";
        }
        return "{" + retrieval + ",\"rawMetadata\":\"" + escapeJson(trimmed) + "\"}";
    }

    private String escapeJson(String value)
    {
        if (value == null)
        {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private Set<Long> resolveReadableResourceIds(AuditVectorSearchRequest request)
    {
        Set<Long> folderIds = normalizeFolderIds(request);
        Set<Long> scopedByResource = normalizeResourceIds(request.getResourceIds());
        boolean hasFolderScope = request.getFolderId() != null || request.getFolderIds() != null;
        boolean hasResourceScope = !scopedByResource.isEmpty() || request.getResourceIds() != null;
        Set<Long> scopedByFolder = resolveFolderResourceIds(folderIds);
        if (scopedByResource.size() > MAX_RESOURCE_IDS)
        {
            throw new IllegalArgumentException("resourceIds 数量不能超过 " + MAX_RESOURCE_IDS);
        }
        if (!scopedByResource.isEmpty())
        {
            scopedByResource = selectExistingResourceIds(scopedByResource);
        }
        if (!scopedByFolder.isEmpty() && !scopedByResource.isEmpty())
        {
            scopedByFolder.retainAll(scopedByResource);
            return scopedByFolder;
        }
        if (!scopedByFolder.isEmpty())
        {
            return scopedByFolder;
        }
        if (!scopedByResource.isEmpty())
        {
            return scopedByResource;
        }
        if (hasFolderScope || hasResourceScope)
        {
            return new LinkedHashSet<>();
        }
        return selectAllCommonResourceIds();
    }

    private Set<Long> resolveFolderResourceIds(Set<Long> folderIds)
    {
        if (folderIds.isEmpty())
        {
            return new LinkedHashSet<>();
        }
        Set<Long> resourceIds = new LinkedHashSet<>();
        for (Long folderId : folderIds)
        {
            AuditCommonResource query = new AuditCommonResource();
            query.setFolderId(folderId);
            for (AuditCommonResource resource : auditLibraryMapper.selectAuditCommonResourceList(query))
            {
                if (resource.getResourceId() != null)
                {
                    resourceIds.add(resource.getResourceId());
                }
            }
        }
        return resourceIds;
    }

    private Set<Long> normalizeFolderIds(AuditVectorSearchRequest request)
    {
        Set<Long> folderIds = new LinkedHashSet<>();
        if (request.getFolderId() != null)
        {
            folderIds.add(Math.max(0L, request.getFolderId()));
        }
        if (request.getFolderIds() != null)
        {
            for (Long folderId : request.getFolderIds())
            {
                if (folderId != null)
                {
                    folderIds.add(Math.max(0L, folderId));
                }
            }
        }
        return folderIds;
    }

    private Set<Long> normalizeResourceIds(Long[] resourceIds)
    {
        Set<Long> ids = new LinkedHashSet<>();
        if (resourceIds == null)
        {
            return ids;
        }
        for (Long resourceId : resourceIds)
        {
            if (resourceId != null && resourceId > 0)
            {
                ids.add(resourceId);
            }
        }
        return ids;
    }

    private Set<Long> selectExistingResourceIds(Collection<Long> resourceIds)
    {
        if (CollectionUtils.isEmpty(resourceIds))
        {
            return new LinkedHashSet<>();
        }
        AuditCommonResource query = new AuditCommonResource();
        query.setResourceIds(resourceIds.toArray(new Long[0]));
        Set<Long> existingIds = new LinkedHashSet<>();
        for (AuditCommonResource resource : auditLibraryMapper.selectAuditCommonResourceList(query))
        {
            if (resource.getResourceId() != null)
            {
                existingIds.add(resource.getResourceId());
            }
        }
        return existingIds;
    }

    private Set<Long> selectAllCommonResourceIds()
    {
        Set<Long> ids = new LinkedHashSet<>();
        for (AuditCommonResource resource : auditLibraryMapper.selectAuditCommonResourceList(new AuditCommonResource()))
        {
            if (resource.getResourceId() != null)
            {
                ids.add(resource.getResourceId());
            }
        }
        return ids;
    }

    private void enrichFolderName(List<AuditVectorSearchHit> hits)
    {
        if (CollectionUtils.isEmpty(hits))
        {
            return;
        }
        Set<Long> resourceIds = new LinkedHashSet<>();
        for (AuditVectorSearchHit hit : hits)
        {
            if (hit.getResourceId() != null)
            {
                resourceIds.add(hit.getResourceId());
            }
        }
        Map<Long, AuditCommonResource> resourceMap = new HashMap<>();
        AuditCommonResource query = new AuditCommonResource();
        query.setResourceIds(resourceIds.toArray(new Long[0]));
        for (AuditCommonResource resource : auditLibraryMapper.selectAuditCommonResourceList(query))
        {
            resourceMap.put(resource.getResourceId(), resource);
        }
        for (AuditVectorSearchHit hit : hits)
        {
            AuditCommonResource resource = resourceMap.get(hit.getResourceId());
            if (resource == null)
            {
                continue;
            }
            hit.setFolderName(resource.getFolderName());
            if (StringUtils.isBlank(hit.getFileName()))
            {
                hit.setFileName(resource.getFileName());
            }
            if (StringUtils.isBlank(hit.getFileUrl()))
            {
                hit.setFileUrl(resource.getFileUrl());
            }
        }
    }

    private static class SearchOptions
    {
        private String query;

        private int topK;

        private int vectorCandidateK;

        private int keywordCandidateK;

        private BigDecimal minScore;

        private boolean includeChunkText;

        private int maxChunkChars;

        private boolean hybrid;

        private boolean rerank;

        private boolean rerankEnabled;

        private int rerankMaxDocumentChars;
    }

    private static class KeywordScore
    {
        private final BigDecimal score;

        private final String reason;

        private KeywordScore(BigDecimal score, String reason)
        {
            this.score = score;
            this.reason = reason;
        }
    }
}
