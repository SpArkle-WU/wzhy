package cn.wolfcode.wolf2w.business.domain;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * RAG 问答请求。
 */
@Data
public class AiAskRequest {

    @NotBlank(message = "question 不能为空")
    private String question;

    @Min(value = 1, message = "topK 必须大于等于 1")
    @Max(value = 20, message = "topK 不能大于 20")
    private Integer topK;
}
