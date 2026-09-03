package cn.wolfcode.wolf2w.business.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 定义独立知识块文档
 */
@Data
public class KnowledgeChunk {

    private String chunkId;        // strategy-1001-0，固定且可重复覆盖
    private String sourceType;     // strategy / note / destination
    private Long sourceId;         // 原始业务主键
    private Integer chunkNo;       // 原文中的块序号
    private String title;          // 攻略标题，提升检索语义
    private Long destinationId;    // 方便后续按目的地过滤
    private String content;        // 本块正文
    // 向量索引
    @JsonProperty("content_vector")
    private List<Float> contentVector; // 必须和 mapping 字段名一致
    private Date updatedAt;
}
