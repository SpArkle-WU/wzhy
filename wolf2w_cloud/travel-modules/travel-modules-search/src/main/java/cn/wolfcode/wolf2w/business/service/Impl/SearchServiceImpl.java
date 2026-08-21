package cn.wolfcode.wolf2w.business.service.Impl;

import cn.wolfcode.wolf2w.business.api.RemoteDestinationService;
import cn.wolfcode.wolf2w.business.api.RemoteNoteService;
import cn.wolfcode.wolf2w.business.api.RemoteStrategyService;
import cn.wolfcode.wolf2w.business.api.domain.Destination;
import cn.wolfcode.wolf2w.business.api.domain.DestinationES;
import cn.wolfcode.wolf2w.business.api.domain.Note;
import cn.wolfcode.wolf2w.business.api.domain.NoteES;
import cn.wolfcode.wolf2w.business.api.domain.Strategy;
import cn.wolfcode.wolf2w.business.api.domain.StrategyES;
import cn.wolfcode.wolf2w.business.api.domain.UserInfoES;
import cn.wolfcode.wolf2w.business.query.SearchQuery;
import cn.wolfcode.wolf2w.business.service.ISearchService;
import cn.wolfcode.wolf2w.member.api.RemoteUserInfoService;
import cn.wolfcode.wolf2w.member.api.domain.UserInfo;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements ISearchService {

    @Autowired
    private ElasticsearchClient client;
    @Autowired
    private RemoteDestinationService remoteDestinationService;
    @Autowired
    private RemoteNoteService remoteNoteService;
    @Autowired
    private RemoteStrategyService remoteStrategyService;
    @Autowired
    private RemoteUserInfoService remoteUserInfoService;

    @Override
    public <T, K> Page<T> searchWitHighLight(String indexName, Class<T> clazz, Class<K> esClazz,
                                             SearchQuery qo, String... fields) {
        try {
            // 1. 默认字段
            final String[] searchFields = (fields == null || fields.length == 0)
                    ? new String[]{"title", "subTitle", "summary", "content", "name"}
                    : fields;

            // 2. 构建高亮
            Map<String, HighlightField> highlightFields = new HashMap<>();
            for (String field : searchFields) {
                highlightFields.put(field, HighlightField.of(h -> h));
            }

            // 3. 执行搜索
            SearchResponse<K> response = client.search(s -> s
                            .index(indexName)
                            .from((qo.getCurrentPage() - 1) * qo.getPageSize())
                            .size(qo.getPageSize())
                            .query(q -> q
                                    .multiMatch(m -> m
                                            .query(qo.getKeyword())
                                            .fields(Arrays.asList(searchFields))
                                            .analyzer("ik_max_word")
                                    )
                            )
                            .highlight(h -> h
                                    .fields(highlightFields)
                                    .preTags("<span style=\"color:red\">")
                                    .postTags("</span>")
                            ),
                    esClazz
            );

            // 4. 处理结果
            List<T> resultList = new ArrayList<>();
            for (Hit<K> hit : response.hits().hits()) {
                K esData = hit.source();
                if (esData == null) continue;

                // 获取完整数据并应用高亮
                T target = getFullData(indexName, esData, clazz);
                if (target == null) continue;

                // 应用高亮
                applyHighlight(target, hit.highlight());
                resultList.add(target);
            }

            // 5. 返回分页结果
            long total = response.hits().total().value();
            PageRequest pageRequest = PageRequest.of(qo.getCurrentPage() - 1, qo.getPageSize());
            return new PageImpl<>(resultList, pageRequest, total);

        } catch (IOException e) {
            throw new RuntimeException("ES搜索失败", e);
        }
    }

    /**
     * 根据索引获取完整数据
     */
    @SuppressWarnings("unchecked")
    private <T, K> T getFullData(String indexName, K esData, Class<T> clazz) {
        Long id = getId(esData);
        if (id == null) return null;

        try {
            Object data = null;
            switch (indexName) {
                case "strategy":
                    data = remoteStrategyService.getOne(id, "inner").getData();
                    break;
                case "note":
                    data = remoteNoteService.getOne(id, "inner").getData();
                    break;
                case "destination":
                    data = remoteDestinationService.getOne(id, "inner").getData();
                    break;
                case "userinfo":
                    data = remoteUserInfoService.getOne(id, "inner").getData();
                    break;
                default:
                    // 没有远程服务，直接属性拷贝
                    data = clazz.getDeclaredConstructor().newInstance();
                    BeanUtils.copyProperties(esData, data);
                    break;
            }
            return (T) data;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 提取ID
     */
    private Long getId(Object obj) {
        try {
            Field field = obj.getClass().getDeclaredField("id");
            field.setAccessible(true);
            Object id = field.get(obj);
            return id instanceof Long ? (Long) id :
                    id instanceof Integer ? ((Integer) id).longValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 应用高亮
     */
    @SuppressWarnings("unchecked")
    private <T> void applyHighlight(T target, Map<String, List<String>> highlight) {
        if (highlight == null || highlight.isEmpty()) return;

        for (Map.Entry<String, List<String>> entry : highlight.entrySet()) {
            String fieldName = entry.getKey();
            List<String> values = entry.getValue();
            if (values == null || values.isEmpty()) continue;

            try {
                Field field = getField(target.getClass(), fieldName);
                if (field != null && field.getType() == String.class) {
                    field.setAccessible(true);
                    field.set(target, values.get(0));
                }
            } catch (Exception e) {
                // 忽略
            }
        }
    }

    /**
     * 递归查找字段
     */
    private Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return getField(clazz.getSuperclass(), fieldName);
            }
            return null;
        }
    }

    @Override
    public Map<String, Object> searchAll(SearchQuery qo) {
        // 使用 LinkedHashMap 保证插入顺序：目的地 → 攻略 → 游记 → 用户
        Map<String, Object> result = new LinkedHashMap<>();
        long total = 0;

        // 1. 目的地
        try {
            Page<Destination> destPage = searchWitHighLight(
                    "destination", Destination.class, DestinationES.class, qo,
                    "name", "info"
            );
            result.put("dests", destPage.getContent());
            total += destPage.getTotalElements();
        } catch (Exception e) {
            result.put("dests", new ArrayList<>());
        }

        // 2. 攻略
        try {
            Page<Strategy> strategyPage = searchWitHighLight(
                    "strategy", Strategy.class, StrategyES.class, qo,
                    "title", "subTitle", "summary"
            );
            result.put("strategies", strategyPage.getContent());
            total += strategyPage.getTotalElements();
        } catch (Exception e) {
            result.put("strategies", new ArrayList<>());
        }

        // 3. 游记
        try {
            Page<Note> notePage = searchWitHighLight(
                    "note", Note.class, NoteES.class, qo,
                    "title", "summary"
            );
            result.put("notes", notePage.getContent());
            total += notePage.getTotalElements();
        } catch (Exception e) {
            result.put("notes", new ArrayList<>());
        }

        // 4. 用户
        try {
            Page<UserInfo> userPage = searchWitHighLight(
                    "userinfo", UserInfo.class, UserInfoES.class, qo,
                    "nickname", "info", "city"
            );
            result.put("users", userPage.getContent());
            total += userPage.getTotalElements();
        } catch (Exception e) {
            result.put("users", new ArrayList<>());
        }

        result.put("total", total);
        return result;
    }
}