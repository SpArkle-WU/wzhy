package cn.wolfcode.wolf2w.business.listener;

import cn.wolfcode.wolf2w.business.api.domain.Strategy;
import cn.wolfcode.wolf2w.business.service.IStrategyService;
import cn.wolfcode.wolf2w.common.redis.service.RedisService;
import cn.wolfcode.wolf2w.common.redis.util.RedisKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 统计攻略数据初始化完成事件监听器
@Component
public class StatisHashInitListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private RedisService redisService;
    @Autowired
    private IStrategyService strategyService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        System.out.println("统计攻略数据初始化完成");

        // 获取所有攻略数据
        List<Strategy> list = strategyService.list();
        for (Strategy strategy : list) {
            // 拼接key
            String id = strategy.getId().toString();
            String key = RedisKeys.STRATEGY_STATIS_HASH.join(id);

            // 判断key是否存在
            if (redisService.hasKey(key)) {
                continue;
            }
            // 若不存在,则将数据转换为map,存储到redis中
            Map<String, Object> map = new HashMap<>();
            map.put("id", strategy.getId());
            map.put("viewnum", strategy.getViewnum().intValue());
            map.put("thumbsupnum", strategy.getThumbsupnum().intValue());
            map.put("replynum", strategy.getReplynum().intValue());
            map.put("sharenum", strategy.getSharenum().intValue());
            map.put("favornum", strategy.getFavornum().intValue());
            redisService.setCacheMap(key, map);
        }
    }
}
