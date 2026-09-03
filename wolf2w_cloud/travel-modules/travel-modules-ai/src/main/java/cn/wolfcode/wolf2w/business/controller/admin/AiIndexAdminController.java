package cn.wolfcode.wolf2w.business.controller.admin;

import cn.wolfcode.wolf2w.business.service.IKnowledgeIndexService;
import cn.wolfcode.wolf2w.common.core.domain.R;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI索引管理控制器,管理员索引接口
 */
@RestController
@RequestMapping({"/ai/admin/index", "/admin/index"})
public class AiIndexAdminController {

    private final IKnowledgeIndexService knowledgeIndexService;

    public AiIndexAdminController(IKnowledgeIndexService knowledgeIndexService) {
        this.knowledgeIndexService = knowledgeIndexService;
    }

    @PostMapping("/ensure")
    public R<Void> ensureIndex() throws Exception {
        knowledgeIndexService.ensureIndex();
        return R.ok();
    }
}
