package cn.wolfcode.wolf2w.business.service;

import cn.wolfcode.wolf2w.business.domain.AiAnswer;

/**
 * 检索增强问答服务。
 */
public interface IAiAnswerService {

    AiAnswer ask(String question, Integer topK);
}
