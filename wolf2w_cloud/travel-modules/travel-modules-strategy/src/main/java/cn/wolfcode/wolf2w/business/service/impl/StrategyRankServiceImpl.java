package cn.wolfcode.wolf2w.business.service.impl;

import cn.wolfcode.wolf2w.business.api.domain.StrategyRank;
import cn.wolfcode.wolf2w.business.mapper.StrategyRankMapper;
import cn.wolfcode.wolf2w.business.query.StrategyRankQuery;
import cn.wolfcode.wolf2w.business.service.IStrategyRankService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 攻略排行Service业务层处理
 * 
 * @author wzh
 * @date 2026-08-06
 */
@Service
@Transactional
public class StrategyRankServiceImpl extends ServiceImpl<StrategyRankMapper,StrategyRank> implements IStrategyRankService {

    @Override
    public IPage<StrategyRank> queryPage(StrategyRankQuery qo) {
        IPage<StrategyRank> page = new Page<>(qo.getCurrentPage(), qo.getPageSize());
        return lambdaQuery()
                .page(page);
    }

    // 查询攻略排行
    @Override
    public List<StrategyRank> queryRank(Long type) {

        LambdaQueryWrapper<StrategyRank> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.apply("statis_time = (select max(statis_time) from ta_strategy_rank where type = {0})", type);
        queryWrapper.eq(StrategyRank::getType,type);
        queryWrapper.orderByAsc(StrategyRank::getStatisnum);
        queryWrapper.last("limit 10");
        return list(queryWrapper);
    }
}
