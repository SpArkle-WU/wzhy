package cn.wolfcode.wolf2w.common.rabbit.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    // 队列
    public static final String QUEUE_NAME = "travel-queue";
    // 交换机
    public static final String EXCHANGE_NAME = "travel-exchange";
    // 路由键
    public static final String ROUTING_KEY = "travel-routing-key";
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true, false, false);
    }

    @Bean
    public Exchange exchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    // 绑定
    @Bean
    public Binding binding(Queue queue, Exchange exchange) {
    return new Binding(QUEUE_NAME, Binding.DestinationType.QUEUE, EXCHANGE_NAME,ROUTING_KEY, null);
    }
}
