package cn.wolfcode.wolf2w.business.controller.admin;

import cn.wolfcode.wolf2w.business.service.IKnowledgeBuildService;
import cn.wolfcode.wolf2w.common.core.domain.R;
import org.springframework.web.bind.annotation.*;
/**
 * AI知识库构建管理控制器,管理员建库接口
 */
@RestController
@RequestMapping({"/ai/admin/knowledge", "/admin/knowledge"})
public class AiKnowledgeAdminController {

    private final IKnowledgeBuildService knowledgeBuildService;

    public AiKnowledgeAdminController(IKnowledgeBuildService knowledgeBuildService) {
        this.knowledgeBuildService = knowledgeBuildService;
    }

    @PostMapping("/strategy/rebuild")
    public R<Void> rebuildAllStrategies() {
        knowledgeBuildService.rebuildAllStrategies();
        return R.ok(null, "知识库重建任务已提交，正在后台执行，请查看日志了解进度");
    }

    @PostMapping("/strategy/{id}/rebuild")
    public R<Void> rebuildStrategy(@PathVariable("id") Long id) {
        knowledgeBuildService.rebuildStrategy(id);
        return R.ok(null, "攻略知识库重建任务已提交，正在后台执行，请查看日志了解进度");
    }
}
