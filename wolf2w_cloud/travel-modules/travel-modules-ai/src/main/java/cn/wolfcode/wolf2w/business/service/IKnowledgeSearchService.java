package cn.wolfcode.wolf2w.business.service;

import cn.wolfcode.wolf2w.business.domain.KnowledgeChunk;

import java.io.IOException;
import java.util.List;

public interface IKnowledgeSearchService {

    List<KnowledgeChunk> search(String question, int topK) throws IOException;
}
