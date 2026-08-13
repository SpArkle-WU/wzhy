package cn.wolfcode.wolf2w.business.service;

import cn.wolfcode.wolf2w.business.api.domain.NoteComment;
import cn.wolfcode.wolf2w.business.query.NoteCommentQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface INoteCommentService extends IService<NoteComment> {

    /** 分页查询评论 */
    IPage<NoteComment> queryPage(NoteCommentQuery qo);

    /** 发表评论（设置创建时间/状态/游记标题冗余，并更新游记回复数） */
    void addContent(NoteComment comment);
}
