package cn.wolfcode.wolf2w.business.service.impl;

import cn.wolfcode.wolf2w.business.client.EmbeddingClient;
import cn.wolfcode.wolf2w.business.config.AiProperties;
import cn.wolfcode.wolf2w.business.domain.KnowledgeChunk;
import cn.wolfcode.wolf2w.business.service.IKnowledgeSearchService;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnowledgeSearchServiceImpl implements IKnowledgeSearchService {

    private static final int RRF_K = 60;

    private final ElasticsearchClient client;
    private final EmbeddingClient embeddingClient;
    private final AiProperties aiProperties;

    public KnowledgeSearchServiceImpl(ElasticsearchClient client,
                                      EmbeddingClient embeddingClient,
                                      AiProperties aiProperties) {
        this.client = client;
        this.embeddingClient = embeddingClient;
        this.aiProperties = aiProperties;
    }

    @Override
    public List<KnowledgeChunk> search(String question, int topK) throws IOException {
        if (question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("question 不能为空");
        }
        if (topK < 1 || topK > 20) {
            throw new IllegalArgumentException("topK 必须在 1 到 20 之间");
        }

        // 1. 将用户问题转换为向量
        List<Float> queryVector = embeddingClient.embed(question.trim());

        // 召回候选数扩大，保证融合后有足够候选
        int candidateSize = Math.max(topK * 2, 20);

        // 2. BM25 关键词召回（title 加权）
        SearchResponse<KnowledgeChunk> bm25Response = client.search(s -> s
                        .index(aiProperties.getRag().getIndexName())
                        .size(candidateSize)
                        .query(q -> q.multiMatch(m -> m
                                .query(question)
                                .fields("title^2", "content")
                                .type(TextQueryType.BestFields))),
                KnowledgeChunk.class);

        // 3. KNN 向量语义召回
        SearchResponse<KnowledgeChunk> knnResponse = client.search(s -> s
                        .index(aiProperties.getRag().getIndexName())
                        .size(candidateSize)
                        .knn(k -> k
                                .field("content_vector")
                                .queryVector(queryVector)
                                .k(candidateSize)
                                .numCandidates(Math.max(candidateSize * 5, 50))),
                KnowledgeChunk.class);

        // 4. RRF（Reciprocal Rank Fusion）分数融合
        // 公式: score(d) = Σ 1/(k + rank_i(d))，k=60
        Map<String, Float> rrfScores = new LinkedHashMap<>();
        Map<String, KnowledgeChunk> chunkMap = new LinkedHashMap<>();

        collectRrfScores(bm25Response.hits().hits(), rrfScores, chunkMap);
        collectRrfScores(knnResponse.hits().hits(), rrfScores, chunkMap);

        // 5. 按 RRF 分数降序，取 topK
        return rrfScores.entrySet().stream()
                .sorted(Map.Entry.<String, Float>comparingByValue().reversed()
                        .thenComparing(Map.Entry::getKey))
                .limit(topK)
                .map(e -> chunkMap.get(e.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void collectRrfScores(List<Hit<KnowledgeChunk>> hits,
                                  Map<String, Float> rrfScores,
                                  Map<String, KnowledgeChunk> chunkMap) {
        for (int i = 0; i < hits.size(); i++) {
            String id = hits.get(i).id();
            float rankScore = 1.0f / (RRF_K + i + 1);
            rrfScores.merge(id, rankScore, Float::sum);
            if (hits.get(i).source() != null) {
                chunkMap.put(id, hits.get(i).source());
            }
        }
    }
}
