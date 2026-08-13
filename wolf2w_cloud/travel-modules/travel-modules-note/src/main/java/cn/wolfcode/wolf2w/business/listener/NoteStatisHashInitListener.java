package cn.wolfcode.wolf2w.business.listener;

import cn.wolfcode.wolf2w.business.api.domain.Note;
import cn.wolfcode.wolf2w.business.service.INoteService;
import cn.wolfcode.wolf2w.common.redis.service.RedisService;
import cn.wolfcode.wolf2w.common.redis.util.RedisKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游记统计数据初始化监听器
 * 应用启动时，将数据库中所有游记的统计数据预加载到 Redis Hash 中
 */
@Component
public class NoteStatisHashInitListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private RedisService redisService;
    @Autowired
    private INoteService noteService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        System.out.println("游记统计数据初始化完成");

        // 获取所有游记数据
        List<Note> list = noteService.list();
        for (Note note : list) {
            // 拼接 key
            String id = note.getId().toString();
            String key = RedisKeys.NOTE_STATIS_HASH.join(id);

            // 判断 key 是否已存在，避免覆盖已有缓存
            if (redisService.hasKey(key)) {
                continue;
            }

            // 若不存在，将统计数据转换为 map 存储到 Redis 中
            Map<String, Object> map = new HashMap<>();
            map.put("id", note.getId());
            map.put("viewnum", note.getViewnum() != null ? note.getViewnum() : 0);
            map.put("thumbsupnum", note.getThumbsupnum() != null ? note.getThumbsupnum() : 0);
            map.put("replynum", note.getReplynum() != null ? note.getReplynum() : 0);
            map.put("sharenum", note.getSharenum() != null ? note.getSharenum() : 0);
            map.put("favornum", note.getFavornum() != null ? note.getFavornum() : 0);
            redisService.setCacheMap(key, map);
        }
    }
}
