package cn.wolfcode.wolf2w.business.api.domain;

import cn.wolfcode.wolf2w.common.core.annotation.Excel;
import cn.wolfcode.wolf2w.member.api.domain.UserInfo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 游记对象 ta_note
 *
 * @author wzh
 * @date 2026-08-10
 */
@Data
@TableName("ta_note")
public class Note implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 目的地id */
    @Excel(name = "目的地id")
    private Long destId;

    /** 目的地 */
    @Excel(name = "目的地")
    private String destName;

    /** 作者id */
    @Excel(name = "作者id")
    private Long authorId;

    /**
     * 作者昵称（冗余，列表/详情页不用再查 userInfo；如果数据库 ta_note 表还没加该列，先标记为非数据库字段）
     * 需要时执行：ALTER TABLE ta_note ADD COLUMN author_nickname VARCHAR(64) DEFAULT NULL COMMENT '作者昵称' AFTER author_id;
     */
    @Excel(name = "作者昵称")
    @TableField(exist = false)
    private String authorNickname;

    /**
     * 作者头像（冗余，列表/详情页不用再查 userInfo；如果数据库 ta_note 表还没加该列，先标记为非数据库字段）
     * 需要时执行：ALTER TABLE ta_note ADD COLUMN author_head_img_url VARCHAR(255) DEFAULT NULL COMMENT '作者头像' AFTER author_nickname;
     */
    @Excel(name = "作者头像")
    @TableField(exist = false)
    private String authorHeadImgUrl;

    /** 标题 */
    @Excel(name = "标题")
    private String title;

    /** 概要 */
    @Excel(name = "概要")
    private String summary;

    /** 封面 */
    @Excel(name = "封面")
    private String coverUrl;

    /** 旅游时间 */
    @Excel(name = "旅游时间")
    private Date travelTime;

    /** 人均消费 */
    @Excel(name = "人均消费")
    private Integer avgConsume;

    /** 旅游天数 */
    @Excel(name = "旅游天数")
    private Integer days;

    /** 和谁旅游 */
    @Excel(name = "和谁旅游")
    private String person;

    /** 创建时间 */
    private Date createTime;

    /** 发布时间 */
    private Date releaseTime;

    /** 最新更新时间 */
    private Date updateTime;

    /** 是否公开 0:否 1:是 */
    @Excel(name = "是否公开")
    private String isPublic;

    /** 回复数 */
    @Excel(name = "回复数")
    private Integer replynum;

    /** 收藏数 */
    @Excel(name = "收藏数")
    private Integer favornum;

    /** 分享数 */
    @Excel(name = "分享数")
    private Integer sharenum;

    /** 点赞数 */
    @Excel(name = "点赞数")
    private Integer thumbsupnum;

    /** 浏览数 */
    @Excel(name = "浏览数")
    private Integer viewnum;

    /** 游记状态 0:草稿 1:待发布 2:审核通过 3:拒绝 */
    @Excel(name = "游记状态")
    private String status;

    /**
     * 游记富文本内容（非数据库字段，仅用于前端 /add 接口传参接收）
     * 真正的内容存 ta_note_content 表（NoteContent 实体）
     */
    @TableField(exist = false)
    private NoteContent content;

    /**
     * 作者信息（非数据库字段，列表页通过 authorId 关联查询后填充，供前端显示头像与昵称）
     */
    @TableField(exist = false)
    private UserInfo author;
}