package cn.wolfcode.wolf2w.business.query;

import cn.wolfcode.wolf2w.common.core.query.QueryObject;
import lombok.Getter;
import lombok.Setter;

/**
 * 游记查询参数封装对象
 */
@Setter
@Getter
public class NoteQuery extends QueryObject {

    /** 目的地id */
    private Long destId;

    /**
     * 排序字段（与前端对齐）：
     * viewnum    最热（按浏览数倒序）
     * create_time 最新（按创建时间倒序）
     */
    private String orderBy;

    /**
     * 出发时间段类型：
     * -1 不限 / 1:1-2月 / 2:3-4月 / 3:5-6月 / 4:7-8月 / 5:9-10月 / 6:11-12月
     */
    private String travelTimeType;

    /**
     * 人均花费类型：
     * -1 不限 / 1:1-999 / 2:1K-6K / 3:6K-20K / 4:20K以上
     */
    private String consumeType;

    /**
     * 出行天数类型：
     * -1 不限 / 1:3天以下 / 2:4-7天 / 3:8-14天 / 4:15天以上
     */
    private String dayType;

    /** 作者id（查我的游记时使用） */
    private Long authorId;

    /** 游记状态（后台管理使用） */
    private String status;

    /** 是否后台管理查询（true 时不过滤 is_public/status，可查看草稿/待审核/拒绝的游记） */
    private Boolean admin;
}
