package cn.wolfcode.wolf2w.business.controller.admin;

import cn.wolfcode.wolf2w.business.api.domain.Note;
import cn.wolfcode.wolf2w.business.api.domain.NoteContent;
import cn.wolfcode.wolf2w.business.mapper.NoteContentMapper;
import cn.wolfcode.wolf2w.business.query.NoteQuery;
import cn.wolfcode.wolf2w.business.service.INoteService;
import cn.wolfcode.wolf2w.common.core.domain.R;
import cn.wolfcode.wolf2w.common.core.web.controller.BaseController;
import cn.wolfcode.wolf2w.common.log.annotation.Log;
import cn.wolfcode.wolf2w.common.log.enums.BusinessType;
import cn.wolfcode.wolf2w.common.security.annotation.RequiresPermissions;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 游记管理 Controller
 *
 * @author wzh
 * @date 2026-08-10
 */
@RestController
@RequestMapping("/admin/notes")
public class NoteAdminController extends BaseController {

    @Autowired
    private INoteService noteService;
    @Autowired
    private NoteContentMapper noteContentMapper;

    /**
     * 查询游记列表
     */
    @RequiresPermissions("business:note:list")
    @GetMapping("/list")
    public R<IPage<Note>> list(NoteQuery qo) {
        // 标识为后台管理查询：可查看草稿/待审核/拒绝的游记
        qo.setAdmin(true);
        return R.ok(noteService.queryPage(qo));
    }

    /**
     * 获取游记详细信息（基础字段）
     */
    @RequiresPermissions("business:note:query")
    @GetMapping(value = "/{id}")
    public R<Note> getInfo(@PathVariable("id") Long id) {
        return R.ok(noteService.getById(id));
    }

    /**
     * 获取游记富文本内容
     * 前端：getNoteContent(id)  GET /note/admin/notes/content/{id}
     */
    @RequiresPermissions("business:note:query")
    @GetMapping(value = "/content/{id}")
    public R<NoteContent> getContent(@PathVariable("id") Long id) {
        NoteContent nc = noteContentMapper.selectById(id);
        if (nc == null) {
            nc = new NoteContent();
            nc.setId(id);
            nc.setContent("");
        }
        return R.ok(nc);
    }

    /**
     * 新增游记（后台直接发布，content 通过 Note.content 字段传入）
     * 前端：addNote(data)  POST /note/admin/notes
     */
    @RequiresPermissions("business:note:add")
    @Log(title = "游记", businessType = BusinessType.INSERT)
    @PostMapping
    public R<?> add(@RequestBody Note note) {
        String content = "";
        if (note.getContent() != null) {
            content = note.getContent().getContent();
        }
        return toAjax(noteService.saveNote(note, content != null ? content : ""));
    }

    /**
     * 修改游记（后台编辑，content 通过 Note.content 字段传入）
     * 前端：updateNote(data)  PUT /note/admin/notes
     */
    @RequiresPermissions("business:note:edit")
    @Log(title = "游记", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<?> edit(@RequestBody Note note) {
        String content = note.getContent() == null ? null : note.getContent().getContent();
        return toAjax(noteService.updateNote(note, content));
    }

    /**
     * 审核游记（兼容前端 POST 方式传参）
     * 前端：noteAudit(data)  POST /note/admin/notes/audit
     */
    @RequiresPermissions("business:note:audit")
    @Log(title = "游记审核", businessType = BusinessType.UPDATE)
    @PostMapping("/audit")
    public R<?> audit(@RequestBody Note note) {
        return toAjax(noteService.audit(note.getId(), note.getStatus()));
    }

    /**
     * 删除游记（兼容单个id和多个逗号分隔id）
     * 前端：delNote(id)  DELETE /note/admin/notes/{id}
     */
    @RequiresPermissions("business:note:remove")
    @Log(title = "游记", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @Transactional
    public R<?> remove(@PathVariable Long[] ids) {
        // 同时删除游记内容表记录
        noteContentMapper.deleteBatchIds(Arrays.stream(ids).collect(Collectors.toList()));
        return toAjax(noteService.removeByIds(Arrays.stream(ids).collect(Collectors.toList())));
    }
}
