package cn.wolfcode.wolf2w.business.service;

import cn.wolfcode.wolf2w.business.api.domain.StrategyRank;
import cn.wolfcode.wolf2w.business.query.StrategyRankQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 攻略排行Service接口
 * 
 * @author wzh
 * @date 2026-08-06
 */
public interface IStrategyRankService extends IService<StrategyRank>{
    /**
    * 分页
    * @param qo
    * @return
    */
    IPage<StrategyRank> queryPage(StrategyRankQuery qo);

    /**
     * 根据类型查询排行
     * @param type 类型
     * @return 排行列表
     */
    List<StrategyRank> queryRank(Long type);
}
