package cn.wolfcode.wolf2w.business.api.factory;

import cn.wolfcode.wolf2w.business.api.RemoteStrategyService;
import cn.wolfcode.wolf2w.common.core.domain.R;
import cn.wolfcode.wolf2w.business.api.domain.Strategy;
import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 攻略 远程服务降级回调
 *
 * @author wzh
 * @date 2026-08-06
 */
@Component
public class RemoteStrategyFallbackFactory implements FallbackFactory<RemoteStrategyService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteStrategyFallbackFactory.class);

    @Override
    public RemoteStrategyService create(Throwable throwable) {
        log.error("攻略服务调用失败:{}", throwable.getMessage());

        return new RemoteStrategyService() {
            @Override
            public R<List<Strategy>> list(String source) {
                return R.fail(Lists.newArrayList(), "查询攻略列表信息失败:" + throwable.getMessage());
            }

            @Override
            public R<Strategy> getOne(Long id, String source) {
                return R.fail("查询攻略信息失败:" + throwable.getMessage());
            }

            // 断路器回调,降级处理,返回默认值,而不是抛出异常,导致服务不可用
            @Override
            public R<?> statisRank(String source) {
                return R.fail("统计攻略排名失败:" + throwable.getMessage());
            }

            @Override
            public R<?> statisCondition(String source) {
                return R.fail("统计攻略条件导航失败:" + throwable.getMessage());
            }

            @Override
            public R<?> statisHashMapPersist(String source) {
                return R.fail("攻略数据持久化到数据库失败:" + throwable.getMessage());
            }
        };
    }
}
