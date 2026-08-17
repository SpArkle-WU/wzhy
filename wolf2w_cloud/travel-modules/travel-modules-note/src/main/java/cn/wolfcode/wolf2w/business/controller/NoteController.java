package cn.wolfcode.wolf2w.business.controller;

import cn.wolfcode.wolf2w.business.api.domain.Note;
import cn.wolfcode.wolf2w.business.api.domain.NoteComment;
import cn.wolfcode.wolf2w.business.query.NoteQuery;
import cn.wolfcode.wolf2w.business.service.INoteService;
import cn.wolfcode.wolf2w.business.vo.NoteDetailVO;
import cn.wolfcode.wolf2w.common.core.domain.R;
import cn.wolfcode.wolf2w.common.security.annotation.InnerAuth;
import cn.wolfcode.wolf2w.common.security.annotation.RequiresLogin;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 游记 Controller
 *
 * @author wzh
 * @date 2026-08-10
 */
@RestController
@RequestMapping("/notes")
public class NoteController {

    @Autowired
    private INoteService noteService;

    /**
     * 游记列表（分页 + 多条件筛选）
     */
    @GetMapping("/query")
    public R<IPage<Note>> query(NoteQuery qo) {
        IPage<Note> page = noteService.queryPage(qo);
        return R.ok(page);
    }

    /**
     * 游记详情（聚合关联数据，同时浏览量 +1）
     */
    @GetMapping("/detail/{id}")
    public R<NoteDetailVO> detail(@PathVariable Long id) {
        return R.ok(noteService.detail(id));
    }

    /**
     * 发布游记
     */
    @PostMapping("/save")
    public R<?> save(@RequestBody Note note, @RequestParam String content) {
        return R.ok(noteService.saveNote(note, content));
    }

    /**
     * 更新游记
     */
    @PutMapping("/update")
    public R<?> update(@RequestBody Note note, @RequestParam String content) {
        return R.ok(noteService.updateNote(note, content));
    }

    /**
     * 目的地下阅读量 top 游记
     */
    @GetMapping("/viewnumTop")
    public R<List<Note>> viewnumTop(Long destId) {
        return R.ok(noteService.queryByDestId(destId, 5));
    }

    /**
     * 目的地下阅读量 top 游记（前端命名 viewnnumTop3 双 n）
     * 与 viewnumTop 等价，默认返回前 3 条
     */
    @GetMapping("/viewnnumTop3")
    public R<List<Note>> viewnnumTop3(Long destId,
                                      @RequestParam(required = false) Integer limit) {
        return R.ok(noteService.queryByDestId(destId, limit != null ? limit : 3));
    }

    /**
     * 首页轮播推荐游记（近2个月收藏数 top5）
     */
    @GetMapping("/bannerList")
    public R<List<Note>> bannerList() {
        return R.ok(noteService.queryBannerList(5));
    }

    // ==================== Redis 计数接口 ====================

    /**
     * 点赞（每天最多 5 次）——前端 POST /note/notes/star/{id}
     */
    @RequiresLogin
    @PostMapping("/star/{id}")
    public R<Map<String, Object>> star(@PathVariable Long id) {
        return R.ok(noteService.thumbsup(id));
    }

    /**
     * 收藏——前端 POST /note/notes/collect/{id}
     */
    @RequiresLogin
    @PostMapping("/collect/{id}")
    public R<Void> collect(@PathVariable Long id) {
        noteService.favor(id);
        return R.ok();
    }

    /**
     * 取消收藏——前端 POST /note/notes/uncollect/{id}
     */
    @RequiresLogin
    @PostMapping("/uncollect/{id}")
    public R<Void> uncollect(@PathVariable Long id) {
        noteService.unFavor(id);
        return R.ok();
    }

    /**
     * 游记新增别名（前端 POST /note/notes/add），与 save 等价。
     * 注意：save 是 @RequestBody Note + @RequestParam content
     *       add 是 @RequestBody Note（前端传 JSON，content 放在 body 内）
     */
    @RequiresLogin
    @PostMapping("/add")
    public R<?> add(@RequestBody Note note) {
        String content = note.getContent() != null ? note.getContent().getContent() : null;
        return R.ok(noteService.saveNote(note, content != null ? content : ""));
    }

    /**
     * 全局搜索游记（按关键字匹配标题或概要）
     * @param keyword 关键字
     * @param limit   限制条数（可选，用于"全部搜索"页前5条；不传则返回全部）
     */
    @GetMapping("/search")
    public R<List<Note>> search(@RequestParam String keyword,
                                @RequestParam(required = false) Integer limit) {
        return R.ok(noteService.search(keyword, limit));
    }

    /*****************************************对外暴露 Feign 接口**********************************************/

    @GetMapping("/feign/list")
    public R<List<Note>> feignList() {
        return R.ok(noteService.list());
    }

    @InnerAuth
    @GetMapping("/feign/{id}")
    public R<Note> feignGet(@PathVariable Long id) {
        return R.ok(noteService.getById(id));
    }

    /**
     * 游记统计数据持久化到数据库
     */
    @PostMapping("/feign/statisHashMapPersist")
    R<?> statisHashMapPersist() {
        noteService.statisHashMapPersist();
        return R.ok();
    }
}