package cn.wolfcode.wolf2w.business.client;

import cn.wolfcode.wolf2w.business.domain.ChatMessage;

import java.util.List;

public interface ChatClient {

    /**
     * 调用兼容 OpenAI 协议的聊天模型，返回模型生成的文本。
     */
    String chat(List<ChatMessage> messages);
}
