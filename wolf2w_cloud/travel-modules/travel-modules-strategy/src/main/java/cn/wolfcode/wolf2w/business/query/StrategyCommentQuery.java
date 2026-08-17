package cn.wolfcode.wolf2w.business.query;


import cn.wolfcode.wolf2w.common.core.query.QueryObject;
import lombok.Getter;
import lombok.Setter;

/**
* 点赞用户id查询参数封装对象
*/
@Setter
@Getter
public class StrategyCommentQuery extends  QueryObject{
    /**
     * 攻略id
     */
    private Long strategyId;

    /**
     * 状态 0正常 1禁用
     */
    private Long state;
}
