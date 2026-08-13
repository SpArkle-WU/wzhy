package cn.wolfcode.wolf2w.business.service;

import cn.wolfcode.wolf2w.business.api.domain.StrategyCondition;
import cn.wolfcode.wolf2w.business.api.domain.StrategyRank;
import cn.wolfcode.wolf2w.business.query.StrategyConditionQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 攻略条件Service接口
 * 
 * @author wzh
 * @date 2026-08-06
 */
public interface IStrategyConditionService extends IService<StrategyCondition>{
    /**
    * 分页
    * @param qo
    * @return
    */
    IPage<StrategyCondition> queryPage(StrategyConditionQuery qo);


    /**
     * 查询条件列表
     * @param type 类型
     * @return 条件列表
     */
    List<StrategyCondition> queryConditionList(Long type);
}
