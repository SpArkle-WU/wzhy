package cn.wolfcode.wolf2w.business.api.domain;

import cn.wolfcode.wolf2w.common.core.annotation.Excel;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 游记内容对象 ta_note_content
 *
 * @author wzh
 * @date 2026-08-10
 */
@Data
@TableName("ta_note_content")
public class NoteContent implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键（非自增，与 ta_note.id 对应） */
    @TableId
    private Long id;

    /** 游记内容 */
    @Excel(name = "游记内容")
    private String content;
}