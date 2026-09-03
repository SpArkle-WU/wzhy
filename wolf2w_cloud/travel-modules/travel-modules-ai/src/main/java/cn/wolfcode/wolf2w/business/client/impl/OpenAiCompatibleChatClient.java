package cn.wolfcode.wolf2w.business.client.impl;

import cn.wolfcode.wolf2w.business.client.ChatClient;
import cn.wolfcode.wolf2w.business.config.AiProperties;
import cn.wolfcode.wolf2w.business.domain.ChatMessage;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiCompatibleChatClient implements ChatClient {

    private final RestTemplate restTemplate;
    private final AiProperties aiProperties;

    public OpenAiCompatibleChatClient(RestTemplate restTemplate,
                                      AiProperties aiProperties) {
        this.restTemplate = restTemplate;
        this.aiProperties = aiProperties;
    }

    @Override
    public String chat(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("聊天消息不能为空");
        }
        String baseUrl = aiProperties.getChat().getBaseUrl();
        if (isBlank(baseUrl) || isBlank(aiProperties.getChat().getModel())
                || isBlank(aiProperties.getChat().getApiKey())) {
            throw new IllegalStateException("聊天模型配置不完整");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiProperties.getChat().getApiKey());

        List<Map<String, String>> requestMessages = new ArrayList<>();
        for (ChatMessage message : messages) {
            if (message == null || isBlank(message.getRole()) || isBlank(message.getContent())) {
                throw new IllegalArgumentException("聊天消息的 role 和 content 不能为空");
            }
            Map<String, String> item = new HashMap<>();
            item.put("role", message.getRole());
            item.put("content", message.getContent());
            requestMessages.add(item);
        }

        Map<String, Object> request = new HashMap<>();
        request.put("model", aiProperties.getChat().getModel());
        request.put("messages", requestMessages);
        request.put("stream", false);

        try {
            JsonNode response = restTemplate.postForObject(
                    trimTrailingSlash(baseUrl) + "/chat/completions",
                    new HttpEntity<>(request, headers),
                    JsonNode.class);

            JsonNode content = response == null
                    ? null
                    : response.at("/choices/0/message/content");
            if (content == null || !content.isTextual() || isBlank(content.asText())) {
                throw new IllegalStateException("聊天接口响应格式错误");
            }
            return content.asText();
        } catch (ResourceAccessException e) {
            throw new IllegalStateException("聊天模型请求超时或网络不可用", e);
        } catch (HttpStatusCodeException e) {
            int status = e.getStatusCode().value();
            if (isQuotaExhausted(e)) {
                throw new IllegalStateException("聊天模型免费额度已耗尽，请在阿里云百炼控制台充值，或关闭“仅使用免费额度”后重试", e);
            }
            if (status == 401 || status == 403) {
                throw new IllegalStateException("聊天模型鉴权失败，请检查 API Key 和模型权限", e);
            }
            throw new IllegalStateException("聊天模型请求失败，HTTP 状态码：" + status, e);
        }
    }

    private static boolean isQuotaExhausted(HttpStatusCodeException exception) {
        return exception.getResponseBodyAsString().contains("insufficient_quota");
    }

    private static String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
