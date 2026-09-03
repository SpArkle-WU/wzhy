package cn.wolfcode.wolf2w.business.service;

import cn.wolfcode.wolf2w.business.domain.KnowledgeChunk;
import java.io.IOException;
import java.util.List;

public interface IKnowledgeIndexService {

    // 确保索引存在
    void ensureIndex() throws IOException;

    // 根据来源类型和来源ID删除索引
    void deleteBySource(String sourceType, Long sourceId) throws IOException;

    // 批量索引知识块
    void bulkIndex(List<KnowledgeChunk> chunks) throws IOException;
}