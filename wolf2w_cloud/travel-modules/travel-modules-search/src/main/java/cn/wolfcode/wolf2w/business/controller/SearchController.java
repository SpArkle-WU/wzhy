package cn.wolfcode.wolf2w.business.controller;

import cn.wolfcode.wolf2w.business.api.domain.*;
import cn.wolfcode.wolf2w.business.query.SearchQuery;
import cn.wolfcode.wolf2w.business.service.ISearchService;
import cn.wolfcode.wolf2w.common.core.domain.R;
import cn.wolfcode.wolf2w.member.api.domain.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
public class SearchController {

    @Autowired
    private ISearchService searchService;

    @RequestMapping("/search")
    public R<?> search(SearchQuery qo) {
        switch (qo.getType()) {
            case 0: {
                // 前端期望 dest(单个对象) + users/notes/strategies/total
                Map<String, Object> result = searchService.searchAll(qo);
                List<?> dests = (List<?>) result.get("dests");
                Object dest = (dests != null && !dests.isEmpty()) ? dests.get(0) : null;
                result.put("dest", dest);
                result.remove("dests");
                return R.ok(result);
            }
            case 1:
                return R.ok(searchService.searchWitHighLight(
                        "strategy", Strategy.class, StrategyES.class, qo,
                        "title", "subTitle", "summary"
                ));
            case 2:
                return R.ok(searchService.searchWitHighLight(
                        "note", Note.class, NoteES.class, qo,
                        "title", "summary"
                ));
            case 3:
                return R.ok(searchService.searchWitHighLight(
                        "userinfo", UserInfo.class, UserInfoES.class, qo,
                        "nickname", "info","city"
                ));
            case -1:
                // 搜索所有类型
                return R.ok(searchService.searchAll(qo));
            default:
                return R.ok();
        }
    }
}
