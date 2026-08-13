package cn.wolfcode.wolf2w.business.vo;

import cn.wolfcode.wolf2w.business.api.domain.Note;
import cn.wolfcode.wolf2w.business.api.domain.NoteContent;
import cn.wolfcode.wolf2w.member.api.domain.UserInfo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 游记详情视图对象（继承 Note，避免手动扁平展开）
 * 继承 Note 所有基础字段，只需补充 VO 特有的扩展字段
 */
@Setter
@Getter
public class NoteDetailVO extends Note implements Serializable {

    /** 富文本内容（前端模板：detailData.content.content） */
    private NoteContent content;
    /** 作者信息（detailData.author.nickname / .headImgUrl 等） */
    private UserInfo author;

    /** 出行天数别名（前端模板用 detailData.day，单数） */
    private Integer day;
    /** 跟谁去-中文展示文本 */
    private String personDisplay;

    /** 当前登录用户是否已收藏（前端：favorFlag） */
    private Boolean favorFlag = false;
    /** 当前登录用户是否已关注作者（前端：isFollow） */
    private Boolean isFollow = false;
}
