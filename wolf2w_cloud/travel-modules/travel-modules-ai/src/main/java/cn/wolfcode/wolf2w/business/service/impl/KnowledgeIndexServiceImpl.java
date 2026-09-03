package cn.wolfcode.wolf2w.business.service.impl;

import cn.wolfcode.wolf2w.business.config.AiProperties;
import cn.wolfcode.wolf2w.business.domain.KnowledgeChunk;
import cn.wolfcode.wolf2w.business.service.IKnowledgeIndexService;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.mapping.DenseVectorSimilarity;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class KnowledgeIndexServiceImpl implements IKnowledgeIndexService {

    private final ElasticsearchClient client;
    private final AiProperties aiProperties;

    public KnowledgeIndexServiceImpl(ElasticsearchClient client, AiProperties aiProperties) {
        this.client = client;
        this.aiProperties = aiProperties;
    }

    @Override
    public void ensureIndex() throws IOException {
        String index = aiProperties.getRag().getIndexName();

        if (client.indices().exists(e -> e.index(index)).value()) {
            return;
        }

        client.indices().create(c -> c.index(index).mappings(m -> m
                .properties("chunkId", p -> p.keyword(k -> k))
                .properties("sourceType", p -> p.keyword(k -> k))
                .properties("sourceId", p -> p.long_(l -> l))
                .properties("chunkNo", p -> p.integer(i -> i))
                .properties("title", p -> p.text(t -> t.analyzer("ik_max_word")))
                .properties("destinationId", p -> p.long_(l -> l))
                .properties("content", p -> p.text(t -> t.analyzer("ik_max_word")))
                .properties("content_vector", p -> p.denseVector(d -> d
                        .dims(aiProperties.getEmbedding().getDimensions())
                        .similarity(DenseVectorSimilarity.Cosine)))
                .properties("updatedAt", p -> p.date(d -> d))
        ));
    }

    @Override
    public void deleteBySource(String sourceType, Long sourceId) throws IOException {
        client.deleteByQuery(d -> d
                .index(aiProperties.getRag().getIndexName())
                .query(q -> q.bool(b -> b
                        .filter(f -> f.term(t -> t.field("sourceType").value(sourceType)))
                        .filter(f -> f.term(t -> t.field("sourceId").value(sourceId)))
                ))
                .refresh(true));
    }

    @Override
    public void bulkIndex(List<KnowledgeChunk> chunks) throws IOException {
        if (chunks.isEmpty()) {
            return;
        }

        BulkRequest.Builder builder = new BulkRequest.Builder();
        String index = aiProperties.getRag().getIndexName();

        for (KnowledgeChunk chunk : chunks) {
            builder.operations(op -> op.index(i -> i
                    .index(index)
                    .id(chunk.getChunkId())
                    .document(chunk)));
        }

        BulkResponse response = client.bulk(
                builder.refresh(Refresh.WaitFor).build());

        if (response.errors()) {
            throw new IOException("知识块批量写入 ES 失败");
        }
    }
}