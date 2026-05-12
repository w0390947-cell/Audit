package com.ruoyi.system.service.audit.vector.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.ruoyi.system.config.VectorProperties;
import com.ruoyi.system.domain.audit.vector.RerankResult;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class AliyunBailianRerankClientTest
{
    @Test
    void rerankParsesResultsInProviderOrder()
    {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("https://dashscope.aliyuncs.com/compatible-api/v1/reranks"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andRespond(withSuccess("{\"results\":[{\"index\":1,\"relevance_score\":0.91}," +
                        "{\"index\":0,\"relevance_score\":0.42}]}", MediaType.APPLICATION_JSON));

        AliyunBailianRerankClient client = new AliyunBailianRerankClient(properties(), restTemplate);
        List<RerankResult> results = client.rerank("防爆手机外壳要求", Arrays.asList("标准 A", "标准 B"), 2);

        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getIndex());
        assertEquals("0.91", results.get(0).getScore().toPlainString());
        assertEquals(0, results.get(1).getIndex());
        assertEquals("0.42", results.get(1).getScore().toPlainString());
        server.verify();
    }

    @Test
    void rerankRequiresApiKey()
    {
        RestTemplate restTemplate = new RestTemplate();
        VectorProperties properties = properties();
        properties.getReranker().setApiKey("");
        AliyunBailianRerankClient client = new AliyunBailianRerankClient(properties, restTemplate);

        assertThrows(IllegalStateException.class, () -> client.rerank("query", Arrays.asList("doc"), 1));
    }

    private VectorProperties properties()
    {
        VectorProperties properties = new VectorProperties();
        properties.getReranker().setBaseUrl("https://dashscope.aliyuncs.com/compatible-api/v1");
        properties.getReranker().setApiKey("test-key");
        properties.getReranker().setModel("qwen3-rerank");
        return properties;
    }
}
