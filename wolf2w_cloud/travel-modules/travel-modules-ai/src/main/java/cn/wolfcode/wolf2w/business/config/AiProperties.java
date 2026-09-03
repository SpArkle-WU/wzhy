package cn.wolfcode.wolf2w.business.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    // 嵌入模型配置
    private Embedding embedding = new Embedding();
    // 聊天模型配置
    private Chat chat = new Chat();
    // RAG模型配置
    private Rag rag = new Rag();

    // 嵌入模型配置
    @Data
    public static class Embedding {
        private String baseUrl;
        private String model;
        private String apiKey;
        private Integer dimensions;
    }

    // 聊天模型配置
    @Data
    public static class Chat {
        private String baseUrl;
        private String model;
        private String apiKey;
    }

    // RAG模型配置
    @Data
    public static class Rag {
        private String indexName;
        private Integer chunkSize;
        private Integer chunkOverlap;
        private Integer retrieveSize;
    }
}