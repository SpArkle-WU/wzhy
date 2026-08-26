package cn.wolfcode.wolf2w.business.listener;

import cn.wolfcode.wolf2w.business.api.domain.Strategy;
import cn.wolfcode.wolf2w.business.api.domain.StrategyES;
import cn.wolfcode.wolf2w.business.service.Impl.StrategyESServiceImpl;
import cn.wolfcode.wolf2w.common.rabbit.config.RabbitConfig;
import cn.wolfcode.wolf2w.common.redis.service.RedisService;
import cn.wolfcode.wolf2w.common.redis.util.RedisKeys;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.alibaba.fastjson.JSON;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@Component
public class TravelRabbitListener {


    @Autowired
    private ElasticsearchClient client;
    @Autowired
    private RedisService redisService;

    @RabbitListener(queues = {RabbitConfig.QUEUE_NAME})
    public void receive(String message) throws InvocationTargetException, IllegalAccessException, IOException {

        // 将json字符串还原成对象
        Strategy strategy = JSON.parseObject(message, Strategy.class);

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
