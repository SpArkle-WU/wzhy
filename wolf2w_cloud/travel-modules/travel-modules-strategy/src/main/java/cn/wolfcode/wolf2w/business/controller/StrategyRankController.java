package cn.wolfcode.wolf2w.business.controller;

import cn.wolfcode.wolf2w.business.service.IStrategyService;
import cn.wolfcode.wolf2w.business.vo.ThemeVO;
import cn.wolfcode.wolf2w.common.core.domain.R;
import cn.wolfcode.wolf2w.common.security.annotation.InnerAuth;
import cn.wolfcode.wolf2w.business.api.domain.StrategyRank;
import cn.wolfcode.wolf2w.business.query.StrategyRankQuery;
import cn.wolfcode.wolf2w.business.service.IStrategyRankService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 攻略排行 Controller
 * 
 * @author wzh
 * @date 2026-08-06
 */
@RestController
@RequestMapping("strategyRanks")
public class StrategyRankController {
    @Autowired
    private IStrategyRankService strategyRankService;
    @Autowired
    private IStrategyService strategyService;
    /**
     * 攻略排行详情
     */
    @GetMapping("/detail/{id}")
    public R<StrategyRank> detail(@PathVariable Long id) {
        StrategyRank strategyRank = strategyRankService.getById(id);
        return R.ok(strategyRank);
    }
    /**
     * 攻略排行 列表
     */
    @GetMapping("/query")
    public R<IPage<StrategyRank>> query(StrategyRankQuery qo) {
        IPage<StrategyRank> page = strategyRankService.queryPage(qo);
        return R.ok(page);
    }

    /**
     * 攻略排行 排行
     * @param type 类型
     * @return 排行列表
     */
    @GetMapping("/rank")
    public R<List<StrategyRank>> getList(@RequestParam Long type) {
        // 直接根据类型查询列表，无需先查单条
        List<StrategyRank> list = strategyRankService.queryRank(type);
        return R.ok(list);
    }
    /**
     * 查询攻略主题列表
     * @return 查询攻略主题列表
     */
    @GetMapping("/theme")
    public R<List<ThemeVO>> themeList() {
        return R.ok(strategyService.queryThemeList());
    }

    /*****************************************对外暴露Fegin接口**********************************************/
    /**
     * Feign 接口
     */
    @GetMapping("/feign/list")
    public R<List<StrategyRank>> feignList() {
        return R.ok(strategyRankService.list());
    }
    @InnerAuth
    @GetMapping("/feign/{id}")
    public R<StrategyRank> feignGet(@PathVariable Long id) {
        return R.ok(strategyRankService.getById(id));
    }
}
