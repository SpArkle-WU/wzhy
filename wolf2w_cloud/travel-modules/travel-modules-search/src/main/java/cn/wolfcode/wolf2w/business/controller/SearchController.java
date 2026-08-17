package cn.wolfcode.wolf2w.business.controller;

import cn.wolfcode.wolf2w.business.api.RemoteStrategyService;
import cn.wolfcode.wolf2w.business.api.domain.Strategy;
import cn.wolfcode.wolf2w.business.api.domain.StrategyES;
import cn.wolfcode.wolf2w.business.query.SearchQuery;
import cn.wolfcode.wolf2w.common.core.domain.R;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SearchController {

    @Autowired
    private ElasticsearchClient client;
    @Autowired
    private RemoteStrategyService remoteStrategyService;

    @RequestMapping("/search")
    public R<Map<String, Object>> search(SearchQuery qo) throws IOException {
        switch (qo.getType()) {
            case 1:
                return R.ok(searchStrategy(qo));
            case 2:
                return R.ok(searchNote(qo));
            case 3:
                return R.ok(searchUser(qo));
            case -1:
                return R.ok(searchAll(qo));
            default:
                return R.ok();
        }
    }

    private Map<String, Object> searchAll(SearchQuery qo) {
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0L);
        result.put("dests", new ArrayList<>());
        result.put("strategies", new ArrayList<>());
        result.put("notes", new ArrayList<>());
        result.put("users", new ArrayList<>());
        return result;
    }

    private Map<String, Object> searchUser(SearchQuery qo) {
        Map<String, Object> result = new HashMap<>();
        result.put("content", new ArrayList<>());
        result.put("totalElements", 0L);
        return result;
    }

    private Map<String, Object> searchNote(SearchQuery qo) {
        Map<String, Object> result = new HashMap<>();
        result.put("content", new ArrayList<>());
        result.put("totalElements", 0L);
        return result;
    }

    private Map<String, Object> searchStrategy(SearchQuery qo) throws IOException {
        Map<String, Object> result = new HashMap<>();

        Map<String, HighlightField> highlightFields = new HashMap<>();
        highlightFields.put("title", HighlightField.of(h -> h));
        highlightFields.put("subTitle", HighlightField.of(h -> h));
        highlightFields.put("summary", HighlightField.of(h -> h));

        SearchResponse<StrategyES> response = client.search(s -> s
                        .index("strategy")
                        .from((qo.getCurrentPage() - 1) * qo.getPageSize())
                        .size(qo.getPageSize())
                        .query(q -> q
                                .multiMatch(m -> m
                                        .query(qo.getKeyword())
                                        .fields("title", "subTitle", "summary")
                                )
                        )
                        .highlight(h -> h
                                .fields(highlightFields)
                                .preTags("<span style=\"color:red\">")
                                .postTags("</span>")
                        ),
                StrategyES.class
        );

        HitsMetadata<StrategyES> hitsMetadata = response.hits();
        long total = hitsMetadata.total().value();
        List<Hit<StrategyES>> hits = hitsMetadata.hits();

        List<Strategy> strategyList = new ArrayList<>();
        for (Hit<StrategyES> hit : hits) {
            StrategyES es = hit.source();
            if (es == null) continue;

            Strategy strategy = remoteStrategyService.getOne(es.getId(), "inner").getData();
            if (strategy == null) continue;

            Map<String, List<String>> highlight = hit.highlight();
            if (highlight != null) {
                if (highlight.containsKey("title") && !highlight.get("title").isEmpty()) {
                    strategy.setTitle(highlight.get("title").get(0));
                }
                if (highlight.containsKey("subTitle") && !highlight.get("subTitle").isEmpty()) {
                    strategy.setSubTitle(highlight.get("subTitle").get(0));
                }
                if (highlight.containsKey("summary") && !highlight.get("summary").isEmpty()) {
                    strategy.setSummary(highlight.get("summary").get(0));
                }
            }

            strategyList.add(strategy);
        }

        System.out.println("搜索攻略完成，总记录数：" + total + "，当前返回：" + strategyList.size());

        result.put("content", strategyList);
        result.put("totalElements", total);
        return result;
    }
}