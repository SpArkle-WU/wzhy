package cn.wolfcode.wolf2w.business.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 问答结果引用的知识来源。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiSource {

    private String chunkId;
    private String sourceType;
    private Long sourceId;
    private Integer chunkNo;
    private String title;
}
