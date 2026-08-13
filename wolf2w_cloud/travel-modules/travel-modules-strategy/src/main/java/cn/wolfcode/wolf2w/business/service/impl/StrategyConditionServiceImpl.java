package cn.wolfcode.wolf2w.business.service.impl;

import cn.wolfcode.wolf2w.business.api.domain.StrategyCondition;
import cn.wolfcode.wolf2w.business.api.domain.StrategyRank;
import cn.wolfcode.wolf2w.business.mapper.StrategyConditionMapper;
import cn.wolfcode.wolf2w.business.query.StrategyConditionQuery;
import cn.wolfcode.wolf2w.business.service.IStrategyConditionService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 攻略条件Service业务层处理
 * 
 * @author wzh
 * @date 2026-08-06
 */
@Service
@Transactional
public class StrategyConditionServiceImpl extends ServiceImpl<StrategyConditionMapper,StrategyCondition> implements IStrategyConditionService {


    @Override
    public IPage<StrategyCondition> queryPage(StrategyConditionQuery qo) {
        IPage<StrategyCondition> page = new Page<>(qo.getCurrentPage(), qo.getPageSize());
        return lambdaQuery()
                .page(page);
    }

    @Override
    public List<StrategyCondition> queryConditionList(Long type) {
        return lambdaQuery()
                .inSql(StrategyCondition::getStatisTime, "select max(statis_time) from ta_strategy_condition")
                .eq(StrategyCondition::getType, type)
                .orderByDesc(StrategyCondition::getCount)
                .list();
    }
}
