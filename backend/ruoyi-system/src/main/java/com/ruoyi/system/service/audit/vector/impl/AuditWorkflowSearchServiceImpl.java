package com.ruoyi.system.service.audit.vector.impl;

import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.audit.vector.AuditKnowledgeScope;
import com.ruoyi.system.domain.audit.vector.AuditRetrievalConfig;
import com.ruoyi.system.domain.audit.vector.AuditVectorSearchLog;
import com.ruoyi.system.domain.audit.vector.AuditVectorSearchHit;
import com.ruoyi.system.domain.audit.vector.AuditVectorSearchRequest;
import com.ruoyi.system.domain.audit.vector.AuditVectorSearchResult;
import com.ruoyi.system.domain.audit.vector.AuditWorkflowBatchQuery;
import com.ruoyi.system.domain.audit.vector.AuditWorkflowBatchQueryResult;
import com.ruoyi.system.domain.audit.vector.AuditWorkflowBatchSearchData;
import com.ruoyi.system.domain.audit.vector.AuditWorkflowBatchSearchRequest;
import com.ruoyi.system.domain.audit.vector.AuditWorkflowBatchSearchResponse;
import com.ruoyi.system.domain.audit.vector.AuditWorkflowCallerContext;
import com.ruoyi.system.domain.audit.vector.AuditWorkflowSearchData;
import com.ruoyi.system.domain.audit.vector.AuditWorkflowSearchHit;
import com.ruoyi.system.domain.audit.vector.AuditWorkflowSearchRequest;
import com.ruoyi.system.domain.audit.vector.AuditWorkflowSearchResponse;
import com.ruoyi.system.mapper.audit.AuditVectorSearchLogMapper;
import com.ruoyi.system.service.audit.vector.AuditVectorSearchService;
import com.ruoyi.system.service.audit.vector.AuditWorkflowSearchService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "vector", name = "enabled", havingValue = "true")
public class AuditWorkflowSearchServiceImpl implements AuditWorkflowSearchService
{
    private static final Logger log = LoggerFactory.getLogger(AuditWorkflowSearchServiceImpl.class);

    public static final String ERROR_KB_UNAVAILABLE = "KB_UNAVAILABLE";

    public static final String ERROR_KB_PERMISSION_DENIED = "KB_PERMISSION_DENIED";

    public static final String ERROR_KB_SCOPE_EMPTY = "KB_SCOPE_EMPTY";

    public static final String ERROR_KB_NO_RESULT = "KB_NO_RESULT";

    public static final String ERROR_KB_BAD_REQUEST = "KB_BAD_REQUEST";

    private static final int MAX_BATCH_QUERY_COUNT = 20;

    private static final int MAX_BATCH_TOTAL_HITS = 200;

    private static final int MAX_WORKFLOW_QUERY_LENGTH = 8000;

    private static final int MAX_VECTOR_QUERY_LENGTH = 1000;

    private final AuditVectorSearchService auditVectorSearchService;

    private final AuditVectorSearchLogMapper auditVectorSearchLogMapper;

    public AuditWorkflowSearchServiceImpl(AuditVectorSearchService auditVectorSearchService,
            AuditVectorSearchLogMapper auditVectorSearchLogMapper)
    {
        this.auditVectorSearchService = auditVectorSearchService;
        this.auditVectorSearchLogMapper = auditVectorSearchLogMapper;
    }

    @Override
    public AuditWorkflowSearchResponse search(AuditWorkflowSearchRequest request, LoginUser loginUser)
    {
        return searchInternal(request, loginUser, true);
    }

    @Override
    public AuditWorkflowBatchSearchResponse batchSearch(AuditWorkflowBatchSearchRequest request, LoginUser loginUser)
    {
        long startMs = System.currentTimeMillis();
        AuditWorkflowBatchSearchResponse response = null;
        try
        {
            validateBatchRequest(request);
            String permissionMode = resolvePermissionMode(request);
            PermissionDecision permissionDecision = resolvePermission(permissionMode,
                    request == null ? null : request.getCallerContext(), loginUser);
            if (!permissionDecision.allowed)
            {
                response = AuditWorkflowBatchSearchResponse.error(request,
                        AuditWorkflowSearchResponse.CODE_PERMISSION_DENIED, ERROR_KB_PERMISSION_DENIED,
                        permissionDecision.message);
                return response;
            }

            if (!permissionDecision.allowEmptyScope && !hasExplicitScope(request.getKnowledgeScope()))
            {
                response = AuditWorkflowBatchSearchResponse.success(request,
                        toBatchEmptyData(request, ERROR_KB_SCOPE_EMPTY, "知识库范围为空"));
                response.setErrorCode(ERROR_KB_SCOPE_EMPTY);
                response.setMessage("知识库范围为空");
                return response;
            }

            AuditWorkflowBatchSearchData data = new AuditWorkflowBatchSearchData();
            List<AuditWorkflowSearchRequest> singleRequests = new ArrayList<>();
            List<AuditVectorSearchRequest> vectorRequests = new ArrayList<>();
            for (AuditWorkflowBatchQuery query : request.getQueries())
            {
                AuditWorkflowSearchRequest singleRequest = toSearchRequest(request, query);
                singleRequests.add(singleRequest);
                vectorRequests.add(toVectorSearchRequest(singleRequest));
            }

            List<AuditVectorSearchResult> searchResults = auditVectorSearchService.batchSearch(vectorRequests,
                    loginUser);
            if (searchResults.size() < singleRequests.size())
            {
                throw new IllegalStateException("批量检索返回数量不足");
            }

            List<AuditWorkflowBatchQueryResult> results = new ArrayList<>();
            int totalHits = 0;
            for (int i = 0; i < request.getQueries().size(); i++)
            {
                AuditWorkflowBatchQuery query = request.getQueries().get(i);
                AuditWorkflowSearchData workflowData = toWorkflowData(singleRequests.get(i), searchResults.get(i));
                AuditWorkflowSearchResponse singleResponse = AuditWorkflowSearchResponse.success(singleRequests.get(i),
                        workflowData);
                if (workflowData.getTotal() == null || workflowData.getTotal() == 0)
                {
                    singleResponse.setErrorCode(ERROR_KB_NO_RESULT);
                    singleResponse.setMessage("没有召回结果");
                }

                AuditWorkflowBatchQueryResult queryResult = toBatchQueryResult(query, singleResponse);
                List<AuditWorkflowSearchHit> hits = queryResult.getHits();
                if (hits != null && hits.size() > 0)
                {
                    int remaining = MAX_BATCH_TOTAL_HITS - totalHits;
                    if (remaining <= 0)
                    {
                        queryResult.setHits(new ArrayList<>());
                    }
                    else if (hits.size() > remaining)
                    {
                        queryResult.setHits(new ArrayList<>(hits.subList(0, remaining)));
                        totalHits = MAX_BATCH_TOTAL_HITS;
                    }
                    else
                    {
                        totalHits += hits.size();
                    }
                }
                results.add(queryResult);
            }

            data.setTotalQueries(request.getQueries().size());
            data.setTotalHits(totalHits);
            data.setResults(results);
            response = AuditWorkflowBatchSearchResponse.success(request, data);
            return response;
        }
        catch (IllegalArgumentException e)
        {
            response = AuditWorkflowBatchSearchResponse.error(request, AuditWorkflowSearchResponse.CODE_BAD_REQUEST,
                    ERROR_KB_BAD_REQUEST, e.getMessage());
            return response;
        }
        catch (RuntimeException e)
        {
            response = AuditWorkflowBatchSearchResponse.error(request, AuditWorkflowSearchResponse.CODE_UNAVAILABLE,
                    ERROR_KB_UNAVAILABLE, safeError(e.getMessage()));
            return response;
        }
        finally
        {
            writeBatchSearchLog(request, response, loginUser, System.currentTimeMillis() - startMs);
        }
    }

    private AuditWorkflowSearchResponse searchInternal(AuditWorkflowSearchRequest request, LoginUser loginUser,
            boolean writeLog)
    {
        long startMs = System.currentTimeMillis();
        AuditWorkflowSearchResponse response = null;
        try
        {
            validateRequest(request);
            String permissionMode = resolvePermissionMode(request);
            PermissionDecision permissionDecision = resolvePermission(permissionMode,
                    request == null ? null : request.getCallerContext(), loginUser);
            if (!permissionDecision.allowed)
            {
                response = AuditWorkflowSearchResponse.error(request, AuditWorkflowSearchResponse.CODE_PERMISSION_DENIED,
                        ERROR_KB_PERMISSION_DENIED, permissionDecision.message);
                return response;
            }
            if (!permissionDecision.allowEmptyScope && !hasExplicitScope(request.getKnowledgeScope()))
            {
                response = successWithEmptyData(request, ERROR_KB_SCOPE_EMPTY, "知识库范围为空");
                return response;
            }

            AuditVectorSearchResult searchResult = auditVectorSearchService.search(toVectorSearchRequest(request),
                    loginUser);
            AuditWorkflowSearchData data = toWorkflowData(request, searchResult);
            response = AuditWorkflowSearchResponse.success(request, data);
            if (data.getTotal() == null || data.getTotal() == 0)
            {
                response.setErrorCode(ERROR_KB_NO_RESULT);
                response.setMessage("没有召回结果");
            }
            return response;
        }
        catch (IllegalArgumentException e)
        {
            response = AuditWorkflowSearchResponse.error(request, AuditWorkflowSearchResponse.CODE_BAD_REQUEST,
                    ERROR_KB_BAD_REQUEST, e.getMessage());
            return response;
        }
        catch (RuntimeException e)
        {
            response = AuditWorkflowSearchResponse.error(request, AuditWorkflowSearchResponse.CODE_UNAVAILABLE,
                    ERROR_KB_UNAVAILABLE, safeError(e.getMessage()));
            return response;
        }
        finally
        {
            if (writeLog)
            {
                writeSearchLog(request, response, loginUser, System.currentTimeMillis() - startMs);
            }
        }
    }

    private void validateRequest(AuditWorkflowSearchRequest request)
    {
        if (request == null)
        {
            throw new IllegalArgumentException("检索请求不能为空");
        }
        if (StringUtils.isBlank(request.getQuery()))
        {
            throw new IllegalArgumentException("query 不能为空");
        }
        if (request.getQuery().length() > MAX_WORKFLOW_QUERY_LENGTH)
        {
            throw new IllegalArgumentException("query 长度不能超过 " + MAX_WORKFLOW_QUERY_LENGTH + " 个字符");
        }
    }

    private void validateBatchRequest(AuditWorkflowBatchSearchRequest request)
    {
        if (request == null)
        {
            throw new IllegalArgumentException("批量检索请求不能为空");
        }
        if (request.getQueries() == null || request.getQueries().isEmpty())
        {
            throw new IllegalArgumentException("queries 不能为空");
        }
        if (request.getQueries().size() > MAX_BATCH_QUERY_COUNT)
        {
            throw new IllegalArgumentException("单次批量检索 query 数不能超过 " + MAX_BATCH_QUERY_COUNT);
        }
        Set<String> queryIds = new LinkedHashSet<>();
        int index = 0;
        for (AuditWorkflowBatchQuery query : request.getQueries())
        {
            index++;
            if (query == null)
            {
                throw new IllegalArgumentException("queries[" + index + "] 不能为空");
            }
            if (StringUtils.isBlank(query.getQueryId()))
            {
                throw new IllegalArgumentException("queries[" + index + "].queryId 不能为空");
            }
            if (!queryIds.add(query.getQueryId()))
            {
                throw new IllegalArgumentException("queryId 不能重复：" + query.getQueryId());
            }
            if (StringUtils.isBlank(query.getQuery()))
            {
                throw new IllegalArgumentException("queries[" + index + "].query 不能为空");
            }
            if (query.getQuery().length() > MAX_WORKFLOW_QUERY_LENGTH)
            {
                throw new IllegalArgumentException("queries[" + index + "].query 长度不能超过 "
                        + MAX_WORKFLOW_QUERY_LENGTH + " 个字符");
            }
        }
    }

    private String resolvePermissionMode(AuditWorkflowSearchRequest request)
    {
        return resolvePermissionMode(request == null ? null : request.getCallerContext());
    }

    private String resolvePermissionMode(AuditWorkflowBatchSearchRequest request)
    {
        return resolvePermissionMode(request == null ? null : request.getCallerContext());
    }

    private String resolvePermissionMode(AuditWorkflowCallerContext context)
    {
        if (context == null || StringUtils.isBlank(context.getPermissionMode()))
        {
            return AuditWorkflowCallerContext.PERMISSION_MODE_EXPLICIT_SCOPE;
        }
        return context.getPermissionMode();
    }

    private PermissionDecision resolvePermission(String permissionMode, AuditWorkflowCallerContext context,
            LoginUser loginUser)
    {
        if (AuditWorkflowCallerContext.PERMISSION_MODE_EXPLICIT_SCOPE.equals(permissionMode))
        {
            return PermissionDecision.allowed(false);
        }
        if (AuditWorkflowCallerContext.PERMISSION_MODE_WORKFLOW_SERVICE.equals(permissionMode))
        {
            return PermissionDecision.allowed(false);
        }
        if (AuditWorkflowCallerContext.PERMISSION_MODE_CALLER_USER.equals(permissionMode))
        {
            if (context != null && StringUtils.isNotBlank(context.getUserId()) && loginUser != null
                    && loginUser.getUserId() != null && !context.getUserId().equals(String.valueOf(loginUser.getUserId()))
                    && !SecurityUtils.isAdmin(loginUser.getUserId()))
            {
                return PermissionDecision.denied("caller_user 与当前登录用户不一致");
            }
            return PermissionDecision.allowed(loginUser != null && SecurityUtils.isAdmin(loginUser.getUserId()));
        }
        return PermissionDecision.denied("不支持的权限模式：" + permissionMode);
    }

    private boolean hasExplicitScope(AuditKnowledgeScope scope)
    {
        if (scope == null)
        {
            return false;
        }
        return hasValues(scope.getFolderIds()) || hasValues(scope.getResourceIds())
                || hasTextValues(scope.getKnowledgeBaseCodes()) || hasTextValues(scope.getCategoryCodes())
                || StringUtils.isNotBlank(scope.getBusinessType());
    }

    private boolean hasValues(Long[] values)
    {
        if (values == null || values.length == 0)
        {
            return false;
        }
        for (Long value : values)
        {
            if (value != null)
            {
                return true;
            }
        }
        return false;
    }

    private boolean hasTextValues(String[] values)
    {
        if (values == null || values.length == 0)
        {
            return false;
        }
        for (String value : values)
        {
            if (StringUtils.isNotBlank(value))
            {
                return true;
            }
        }
        return false;
    }

    private AuditWorkflowSearchRequest toSearchRequest(AuditWorkflowBatchSearchRequest request,
            AuditWorkflowBatchQuery query)
    {
        AuditWorkflowSearchRequest searchRequest = new AuditWorkflowSearchRequest();
        searchRequest.setRequestId(request.getRequestId());
        searchRequest.setWorkflowCode(request.getWorkflowCode());
        searchRequest.setTaskId(request.getTaskId());
        searchRequest.setKnowledgeScope(request.getKnowledgeScope());
        searchRequest.setCallerContext(request.getCallerContext());
        searchRequest.setRetrievalConfig(request.getRetrievalConfig());
        if (query != null)
        {
            searchRequest.setQuery(query.getQuery());
            searchRequest.setQueryType(query.getQueryType());
        }
        return searchRequest;
    }

    private AuditVectorSearchRequest toVectorSearchRequest(AuditWorkflowSearchRequest request)
    {
        AuditVectorSearchRequest searchRequest = new AuditVectorSearchRequest();
        searchRequest.setQuery(toVectorQuery(request.getQuery()));
        AuditKnowledgeScope scope = request.getKnowledgeScope();
        if (scope != null)
        {
            searchRequest.setFolderIds(scope.getFolderIds());
            searchRequest.setResourceIds(scope.getResourceIds());
            searchRequest.setKnowledgeBaseCodes(scope.getKnowledgeBaseCodes());
            searchRequest.setCategoryCodes(scope.getCategoryCodes());
            searchRequest.setBusinessType(scope.getBusinessType());
            searchRequest.setEffectiveOnly(scope.getEffectiveOnly());
            searchRequest.setAsOfDate(scope.getAsOfDate());
        }
        AuditRetrievalConfig config = request.getRetrievalConfig();
        if (config != null)
        {
            searchRequest.setTopK(config.getTopK());
            searchRequest.setMinScore(config.getMinScore());
            searchRequest.setHybrid(config.getHybrid());
            searchRequest.setRerank(config.getRerank());
            searchRequest.setIncludeChunkText(config.getIncludeChunkText());
            searchRequest.setMaxChunkChars(config.getMaxChunkChars());
        }
        return searchRequest;
    }

    private AuditWorkflowBatchSearchData toBatchEmptyData(AuditWorkflowBatchSearchRequest request, String errorCode,
            String errorMessage)
    {
        AuditWorkflowBatchSearchData data = new AuditWorkflowBatchSearchData();
        List<AuditWorkflowBatchQueryResult> results = new ArrayList<>();
        if (request != null && request.getQueries() != null)
        {
            for (AuditWorkflowBatchQuery query : request.getQueries())
            {
                AuditWorkflowBatchQueryResult result = new AuditWorkflowBatchQueryResult();
                if (query != null)
                {
                    result.setQueryId(query.getQueryId());
                    result.setSourceChunkId(query.getSourceChunkId());
                    result.setQuery(query.getQuery());
                    result.setQueryType(query.getQueryType());
                }
                result.setErrorCode(errorCode);
                result.setErrorMessage(errorMessage);
                result.setHits(new ArrayList<>());
                results.add(result);
            }
        }
        data.setTotalQueries(results.size());
        data.setTotalHits(0);
        data.setResults(results);
        return data;
    }

    private String toVectorQuery(String query)
    {
        String text = StringUtils.trim(query);
        if (text == null || text.length() <= MAX_VECTOR_QUERY_LENGTH)
        {
            return text;
        }
        return text.substring(0, MAX_VECTOR_QUERY_LENGTH);
    }

    private AuditWorkflowBatchQueryResult toBatchQueryResult(AuditWorkflowBatchQuery query,
            AuditWorkflowSearchResponse response)
    {
        AuditWorkflowBatchQueryResult result = new AuditWorkflowBatchQueryResult();
        result.setQueryId(query.getQueryId());
        result.setSourceChunkId(query.getSourceChunkId());
        result.setQuery(query.getQuery());
        result.setQueryType(query.getQueryType());
        if (response == null)
        {
            result.setErrorCode(ERROR_KB_UNAVAILABLE);
            result.setErrorMessage("知识库服务不可用");
        }
        else if (StringUtils.isNotBlank(response.getErrorCode())
                || response.getCode() != AuditWorkflowSearchResponse.CODE_SUCCESS)
        {
            result.setErrorCode(response.getErrorCode());
            result.setErrorMessage(response.getMessage());
        }
        if (response != null && response.getData() != null && response.getData().getResults() != null)
        {
            result.setHits(response.getData().getResults());
        }
        else
        {
            result.setHits(new ArrayList<>());
        }
        return result;
    }

    private AuditWorkflowSearchResponse successWithEmptyData(AuditWorkflowSearchRequest request, String errorCode,
            String message)
    {
        AuditWorkflowSearchData data = new AuditWorkflowSearchData();
        data.setQuery(request == null ? null : request.getQuery());
        data.setQueryType(request == null ? null : request.getQueryType());
        data.setTotal(0);
        data.setResults(new ArrayList<>());
        data.setIgnoredFilters(resolveIgnoredFilters(request));
        AuditWorkflowSearchResponse response = AuditWorkflowSearchResponse.success(request, data);
        response.setErrorCode(errorCode);
        response.setMessage(message);
        return response;
    }

    private AuditWorkflowSearchData toWorkflowData(AuditWorkflowSearchRequest request, AuditVectorSearchResult result)
    {
        AuditWorkflowSearchData data = new AuditWorkflowSearchData();
        data.setQuery(request.getQuery());
        data.setQueryType(request.getQueryType());
        List<AuditWorkflowSearchHit> hits = new ArrayList<>();
        if (result != null && result.getHits() != null)
        {
            for (AuditVectorSearchHit hit : result.getHits())
            {
                hits.add(toWorkflowHit(hit));
            }
        }
        data.setResults(hits);
        data.setTotal(hits.size());
        data.setIgnoredFilters(resolveIgnoredFilters(request));
        return data;
    }

    private List<String> resolveIgnoredFilters(AuditWorkflowSearchRequest request)
    {
        List<String> ignoredFilters = new ArrayList<>();
        if (request == null)
        {
            return ignoredFilters;
        }
        AuditKnowledgeScope scope = request.getKnowledgeScope();
        if (scope != null)
        {
            // Stage 3 has implemented knowledgeBaseCodes/categoryCodes/businessType/effectiveOnly/asOfDate.
        }
        return ignoredFilters;
    }

    private AuditWorkflowSearchHit toWorkflowHit(AuditVectorSearchHit hit)
    {
        AuditWorkflowSearchHit workflowHit = new AuditWorkflowSearchHit();
        workflowHit.setChunkId(toString(hit.getChunkId()));
        workflowHit.setChunkUid(StringUtils.defaultIfBlank(hit.getChunkUid(),
                hit.getChunkId() == null ? "" : "KB-CHUNK-" + hit.getChunkId()));
        workflowHit.setDocumentId(toString(hit.getDocumentId()));
        workflowHit.setResourceId(hit.getResourceId());
        workflowHit.setResourceType(hit.getResourceType());
        workflowHit.setFileName(hit.getFileName());
        workflowHit.setFileUrl(hit.getFileUrl());
        workflowHit.setFileHash(hit.getFileHash());
        workflowHit.setVersionNo(hit.getVersionNo());
        workflowHit.setFolderId(hit.getFolderId());
        workflowHit.setFolderName(hit.getFolderName());
        workflowHit.setPageNo(hit.getPageNo());
        workflowHit.setSectionTitle(hit.getSectionTitle());
        workflowHit.setSectionPath(StringUtils.defaultIfBlank(hit.getSectionPath(), hit.getSectionTitle()));
        workflowHit.setChunkNo(hit.getChunkNo());
        workflowHit.setChunkText(hit.getChunkText());
        workflowHit.setScore(hit.getScore());
        workflowHit.setRankScore(hit.getRankScore());
        workflowHit.setStatus(StringUtils.defaultIfBlank(hit.getStatus(), "effective"));
        workflowHit.setEffectiveDate(hit.getEffectiveDate());
        workflowHit.setExpireDate(hit.getExpireDate());
        workflowHit.setKnowledgeBaseCode(StringUtils.defaultIfBlank(hit.getKnowledgeBaseCode(), "default"));
        workflowHit.setCategoryCode(StringUtils.defaultString(hit.getCategoryCode()));
        workflowHit.setBusinessType(StringUtils.defaultString(hit.getBusinessType()));
        workflowHit.setOwnerDeptId(StringUtils.defaultString(hit.getOwnerDeptId()));
        workflowHit.setRuleCode(StringUtils.defaultString(hit.getRuleCode()));
        workflowHit.setParagraphNo(hit.getParagraphNo());
        workflowHit.setContentHash(StringUtils.defaultString(hit.getContentHash()));
        workflowHit.setMetadata(StringUtils.defaultIfBlank(hit.getMetadata(), "{}"));
        return workflowHit;
    }

    private String toString(Long value)
    {
        return value == null ? "" : value.toString();
    }

    private String safeError(String message)
    {
        if (StringUtils.isBlank(message))
        {
            return "知识库服务不可用";
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    private void writeSearchLog(AuditWorkflowSearchRequest request, AuditWorkflowSearchResponse response,
            LoginUser loginUser, long costMs)
    {
        try
        {
            AuditVectorSearchLog searchLog = new AuditVectorSearchLog();
            searchLog.setRequestId(request == null ? "" : safeText(request.getRequestId(), 100));
            searchLog.setWorkflowCode(request == null ? "" : safeText(request.getWorkflowCode(), 100));
            searchLog.setTaskId(request == null ? "" : safeText(request.getTaskId(), 100));
            searchLog.setQueryCount(1);
            searchLog.setPermissionMode(safeText(resolvePermissionMode(request), 32));
            searchLog.setScopeSummary(safeText(summaryScope(request == null ? null : request.getKnowledgeScope()),
                    1000));
            searchLog.setRetrievalConfig(safeText(summaryRetrievalConfig(
                    request == null ? null : request.getRetrievalConfig()), 1000));
            searchLog.setResultCount(countHits(response));
            searchLog.setTopResourceIds(safeText(topResourceIds(response), 1000));
            searchLog.setStatus(response != null && response.getCode() == AuditWorkflowSearchResponse.CODE_SUCCESS
                    ? "success" : "failed");
            searchLog.setErrorCode(response == null ? ERROR_KB_UNAVAILABLE : safeText(response.getErrorCode(), 64));
            searchLog.setErrorMsg(response == null ? "知识库服务不可用" : safeText(response.getMessage(), 500));
            searchLog.setCostMs(costMs);
            searchLog.setCreateBy(resolveCreateBy(loginUser));
            auditVectorSearchLogMapper.insertAuditVectorSearchLog(searchLog);
        }
        catch (RuntimeException e)
        {
            log.warn("写入审核知识库检索日志失败", e);
        }
    }

    private void writeBatchSearchLog(AuditWorkflowBatchSearchRequest request, AuditWorkflowBatchSearchResponse response,
            LoginUser loginUser, long costMs)
    {
        try
        {
            AuditVectorSearchLog searchLog = new AuditVectorSearchLog();
            searchLog.setRequestId(request == null ? "" : safeText(request.getRequestId(), 100));
            searchLog.setWorkflowCode(request == null ? "" : safeText(request.getWorkflowCode(), 100));
            searchLog.setTaskId(request == null ? "" : safeText(request.getTaskId(), 100));
            searchLog.setQueryCount(request == null || request.getQueries() == null ? 0 : request.getQueries().size());
            searchLog.setPermissionMode(safeText(resolvePermissionMode(request), 32));
            searchLog.setScopeSummary(safeText(summaryScope(request == null ? null : request.getKnowledgeScope()),
                    1000));
            searchLog.setRetrievalConfig(safeText(summaryRetrievalConfig(
                    request == null ? null : request.getRetrievalConfig()), 1000));
            searchLog.setResultCount(countHits(response));
            searchLog.setTopResourceIds(safeText(topResourceIds(response), 1000));
            searchLog.setStatus(response != null && response.getCode() == AuditWorkflowSearchResponse.CODE_SUCCESS
                    ? "success" : "failed");
            searchLog.setErrorCode(response == null ? ERROR_KB_UNAVAILABLE : safeText(resolveBatchErrorCode(response),
                    64));
            searchLog.setErrorMsg(response == null ? "知识库服务不可用" : safeText(response.getMessage(), 500));
            searchLog.setCostMs(costMs);
            searchLog.setCreateBy(resolveCreateBy(loginUser));
            auditVectorSearchLogMapper.insertAuditVectorSearchLog(searchLog);
        }
        catch (RuntimeException e)
        {
            log.warn("写入审核知识库批量检索日志失败", e);
        }
    }

    private int countHits(AuditWorkflowSearchResponse response)
    {
        if (response == null || response.getData() == null || response.getData().getResults() == null)
        {
            return 0;
        }
        return response.getData().getResults().size();
    }

    private int countHits(AuditWorkflowBatchSearchResponse response)
    {
        if (response == null || response.getData() == null || response.getData().getResults() == null)
        {
            return 0;
        }
        int total = 0;
        for (AuditWorkflowBatchQueryResult result : response.getData().getResults())
        {
            if (result != null && result.getHits() != null)
            {
                total += result.getHits().size();
            }
        }
        return total;
    }

    private String topResourceIds(AuditWorkflowSearchResponse response)
    {
        if (response == null || response.getData() == null)
        {
            return "";
        }
        return topResourceIds(response.getData().getResults());
    }

    private String topResourceIds(AuditWorkflowBatchSearchResponse response)
    {
        if (response == null || response.getData() == null || response.getData().getResults() == null)
        {
            return "";
        }
        List<AuditWorkflowSearchHit> hits = new ArrayList<>();
        for (AuditWorkflowBatchQueryResult result : response.getData().getResults())
        {
            if (result != null && result.getHits() != null)
            {
                hits.addAll(result.getHits());
            }
        }
        return topResourceIds(hits);
    }

    private String topResourceIds(List<AuditWorkflowSearchHit> hits)
    {
        if (hits == null || hits.isEmpty())
        {
            return "";
        }
        Set<Long> resourceIds = new LinkedHashSet<>();
        for (AuditWorkflowSearchHit hit : hits)
        {
            if (hit != null && hit.getResourceId() != null)
            {
                resourceIds.add(hit.getResourceId());
            }
            if (resourceIds.size() >= 50)
            {
                break;
            }
        }
        StringBuilder builder = new StringBuilder();
        for (Long resourceId : resourceIds)
        {
            if (builder.length() > 0)
            {
                builder.append(',');
            }
            builder.append(resourceId);
        }
        return builder.toString();
    }

    private String resolveBatchErrorCode(AuditWorkflowBatchSearchResponse response)
    {
        if (response == null)
        {
            return ERROR_KB_UNAVAILABLE;
        }
        if (StringUtils.isNotBlank(response.getErrorCode()))
        {
            return response.getErrorCode();
        }
        if (response.getData() != null && response.getData().getResults() != null)
        {
            for (AuditWorkflowBatchQueryResult result : response.getData().getResults())
            {
                if (result != null && StringUtils.isNotBlank(result.getErrorCode()))
                {
                    return result.getErrorCode();
                }
            }
        }
        return "";
    }

    private String summaryScope(AuditKnowledgeScope scope)
    {
        if (scope == null)
        {
            return "";
        }
        return "folderIds=" + join(scope.getFolderIds()) + ";resourceIds=" + join(scope.getResourceIds())
                + ";knowledgeBaseCodes=" + join(scope.getKnowledgeBaseCodes()) + ";categoryCodes="
                + join(scope.getCategoryCodes()) + ";businessType=" + StringUtils.defaultString(scope.getBusinessType())
                + ";effectiveOnly=" + scope.getEffectiveOnly() + ";asOfDate="
                + StringUtils.defaultString(scope.getAsOfDate());
    }

    private String summaryRetrievalConfig(AuditRetrievalConfig config)
    {
        if (config == null)
        {
            return "";
        }
        return "topK=" + config.getTopK() + ";minScore=" + config.getMinScore() + ";includeChunkText="
                + config.getIncludeChunkText() + ";maxChunkChars=" + config.getMaxChunkChars() + ";hybrid="
                + config.getHybrid() + ";rerank=" + config.getRerank();
    }

    private String join(Long[] values)
    {
        if (values == null || values.length == 0)
        {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (Long value : values)
        {
            if (value == null)
            {
                continue;
            }
            if (builder.length() > 0)
            {
                builder.append(',');
            }
            builder.append(value);
            count++;
            if (count >= 50)
            {
                break;
            }
        }
        return builder.toString();
    }

    private String join(String[] values)
    {
        if (values == null || values.length == 0)
        {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (String value : values)
        {
            if (StringUtils.isBlank(value))
            {
                continue;
            }
            if (builder.length() > 0)
            {
                builder.append(',');
            }
            builder.append(value);
            count++;
            if (count >= 50)
            {
                break;
            }
        }
        return builder.toString();
    }

    private String resolveCreateBy(LoginUser loginUser)
    {
        if (loginUser == null || StringUtils.isBlank(loginUser.getUsername()))
        {
            return "";
        }
        return safeText(loginUser.getUsername(), 64);
    }

    private String safeText(String text, int maxLength)
    {
        if (StringUtils.isBlank(text))
        {
            return "";
        }
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }

    private static class PermissionDecision
    {
        private final boolean allowed;

        private final boolean allowEmptyScope;

        private final String message;

        private PermissionDecision(boolean allowed, boolean allowEmptyScope, String message)
        {
            this.allowed = allowed;
            this.allowEmptyScope = allowEmptyScope;
            this.message = message;
        }

        private static PermissionDecision allowed(boolean allowEmptyScope)
        {
            return new PermissionDecision(true, allowEmptyScope, "");
        }

        private static PermissionDecision denied(String message)
        {
            return new PermissionDecision(false, false, message);
        }
    }
}
