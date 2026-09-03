package cn.wolfcode.wolf2w.business.listener;

import cn.wolfcode.wolf2w.business.api.domain.Strategy;
import cn.wolfcode.wolf2w.business.api.domain.StrategyCanal;
import cn.wolfcode.wolf2w.business.api.domain.StrategyChangeMessage;
import cn.wolfcode.wolf2w.business.api.domain.StrategyES;
import cn.wolfcode.wolf2w.business.service.Impl.StrategyESServiceImpl;
import cn.wolfcode.wolf2w.common.rabbit.config.RabbitConfig;
import cn.wolfcode.wolf2w.common.redis.service.RedisService;
import cn.wolfcode.wolf2w.common.redis.util.RedisKeys;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@Component
public class TravelRabbitListener {

    private static final Logger log = LoggerFactory.getLogger(TravelRabbitListener.class);

    @Autowired
    private ElasticsearchClient client;
    @Autowired
    private RedisService redisService;

    @RabbitListener(queues = {RabbitConfig.QUEUE_NAME})
    public void receive(String message) throws InvocationTargetException, IllegalAccessException, IOException {

        if (message == null || message.trim().isEmpty()) {
            log.warn("Ignore empty RabbitMQ message");
            return;
        }

        JSONObject payload;
        try {
            payload = JSON.parseObject(message);
        } catch (RuntimeException ex) {
            log.error("Ignore malformed RabbitMQ message: {}", message, ex);
            return;
        }
        if (payload == null) {
            log.warn("Ignore null RabbitMQ payload");
            return;
        }
        String operation = payload.getString("operation");
        if (operation != null && payload.containsKey("strategy")) {
            StrategyChangeMessage event;
            try {
                event = JSON.parseObject(message, StrategyChangeMessage.class);
            } catch (RuntimeException ex) {
                log.error("Ignore malformed strategy change message: {}", message, ex);
                return;
            }
            StrategyCanal strategy = event.getStrategy();
            if (strategy == null || strategy.getId() == null) {
                return;
            }
            if ("DELETE".equalsIgnoreCase(operation)) {
                client.delete(d -> d.index(StrategyESServiceImpl.INDEX_NAME)
                        .id(strategy.getId().toString()));
                return;
            }

            StrategyES strategyES = new StrategyES();
            BeanUtils.copyProperties(strategy, strategyES);
            client.index(i -> i.index(StrategyESServiceImpl.INDEX_NAME)
                    .id(strategyES.getId().toString()).document(strategyES));
            return;
        }

        // Backward-compatible handling for messages sent directly by strategy service.
        Strategy strategy = JSON.parseObject(message, Strategy.class);
        if (strategy == null || strategy.getId() == null) {
            log.warn("Ignore strategy message without id: {}", message);
            return;
        }

        StrategyES strategyES = new StrategyES();
        // 属性拷贝(org.apache.commons.beanutils.BeanUtils: 第一个参数是 dest 目标, 第二个是 orig 源)
        // org.springframework.beans.BeanUtils.相反，第一个参数是源，第二个参数是目标
        BeanUtils.copyProperties(strategy, strategyES);

        // 生成索引
        client.index(i -> i.index(StrategyESServiceImpl.INDEX_NAME).id(strategyES.getId().toString()).document(strategyES));
        // 清空缓存
        String key = RedisKeys.STRATEGY_RABBITMQ_ZSET.getPrefix();
        redisService.deleteCacheZSetValue(key, message);

    }

}
