package cn.wolfcode.wolf2w.business.client.impl;

import cn.wolfcode.wolf2w.business.client.EmbeddingClient;
import cn.wolfcode.wolf2w.business.config.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiCompatibleEmbeddingClient implements EmbeddingClient {

    private final RestTemplate restTemplate;
    private final AiProperties aiProperties;

    public OpenAiCompatibleEmbeddingClient(RestTemplate restTemplate,
                                           AiProperties aiProperties) {
        this.restTemplate = restTemplate;
        this.aiProperties = aiProperties;
    }

    @Override
    public List<Float> embed(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiProperties.getEmbedding().getApiKey());

        Map<String, Object> request = new HashMap<>();
        request.put("model", aiProperties.getEmbedding().getModel());
        request.put("input", text);
        // 要求服务端按 ES 索引的维度返回向量
        request.put("dimensions", aiProperties.getEmbedding().getDimensions());

        JsonNode response = restTemplate.postForObject(
                aiProperties.getEmbedding().getBaseUrl() + "/embeddings",
                new HttpEntity<>(request, headers),
                JsonNode.class);

        JsonNode embedding = response == null ? null : response.at("/data/0/embedding");
        if (embedding == null || !embedding.isArray()) {
            throw new IllegalStateException("Embedding 接口响应格式错误");
        }

        List<Float> vector = new ArrayList<>();
        for (JsonNode value : embedding) {
            vector.add(value.floatValue());
        }

        if (vector.size() != aiProperties.getEmbedding().getDimensions()) {
            throw new IllegalStateException(
                    "向量维度不匹配，期望：" + aiProperties.getEmbedding().getDimensions()
                            + "，实际：" + vector.size());
        }
        return vector;
    }

    @Override
    public List<List<Float>> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return new ArrayList<>();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiProperties.getEmbedding().getApiKey());

        Map<String, Object> request = new HashMap<>();
        request.put("model", aiProperties.getEmbedding().getModel());
        request.put("input", texts);
        request.put("dimensions", aiProperties.getEmbedding().getDimensions());

        JsonNode response = restTemplate.postForObject(
                aiProperties.getEmbedding().getBaseUrl() + "/embeddings",
                new HttpEntity<>(request, headers),
                JsonNode.class);

        JsonNode data = response == null ? null : response.get("data");
        if (data == null || !data.isArray() || data.size() != texts.size()) {
            throw new IllegalStateException("Embedding 批量接口响应格式错误或数量不匹配");
        }

        List<List<Float>> results = new ArrayList<>();
        int dims = aiProperties.getEmbedding().getDimensions();
        for (JsonNode item : data) {
            JsonNode embedding = item.get("embedding");
            if (embedding == null || !embedding.isArray()) {
                throw new IllegalStateException("Embedding 批量接口响应中 embedding 字段缺失");
            }
            List<Float> vector = new ArrayList<>();
            for (JsonNode value : embedding) {
                vector.add(value.floatValue());
            }
            if (vector.size() != dims) {
                throw new IllegalStateException(
                        "向量维度不匹配，期望：" + dims + "，实际：" + vector.size());
            }
            results.add(vector);
        }
        return results;
    }
}