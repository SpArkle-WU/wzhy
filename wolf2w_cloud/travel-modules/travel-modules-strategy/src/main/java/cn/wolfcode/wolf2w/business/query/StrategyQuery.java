package cn.wolfcode.wolf2w.business.query;


import cn.wolfcode.wolf2w.common.core.query.QueryObject;
import lombok.Getter;
import lombok.Setter;

/**
* 状态，0表示待发布，1表示发布查询参数封装对象
*/
@Setter
@Getter
public class StrategyQuery extends  QueryObject{

    // 目的地id 1国内 2国外 3主题
    private Long type;
    // type=1 || type=2 refid 为目的地id type=3 refid 为主题id
    private Long refid;
    // 排序字段，如 viewnum / thumbsupnum / favornum / replynum / sharenum / createTime
    private String orderBy;
}
