package cn.wolfcode.wolf2w.business.api;

import cn.wolfcode.wolf2w.business.api.domain.Strategy;
import cn.wolfcode.wolf2w.common.core.constant.SecurityConstants;
import cn.wolfcode.wolf2w.common.core.domain.R;
import cn.wolfcode.wolf2w.business.api.factory.RemoteStrategyFallbackFactory;
import cn.wolfcode.wolf2w.common.core.constant.ServiceNameConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* 攻略 远程服务
 *
 * @author wzh
 * @date 2026-08-06
 */
@FeignClient(contextId = "RemoteStrategyService", name = ServiceNameConstants.STRATEGY_SERVICE, fallbackFactory = RemoteStrategyFallbackFactory.class)
public interface RemoteStrategyService {

    @GetMapping("/strategies/feign/list")
    R<List<Strategy>> list(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/strategies/feign/{id}")
    R<Strategy> getOne(@PathVariable("id") Long id, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 统计攻略排名
     * 需要根据策略的点击量、收藏量、评论量等指标进行统计
     * Fegin 动态代理发送HTTP Post 请求 到travel-modules-strategy 服务的 /strategies/feign/statisRank 接口
     * 带 from-source=inner 标识内部调用
     */
    @PostMapping("/strategies/feign/statisRank")
    R<?> statisRank(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PostMapping("/strategies/feign/statisCondition")
    R<?> statisCondition(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 攻略数据持久化到数据库
     */
    @PostMapping("/strategies/feign/statisHashMapPersist")
    R<?> statisHashMapPersist(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PostMapping("/strategies/feign/checkRabbitMQMessage")
    R<?> checkRabbitMQMessage(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
