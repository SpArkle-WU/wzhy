package cn.wolfcode.wolf2w.business.listener;

import cn.wolfcode.wolf2w.business.api.domain.StrategyCanal;
import cn.wolfcode.wolf2w.business.api.domain.StrategyChangeMessage;
import cn.wolfcode.wolf2w.common.rabbit.config.RabbitConfig;
import com.alibaba.fastjson2.JSON;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

/**
 * 策略表Canal事件处理类 ，负责处理策略表的Canal事件，将事件转换为RabbitMQ消息并发送到指定的队列。
 */
@Component
@CanalTable("ta_strategy")
public class StrategyCanalHandler implements EntryHandler<StrategyCanal> {

    private final AmqpTemplate amqpTemplate;

    public StrategyCanalHandler(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    @Override
    public void insert(StrategyCanal strategy) {
        publish("INSERT", strategy);
    }

    @Override
    public void update(StrategyCanal before, StrategyCanal after) {
        publish("UPDATE", after);
    }

    @Override
    public void delete(StrategyCanal strategy) {
        publish("DELETE", strategy);
    }

    private void publish(String operation, StrategyCanal strategy) {
        if (strategy == null || strategy.getId() == null) {
            return;
        }
        StrategyChangeMessage event = new StrategyChangeMessage(operation, strategy);
        amqpTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_NAME,
                RabbitConfig.ROUTING_KEY,
                JSON.toJSONString(event));
    }
}
