package cn.wolfcode.wolf2w.business.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * 攻略评论状态修改参数。
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class StrategyCommentStatusRequest {

    private Long id;

    /** 状态：0 正常，1 禁用。 */
    private Long state;
}
