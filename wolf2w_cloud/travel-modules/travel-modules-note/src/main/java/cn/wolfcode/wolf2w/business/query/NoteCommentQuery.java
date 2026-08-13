package cn.wolfcode.wolf2w.business.query;

import cn.wolfcode.wolf2w.common.core.query.QueryObject;
import lombok.Getter;
import lombok.Setter;

/**
 * 游记评论查询参数封装对象
 */
@Setter
@Getter
public class NoteCommentQuery extends QueryObject {

    /** 游记id */
    private Long noteId;

    /** 用户id */
    private Long userId;

    /** 评论类型 */
    private String type;

    /** 评论状态 */
    private String status;
}