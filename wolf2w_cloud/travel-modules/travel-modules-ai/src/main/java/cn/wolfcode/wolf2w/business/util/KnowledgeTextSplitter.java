package cn.wolfcode.wolf2w.business.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识文本分割工具类
 */
@Component
public class KnowledgeTextSplitter {

    public List<String> split(String html, int chunkSize, int overlap) {
        String text = html == null ? "" : html
                .replaceAll("<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .trim();

        List<String> chunks = new ArrayList<>();
        if (text.isEmpty()) {
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());

            // 尽量在句末切开，避免语义被截成两半。
            int sentenceEnd = text.lastIndexOf('。', end);
            if (sentenceEnd > start + chunkSize / 2) {
                end = sentenceEnd + 1;
            }

            chunks.add(text.substring(start, end));
            if (end >= text.length()) {
                break;
            }

            // overlap 让相邻知识块共享部分上下文。
            start = Math.max(start + 1, end - overlap);
        }
        return chunks;
    }
}