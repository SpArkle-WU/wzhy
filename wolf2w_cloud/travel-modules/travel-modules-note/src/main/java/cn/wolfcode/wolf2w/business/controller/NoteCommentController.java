package cn.wolfcode.wolf2w.business.controller;

import cn.wolfcode.wolf2w.business.api.domain.NoteComment;
import cn.wolfcode.wolf2w.business.query.NoteCommentQuery;
import cn.wolfcode.wolf2w.business.service.INoteCommentService;
import cn.wolfcode.wolf2w.common.core.domain.R;
import cn.wolfcode.wolf2w.common.security.annotation.InnerAuth;
import cn.wolfcode.wolf2w.common.security.annotation.RequiresLogin;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 游记评论 Controller
 *
 * @author wzh
 * @date 2026-08-10
 */
@RestController
@RequestMapping("noteComments")
public class NoteCommentController {

    @Autowired
    private INoteCommentService noteCommentService;

    /**
     * 评论列表
     */
    @GetMapping("/query")
    public R<IPage<NoteComment>> query(NoteCommentQuery qo) {
        // 前台只能读取正常评论，状态筛选由后台管理接口承担。
        qo.setStatus("0");
        return R.ok(noteCommentService.queryPage(qo));
    }

    /**
     * 发表评论
     */
    @RequiresLogin
    @PostMapping("/save")
    public R<?> save(@RequestBody NoteComment comment) {
        noteCommentService.addContent(comment);
        return R.ok();
    }

    /**
     * 发表评论（前端路径别名 POST /noteComments/add）
     * 与 /save 等价，避免与前端约定路径不一致导致 404。
     */
    @RequiresLogin
    @PostMapping("/add")
    public R<?> add(@RequestBody NoteComment comment) {
        noteCommentService.addContent(comment);
        return R.ok();
    }

    /*****************************************对外暴露 Feign 接口**********************************************/

    @GetMapping("/feign/list")
    @InnerAuth
    public R<List<NoteComment>> feignList() {
        return R.ok(noteCommentService.list());
    }

    @InnerAuth
    @GetMapping("/feign/{id}")
    public R<NoteComment> feignGet(@PathVariable Long id) {
        return R.ok(noteCommentService.getById(id));
    }
}
