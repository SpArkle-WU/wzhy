package cn.wolfcode.wolf2w.business.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 对外暴露的检索结果，不返回高维向量。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSearchResult {

    private String chunkId;
    private String sourceType;
    private Long sourceId;
    private Integer chunkNo;
    private String title;
    private Long destinationId;
    private String content;
    private Date updatedAt;

    public static KnowledgeSearchResult from(KnowledgeChunk chunk) {
        return new KnowledgeSearchResult(
                chunk.getChunkId(),
                chunk.getSourceType(),
                chunk.getSourceId(),
                chunk.getChunkNo(),
                chunk.getTitle(),
                chunk.getDestinationId(),
                chunk.getContent(),
                chunk.getUpdatedAt());
    }
}
