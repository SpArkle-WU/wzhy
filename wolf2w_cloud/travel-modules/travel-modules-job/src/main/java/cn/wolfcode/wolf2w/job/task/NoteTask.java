package cn.wolfcode.wolf2w.job.task;

import cn.wolfcode.wolf2w.business.api.RemoteNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 游记定时任务
 * 通过 Quartz 调度，Feign 调用 note 服务
 * 在 sys_job 表中配置 invokeTarget 为 noteTask.statisHashMapPersist
 */
@Component("noteTask")
public class NoteTask {

    @Autowired
    private RemoteNoteService remoteNoteService;

    /**
     * 游记统计数据持久化到数据库
     * 将 Redis Hash 中的浏览/点赞/评论/收藏等计数刷回 DB
     */
    public void statisHashMapPersist() {
        System.out.println("游记统计数据持久化到数据库");
        remoteNoteService.statisHashMapPersist("inner");
    }
}
