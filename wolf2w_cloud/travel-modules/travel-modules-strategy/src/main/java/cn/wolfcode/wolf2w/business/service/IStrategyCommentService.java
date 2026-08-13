package cn.wolfcode.wolf2w.business.service;

import cn.wolfcode.wolf2w.business.api.domain.StrategyComment;
import cn.wolfcode.wolf2w.business.query.StrategyCommentQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 攻略评论Service接口
 * 
 * @author wzh
 * @date 2026-08-06
 */
public interface IStrategyCommentService extends IService<StrategyComment>{
    /**
    * 分页
    * @param qo
    * @return
    */
    IPage<StrategyComment> queryPage(StrategyCommentQuery qo);

    /**
     * 新增 评论内容
     * @param strategyComment 评论内容
     */
    void addContent(StrategyComment strategyComment);
}
