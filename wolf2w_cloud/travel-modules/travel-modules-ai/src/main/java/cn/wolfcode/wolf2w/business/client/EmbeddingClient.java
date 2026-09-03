package cn.wolfcode.wolf2w.business.client;

import java.util.List;

public interface EmbeddingClient {
    // 对文本进行嵌入
    // @param text 要嵌入的文本
    // @return 嵌入后的向量
    List<Float> embed(String text);

    // 批量嵌入文本，减少网络请求次数
    // @param texts 要嵌入的文本列表
    // @return 嵌入后的向量列表，顺序与输入一致
    List<List<Float>> embedBatch(List<String> texts);
}
