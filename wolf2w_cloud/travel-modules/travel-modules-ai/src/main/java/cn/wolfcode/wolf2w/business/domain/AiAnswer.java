package cn.wolfcode.wolf2w.business.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG 问答响应，不包含知识块中的向量字段。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiAnswer {

    private String answer;
    private List<AiSource> sources;
    /**
     * true 表示答案由当前检索到的知识块支撑；false 表示未命中相关资料后的通用回答。
     */
    private boolean knowledgeBased;
}
