package cn.wolfcode.wolf2w.business.api.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 策略表Canal事件消息类 ，用于表示策略表的Canal事件，包含操作类型和策略数据。
 */
@Data
public class StrategyChangeMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String operation;

    private StrategyCanal strategy;

    public StrategyChangeMessage() {
    }

    public StrategyChangeMessage(String operation, StrategyCanal strategy) {
        this.operation = operation;
        this.strategy = strategy;
    }
}
