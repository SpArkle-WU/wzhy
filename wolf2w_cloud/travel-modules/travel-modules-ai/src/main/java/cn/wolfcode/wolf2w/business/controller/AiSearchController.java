package cn.wolfcode.wolf2w.business.controller;

import cn.wolfcode.wolf2w.business.config.AiProperties;
import cn.wolfcode.wolf2w.business.domain.KnowledgeSearchResult;
import cn.wolfcode.wolf2w.business.service.IKnowledgeSearchService;
import cn.wolfcode.wolf2w.common.core.domain.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/ai/search", "/search"})
public class AiSearchController {

    private final IKnowledgeSearchService searchService;
    private final AiProperties aiProperties;

    public AiSearchController(IKnowledgeSearchService searchService,
                              AiProperties aiProperties) {
        this.searchService = searchService;
        this.aiProperties = aiProperties;
    }

    @GetMapping
    public R<List<KnowledgeSearchResult>> search(
            @RequestParam("question") String question,
            @RequestParam(value = "topK", required = false) Integer topK) throws IOException {

        int effectiveTopK = topK == null
                ? (aiProperties.getRag().getRetrieveSize() == null
                ? 5 : aiProperties.getRag().getRetrieveSize())
                : topK;

        List<KnowledgeSearchResult> results = searchService.search(question, effectiveTopK)
                .stream()
                .map(KnowledgeSearchResult::from)
                .collect(Collectors.toList());
        return R.ok(results);
    }
}
