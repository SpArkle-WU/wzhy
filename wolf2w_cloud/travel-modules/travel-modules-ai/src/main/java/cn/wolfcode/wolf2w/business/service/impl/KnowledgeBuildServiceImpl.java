package cn.wolfcode.wolf2w.business.service.impl;

import cn.wolfcode.wolf2w.business.api.RemoteStrategyContentService;
import cn.wolfcode.wolf2w.business.api.RemoteStrategyService;
import cn.wolfcode.wolf2w.business.api.domain.Strategy;
import cn.wolfcode.wolf2w.business.api.domain.StrategyContent;
import cn.wolfcode.wolf2w.business.client.EmbeddingClient;
import cn.wolfcode.wolf2w.business.config.AiProperties;
import cn.wolfcode.wolf2w.business.domain.KnowledgeChunk;
import cn.wolfcode.wolf2w.business.service.IKnowledgeBuildService;
import cn.wolfcode.wolf2w.business.service.IKnowledgeIndexService;
import cn.wolfcode.wolf2w.business.util.KnowledgeTextSplitter;
import cn.wolfcode.wolf2w.common.core.domain.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class KnowledgeBuildServiceImpl implements IKnowledgeBuildService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBuildServiceImpl.class);

    private final RemoteStrategyService strategyClient;
    private final RemoteStrategyContentService contentClient;
    private final IKnowledgeIndexService indexService;
    private final EmbeddingClient embeddingClient;
    private final KnowledgeTextSplitter splitter;
    private final AiProperties aiProperties;

    public KnowledgeBuildServiceImpl(RemoteStrategyService strategyClient,
                                     RemoteStrategyContentService contentClient,
                                     IKnowledgeIndexService indexService,
                                     EmbeddingClient embeddingClient,
                                     KnowledgeTextSplitter splitter,
                                     AiProperties aiProperties) {
        this.strategyClient = strategyClient;
        this.contentClient = contentClient;
        this.indexService = indexService;
        this.embeddingClient = embeddingClient;
        this.splitter = splitter;
        this.aiProperties = aiProperties;
    }

    @Async("knowledgeBuildExecutor")
    @Override
    public void rebuildAllStrategies() {
        try {
            R<List<Strategy>> result = strategyClient.list("inner");
            if (result == null || R.isError(result) || result.getData() == null) {
                log.error("知识库重建失败：获取攻略列表失败");
                return;
            }

            log.info("开始全量重建知识库，共 {} 篇攻略", result.getData().size());
            int successCount = 0;
            for (Strategy strategy : result.getData()) {
                try {
                    rebuild(strategy);
                    successCount++;
                } catch (Exception e) {
                    log.error("重建攻略知识库失败，id={}，title={}",
                            strategy.getId(), strategy.getTitle(), e);
                }
            }
            log.info("全量知识库重建完成，成功 {}/{} 篇", successCount, result.getData().size());
        } catch (Exception e) {
            log.error("全量知识库重建异常", e);
        }
    }

    @Async("knowledgeBuildExecutor")
    @Override
    public void rebuildStrategy(Long strategyId) {
        try {
            R<Strategy> result = strategyClient.getOne(strategyId, "inner");
            if (result == null || R.isError(result) || result.getData() == null) {
                log.error("重建攻略知识库失败：获取攻略失败，id={}", strategyId);
                return;
            }

            log.info("开始重建攻略知识库，id={}，title={}", strategyId, result.getData().getTitle());
            rebuild(result.getData());
            log.info("攻略知识库重建完成，id={}", strategyId);
        } catch (Exception e) {
            log.error("重建攻略知识库异常，id={}", strategyId, e);
        }
    }

    private void rebuild(Strategy strategy) {
        try {
            indexService.ensureIndex();

            R<StrategyContent> result =
                    contentClient.getOne(strategy.getId(), "inner");
            StrategyContent fullContent = result == null ? null : result.getData();

            if (fullContent == null || fullContent.getContent() == null) {
                return;
            }

            // 先删除旧块。正文编辑变短时，不会残留旧知识块。
            indexService.deleteBySource("strategy", strategy.getId());

            List<String> parts = splitter.split(
                    fullContent.getContent(),
                    aiProperties.getRag().getChunkSize(),
                    aiProperties.getRag().getChunkOverlap());

            // 构造所有待嵌入文本（标题 + 内容片段一起参与 Embedding）
            List<String> textsToEmbed = new ArrayList<>();
            for (String part : parts) {
                textsToEmbed.add(strategy.getTitle() + "\n" + part);
            }

            // 批量嵌入，一次 API 调用获取所有向量，大幅减少网络开销
            List<List<Float>> vectors = embeddingClient.embedBatch(textsToEmbed);

            List<KnowledgeChunk> chunks = new ArrayList<>();
            for (int i = 0; i < parts.size(); i++) {
                String part = parts.get(i);

                KnowledgeChunk chunk = new KnowledgeChunk();
                chunk.setChunkId("strategy-" + strategy.getId() + "-" + i);
                chunk.setSourceType("strategy");
                chunk.setSourceId(strategy.getId());
                chunk.setChunkNo(i);
                chunk.setTitle(strategy.getTitle());
                chunk.setDestinationId(strategy.getDestId());
                chunk.setContent(part);
                chunk.setContentVector(vectors.get(i));
                chunk.setUpdatedAt(new Date());
                chunks.add(chunk);
            }

            indexService.bulkIndex(chunks);
        } catch (IOException e) {
            throw new IllegalStateException("攻略知识库构建失败，id=" + strategy.getId(), e);
        }
    }
}