package cn.wolfcode.wolf2w.business.service;

import cn.wolfcode.wolf2w.business.query.SearchQuery;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface ISearchService {
    // 搜索并高亮显示 T:MySQL实体类 K:ES实体类
    // @param indexName ES索引名
    // @param clazz MySQL实体类
    // @param esClazz ES实体类
    // @param qo 搜索查询条件
    // @param fields 高亮显示的字段，默认所有字段
    // @return 搜索结果
    <T,K> Page<T> searchWitHighLight(String indexName, Class<T> clazz, Class<K> esClazz, SearchQuery qo, String... fields);

    /**
     * 搜索所有类型（目的地、攻略、游记、用户）并聚合返回
     * @param qo 搜索查询条件
     * @return Map，key: 类型标识(dests/strategies/notes/users/total)，value: 对应数据列表或总数
     */
    Map<String, Object> searchAll(SearchQuery qo);
}
