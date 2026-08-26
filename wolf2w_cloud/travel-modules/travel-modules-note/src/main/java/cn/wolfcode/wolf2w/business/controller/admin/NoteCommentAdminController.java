package cn.wolfcode.wolf2w.business.controller.admin;

import cn.wolfcode.wolf2w.business.api.domain.NoteComment;
import cn.wolfcode.wolf2w.business.query.NoteCommentQuery;
import cn.wolfcode.wolf2w.business.service.INoteCommentService;
import cn.wolfcode.wolf2w.common.core.domain.R;
import cn.wolfcode.wolf2w.common.core.web.controller.BaseController;
import cn.wolfcode.wolf2w.common.log.annotation.Log;
import cn.wolfcode.wolf2w.common.log.enums.BusinessType;
import cn.wolfcode.wolf2w.common.security.annotation.RequiresPermissions;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 游记评论管理 Controller
 *
 * @author wzh
 * @date 2026-08-10
 */
@RestController
@RequestMapping("/admin/noteComments")
public class NoteCommentAdminController extends BaseController {

    @Autowired
    private INoteCommentService noteCommentService;

    /**
     * 查询游记评论列表
     */
    @RequiresPermissions("business:noteComment:list")
    @GetMapping("/list")
    public R<IPage<NoteComment>> list(NoteCommentQuery qo) {
        return R.ok(noteCommentService.queryPage(qo));
    }

    /**
     * 获取游记评论详细信息
     */
    @RequiresPermissions("business:noteComment:query")
    @GetMapping(value = "/{id}")
    public R<NoteComment> getInfo(@PathVariable("id") Long id) {
        return R.ok(noteCommentService.getById(id));
    }

    /**
     * 后台新增游记评论（前端：addNoteComment(data) POST /note/admin/noteComments）
     */
    @RequiresPermissions("business:noteComment:add")
    @Log(title = "游记评论", businessType = BusinessType.INSERT)
    @PostMapping
    public R<?> add(@RequestBody NoteComment comment) {
        // 后台手动添加时走通用 addContent（会注入登录用户/时间/状态/游记标题冗余/回复数自增）
        noteCommentService.addContent(comment);
        return R.ok();
    }

    /**
     * 修改游记评论 / 修改评论状态（0 正常 / 1 禁用）
     * 前端：updateNoteComment(data) PUT /note/admin/noteComments
     * 兼容通用状态修改：comment 对象只需要 id + status 即可做 status 切换
     */
    @RequiresPermissions("business:noteComment:edit")
    @Log(title = "游记评论", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<?> edit(@RequestBody NoteComment comment) {
        return toAjax(noteCommentService.updateById(comment));
    }

    /**
     * 删除游记评论（支持单个 id 或逗号分隔多个 id）
     * 前端：delNoteComment(id) DELETE /note/admin/noteComments/{id}
     */
    @RequiresPermissions("business:noteComment:remove")
    @Log(title = "游记评论", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<?> remove(@PathVariable Long[] ids) {
        return toAjax(noteCommentService.removeByIds(Arrays.stream(ids).collect(Collectors.toList())));
    }
}
