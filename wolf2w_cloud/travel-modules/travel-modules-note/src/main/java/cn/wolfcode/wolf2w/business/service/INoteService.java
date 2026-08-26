// INoteService.java
package cn.wolfcode.wolf2w.business.service;

import cn.wolfcode.wolf2w.business.api.domain.Note;
import cn.wolfcode.wolf2w.business.query.NoteQuery;
import cn.wolfcode.wolf2w.business.vo.NoteDetailVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface INoteService extends IService<Note> {

    /** 分页查询游记 */
    IPage<Note> queryPage(NoteQuery qo);

    /** 游记详情（聚合关联数据） */
    NoteDetailVO detail(Long id);

    /** 保存游记（同时保存内容） */
    boolean saveNote(Note note, String content);

    /** 更新游记（同时更新内容） */
    boolean updateNote(Note note, String content);

    /** 审核游记 */
    boolean audit(Long id, String status);

    /** 查询目的地下的游记（阅读量 top N） */
    List<Note> queryByDestId(Long destId, int limit);

    /** 查询热门游记（收藏数 top N，用于轮播图） */
    List<Note> queryBannerList(int limit);

    /** 全局搜索游记（按关键字匹配标题或概要，limit 限制条数，null 表示分页查询） */
    List<Note> search(String keyword, Integer limit);

    // ==================== Redis 计数相关 ====================

    /** 浏览量 + 1 */
    Map<String, Object> viewnumIncr(Long nid);

    /** 评论数 + 1 */
    Map<String, Object> replynumIncr(Long nid);

    /** 收藏/取消收藏（切换状态） */
    void favor(Long nid);

    /** 点赞（每天最多 5 次） */
    Map<String, Object> thumbsup(Long nid);

    /** Redis 统计数据持久化到数据库 */
    void statisHashMapPersist();

    /** 取消收藏 */
    void unFavor(Long nid);
}