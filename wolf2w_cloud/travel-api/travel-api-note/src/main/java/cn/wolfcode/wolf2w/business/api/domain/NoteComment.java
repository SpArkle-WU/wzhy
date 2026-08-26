package cn.wolfcode.wolf2w.business.api.domain;

import cn.wolfcode.wolf2w.common.core.annotation.Excel;
import cn.wolfcode.wolf2w.member.api.domain.UserInfo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 游记评论对象 ta_note_comment
 *
 * @author wzh
 * @date 2026-08-10
 */
@Data
@TableName("ta_note_comment")
public class NoteComment implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 游记id */
    @Excel(name = "游记id")
    private Long noteId;

    /** 游记标题 */
    @Excel(name = "游记标题")
    private String noteTitle;

    /** 用户id */
    @Excel(name = "用户id")
    private Long userId;

    /** 评论内容 */
    @Excel(name = "评论内容")
    private String content;

    /** 评论类型 */
    @Excel(name = "评论类型")
    private String type;

    /** 评论状态 */
    @Excel(name = "评论状态")
    private String status;

    /** 关联id（引用回复时记录被回复评论的id；顶级评论 refId 为 null 或 0） */
    @Excel(name = "关联id")
    private Long refId;

    /** 创建时间 */
    @Excel(name = "创建时间")
    private Date createTime;

    /** 用户信息（非数据库字段，关联查询使用） */
    @TableField(exist = false)
    private UserInfo user;

    /** 子回复列表（非数据库字段，查询时根据 refId 树形组装后填充） */
    @TableField(exist = false)
    private List<NoteComment> comments = new ArrayList<>();

    /** 被回复评论所属的用户信息（非数据库字段，展示回复时显示"回复@某某"） */
    @TableField(exist = false)
    private UserInfo refUser;
}