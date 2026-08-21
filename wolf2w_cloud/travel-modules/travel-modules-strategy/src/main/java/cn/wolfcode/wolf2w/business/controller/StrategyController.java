package cn.wolfcode.wolf2w.business.controller;

import cn.wolfcode.wolf2w.business.api.domain.StrategyCatalog;
import cn.wolfcode.wolf2w.business.api.domain.StrategyComment;
import cn.wolfcode.wolf2w.business.api.domain.StrategyContent;
import cn.wolfcode.wolf2w.business.service.IStrategyContentService;
import cn.wolfcode.wolf2w.business.vo.ThemeVO;
import cn.wolfcode.wolf2w.common.core.constant.SecurityConstants;
import cn.wolfcode.wolf2w.common.core.domain.R;
import cn.wolfcode.wolf2w.common.security.annotation.InnerAuth;
import cn.wolfcode.wolf2w.business.api.domain.Strategy;
import cn.wolfcode.wolf2w.business.query.StrategyQuery;
import cn.wolfcode.wolf2w.business.service.IStrategyService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 攻略 Controller
 * 
 * @author wzh
 * @date 2026-08-06
 */
@RestController
@RequestMapping("strategies")
public class StrategyController {
    @Autowired
    private IStrategyService strategyService;
    @Autowired
    private IStrategyContentService strategyContentService;
    /**
     * 攻略详情
     */
    @GetMapping("/detail/{id}")
    public R<Strategy> detail(@PathVariable Long id) {
        // 查询攻略
        Strategy strategy = strategyService.getById(id);
        // 查询攻略评论内容
        StrategyContent strategyContent = strategyContentService.getById(id);
        // 合并攻略评论内容
        strategy.setContent(strategyContent);
        return R.ok(strategy);
    }
    /**
     * 攻略 列表
     */
    @GetMapping("/query")
    public R<IPage<Strategy>> query(StrategyQuery qo) {
        IPage<Strategy> page = strategyService.queryPage(qo);
        return R.ok(page);
    }

    /**
     * 查询攻略分类列表
     * @param destId 目标id
     * @return 查询分类列表
     */
    @GetMapping("/catalogList")
    public R<List<StrategyCatalog>> list(Long destId) {
        return R.ok(strategyService.queryCatalogListByDestId(destId));
    }

    /**
     *  查询攻略分类列表中，点击量前3的分类
     * @param destId 目标id
     * @return 查询分类列表中，点击量前3的分类
     */
    @GetMapping("/viewnnumTop3")
    public R<List<Strategy>> viewnnumTop3(Long destId) {
        return R.ok(strategyService.queryViewnnumTop3(destId));
    }

    /**
     * 攻略点击量 + 1
     */
    @PostMapping("/viewnumIncr")
    public R<Map<String, Object>> viewnumIncr(Long sid) {
        Map<String, Object> map = strategyService.viewnumIncr(sid);
        return R.ok(map);
    }

    /**
     * 攻略评论 + 1
     */
    @PostMapping("/replynumIncr")
    public R<Map<String, Object>> replynumIncr(Long sid) {
        Map<String, Object> map = strategyService.replynumIncr(sid);
        return R.ok(map);
    }

    /**
     * 攻略收藏 + 1/取消收藏 - 1
     */
    @PostMapping("/favor")
    public R<Map<String, Object>> favor(Long sid) {
        Map<String, Object> map = strategyService.favor(sid);
        return R.ok(map);
    }

    /**
     * 收藏数据初始化
     */
    @GetMapping("/isUserFavor")
    public R<Boolean> isUserFavor(Long sid, Long uid) {
        Boolean isFavor = strategyService.isUserFavor(sid, uid);
        return R.ok(isFavor);
    }

    /**
     * 攻略点赞 + 1/取消点赞 - 1
     */
    @PostMapping("/thumbsup")
    public R<Map<String, Object>> thumbsup(Long sid) {
        Map<String, Object> map = strategyService.thumbsup(sid);
        return R.ok(map);
    }



    /*****************************************对外暴露Fegin接口**********************************************/
    /**
     * Feign 接口
     */
    @GetMapping("/feign/list")
    public R<List<Strategy>> feignList() {
        return R.ok(strategyService.list());
    }
    @InnerAuth
    @GetMapping("/feign/{id}")
    public R<Strategy> feignGet(@PathVariable Long id) {
        return R.ok(strategyService.getById(id));
    }

    /**
     * 统计攻略排名
     * 需要根据策略的点击量、收藏量、评论量等指标进行统计
     */
    @PostMapping("/feign/statisRank")
    // 内部调用,服务器内部调用,省去调用
    R<?> statisRank() {
        strategyService.statisRank();
        return R.ok();
    }
    /**
     * 统计攻略条件导航数据
     */
    @PostMapping("/feign/statisCondition")
    R<?> statisCondition() {
        strategyService.statisCondition();
        return R.ok();
    }

    /**
     * 攻略数据持久化到数据库
     */
    @PostMapping("/feign/statisHashMapPersist")
    R<?> statisHashMapPersist() {
        strategyService.statisHashMapPersist();
        return R.ok();
    }
    /**
     * 检查RabbitMQ队列是否有数据
     */
    @PostMapping("/feign/checkRabbitMQMessage")
    R<?> checkRabbitMQMessage() {
        strategyService.checkRabbitMQMessage();
        return R.ok();
    }
}
