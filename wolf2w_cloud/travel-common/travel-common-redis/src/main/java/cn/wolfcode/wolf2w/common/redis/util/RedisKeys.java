package cn.wolfcode.wolf2w.common.redis.util;

import lombok.Getter;

/**
 * 枚举 类型 --> 给自己创建对象,别人无法构造,构造方法私有
 */
@Getter
public enum RedisKeys {

    // 攻略点击统计Hash表
    STRATEGY_STATIS_HASH("strategy_statis_hash", -1),
    // 用户攻略收藏统计Set表
    STRATEGY_FAVOR_SET("strategy_favor_set", -1),
    // 用户攻略点赞统计List表,多key(sid,uid)
    USER_STRATEGY_THUMBSUP("user_strategy_thumbsup", 60 * 60 * 24),

    // 攻略的RabbitMQ队列数据,ZSet表
    STRATEGY_RABBITMQ_ZSET("strategy_rabbitmq_zset", -1),

    // 游记统计Hash表
    NOTE_STATIS_HASH("note_statis_hash", -1),
    // 用户游记收藏统计Set表
    NOTE_FAVOR_SET("note_favor_set", -1),
    // 用户游记点赞统计,多key(nid,uid),24小时过期
    USER_NOTE_THUMBSUP("user_note_thumbsup", 60 * 60 * 24),

    // 枚举值 --> 因为外界无法创建对象,所以必须调用自己的构造方法先创建枚举对象
    VERIFY_CODE("verify_code:", 60 * 5);
    // key 前缀
    private String prefix;
    // 过期时间
    private long expire;

    // 构造方法
    private RedisKeys(String prefix, long expire) {
        this.prefix = prefix;
        this.expire = expire;
    }


    /**
     * 对外提供方法 --> 外界拼接获取key
     * @param values  参数列表
     * @return 拼接后的key
     */
    public String join(String...values) {
        StringBuffer sb = new StringBuffer();
        sb.append(prefix);
        for (String value : values) {
            sb.append(":");
            sb.append(value);
        }
        return sb.toString();
    }

}