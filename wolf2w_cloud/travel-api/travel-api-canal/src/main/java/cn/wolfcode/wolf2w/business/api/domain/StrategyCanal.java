package cn.wolfcode.wolf2w.business.api.domain;

import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Date;

/**
 * 攻略对象 ta_strategy
 * Canal想监听谁就放谁的domain.这里以攻略为例,@Column 注解指定数据库字段名,默认是属性名
 * @author wzh
 * @date 2026-08-06
 */
@Data
public class StrategyCanal implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */

    @Column(name = "id")
    private Long id;

    /**
     * 引用目的地ID
     */
    @Column(name = "dest_id")
    private Long destId;

    /**
     * 引用目的地名称
     */
    @Column(name = "dest_name")
    private String destName;

    /**
     * 引用主题ID
     */
    @Column(name = "theme_id")
    private Long themeId;

    /**
     * 引用主题名称
     */
    @Column(name = "theme_name")
    private String themeName;

    /**
     * 引用分类ID
     */
    @Column(name = "catalog_id")
    private Long catalogId;

    /**
     * 引用分类名称
     */
    @Column(name = "catalog_name")
    private String catalogName;

    /**
     * 标题
     */
    @Column(name = "title")
    private String title;

    /**
     * 副标题
     */
    @Column(name = "sub_title")
    private String subTitle;

    /**
     * 内容概要
     */
    @Column(name = "summary")
    private String summary;

    /**
     * 封面图片地址
     */
    @Column(name = "cover_url")
    private String coverUrl;

    /**
     * 是否为国外，0表示国内，1表示国外
     */
    @Column(name = "is_abroad")
    private Long isabroad;

    /**
     * 点击数
     */
    @Column(name = "viewnum")
    private Long viewnum;

    /**
     * 攻略评论数
     */
    @Column(name = "replynum")
    private Long replynum;

    /**
     * 收藏数
     */
    @Column(name = "favornum")
    private Long favornum;

    /**
     * 分享数
     */
    @Column(name = "sharenum")
    private Long sharenum;

    /**
     * 点赞数
     */
    @Column(name = "thumbsupnum")
    private Long thumbsupnum;

    /**
     * 状态，0表示待发布，1表示发布
     */
    @Column(name = "state")
    private Long state;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;
}
