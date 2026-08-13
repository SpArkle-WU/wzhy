package cn.wolfcode.wolf2w.business.controller.admin;

import cn.wolfcode.wolf2w.business.api.domain.Note;
import cn.wolfcode.wolf2w.business.query.NoteQuery;
import cn.wolfcode.wolf2w.business.service.INoteService;
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
     * 获取游记详细信息
     */
    @RequiresPermissions("business:note:query")
    @GetMapping(value = "/{id}")
    public R<Note> getInfo(@PathVariable("id") Long id) {
        return R.ok(noteService.getById(id));
    }

    /**
     * 审核游记
     */
    @RequiresPermissions("business:note:audit")
    @Log(title = "游记审核", businessType = BusinessType.UPDATE)
    @PutMapping("/audit")
    public R<?> audit(@RequestParam Long id, @RequestParam String status) {
        return toAjax(noteService.audit(id, status));
    }

    /**
     * 删除游记
     */
    @RequiresPermissions("business:note:remove")
    @Log(title = "游记", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<?> remove(@PathVariable Long[] ids) {
        return toAjax(noteService.removeByIds(Arrays.stream(ids).collect(Collectors.toList())));
    }
}