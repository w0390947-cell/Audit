package com.ruoyi.system.service.audit.vector.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.ruoyi.system.config.VectorProperties;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class AliyunBailianEmbeddingClientTest
{
    @Test
    void embedParsesResponseInInputOrder()
    {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("https://dashscope.aliyuncs.com/compatible-mode/v1/embeddings"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andRespond(withSuccess(successBody(), MediaType.APPLICATION_JSON));

        AliyunBailianEmbeddingClient client = new AliyunBailianEmbeddingClient(properties(1024), restTemplate);
        List<float[]> vectors = client.embed(Arrays.asList("alpha", "beta"));

        assertEquals(2, vectors.size());
        assertEquals(0.1f, vectors.get(0)[0], 0.0001f);
        assertEquals(0.2f, vectors.get(1)[0], 0.0001f);
        server.verify();
    }

    @Test
    void embedRejectsUnexpectedDimensions()
    {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("https://dashscope.aliyuncs.com/compatible-mode/v1/embeddings"))
                .andRespond(withSuccess("{\"data\":[{\"index\":0,\"embedding\":[0.1,0.2]}]}",
                        MediaType.APPLICATION_JSON));

        AliyunBailianEmbeddingClient client = new AliyunBailianEmbeddingClient(properties(1024), restTemplate);

        assertThrows(IllegalStateException.class, () -> client.embed(Arrays.asList("alpha")));
        server.verify();
    }

    private VectorProperties properties(int dimensions)
    {
        VectorProperties properties = new VectorProperties();
        properties.getEmbedding().setBaseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1");
        properties.getEmbedding().setApiKey("test-key");
        properties.getEmbedding().setModel("text-embedding-v4");
        properties.getEmbedding().setDimensions(dimensions);
        return properties;
    }

    private String successBody()
    {
        return "{\"data\":[{\"index\":1,\"embedding\":" + vector(0.2f) + "}," +
                "{\"index\":0,\"embedding\":" + vector(0.1f) + "}]}";
    }

    private String vector(float first)
    {
        StringBuilder builder = new StringBuilder();
        builder.append('[').append(first);
        for (int i = 1; i < 1024; i++)
        {
            builder.append(',').append(0.0f);
        }
        builder.append(']');
        return builder.toString();
    }
}
