package cn.wolfcode.wolf2w.business.controller;

import cn.wolfcode.wolf2w.business.api.domain.StrategyRank;
import cn.wolfcode.wolf2w.business.service.IStrategyRankService;
import cn.wolfcode.wolf2w.common.core.domain.R;
import cn.wolfcode.wolf2w.common.security.annotation.InnerAuth;
import cn.wolfcode.wolf2w.business.api.domain.StrategyCondition;
import cn.wolfcode.wolf2w.business.query.StrategyConditionQuery;
import cn.wolfcode.wolf2w.business.service.IStrategyConditionService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 攻略条件 Controller
 * 
 * @author wzh
 * @date 2026-08-06
 */
@RestController
@RequestMapping("strategyConditions")
public class StrategyConditionController {
    @Autowired
    private IStrategyConditionService strategyConditionService;
    @Autowired
    private IStrategyRankService strategyRankService;
    /**
     * 攻略条件详情
     */
    @GetMapping("/detail/{id}")
    public R<StrategyCondition> detail(@PathVariable Long id) {
        StrategyCondition strategyCondition = strategyConditionService.getById(id);
        return R.ok(strategyCondition);
    }
    /**
     * 攻略条件 列表
     */
    @GetMapping("/query")
    public R<IPage<StrategyCondition>> query(StrategyConditionQuery qo) {
        IPage<StrategyCondition> page = strategyConditionService.queryPage(qo);
        return R.ok(page);
    }


    /**
     * 攻略条件 列表
     * @param type 类型
     * @return 条件列表
     */
    @GetMapping("/condition")
    public R<List<StrategyCondition>> getList(@RequestParam Long type) {
        List<StrategyCondition> list = strategyConditionService.queryConditionList(type);
        return R.ok(list);
    }


    /*****************************************对外暴露Fegin接口**********************************************/
    /**
     * Feign 接口
     */
    @GetMapping("/feign/list")
    public R<List<StrategyCondition>> feignList() {
        return R.ok(strategyConditionService.list());
    }
    @InnerAuth
    @GetMapping("/feign/{id}")
    public R<StrategyCondition> feignGet(@PathVariable Long id) {
        return R.ok(strategyConditionService.getById(id));
    }
}
