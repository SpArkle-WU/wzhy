package cn.wolfcode.wolf2w.business.service;

/**
 * 攻略知识库构建服务接口
 */
public interface IKnowledgeBuildService {
    // 重建构建所有策略的知识库
    void rebuildAllStrategies();

    // 重建构建指定策略的知识库
    void rebuildStrategy(Long strategyId);
}
