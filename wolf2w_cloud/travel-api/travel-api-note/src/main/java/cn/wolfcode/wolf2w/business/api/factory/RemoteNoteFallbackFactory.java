package cn.wolfcode.wolf2w.business.api.factory;

import cn.wolfcode.wolf2w.business.api.RemoteNoteService;
import cn.wolfcode.wolf2w.business.api.domain.Note;
import cn.wolfcode.wolf2w.common.core.domain.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RemoteNoteFallbackFactory implements FallbackFactory<RemoteNoteService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteNoteFallbackFactory.class);

    @Override
    public RemoteNoteService create(Throwable cause) {
        log.error("游记服务调用失败: {}", cause.getMessage());
        return new RemoteNoteService() {
            @Override
            public R<List<Note>> list(String source) {
                return R.fail("获取游记列表失败:" + cause.getMessage());
            }

            @Override
            public R<Note> getOne(Long id, String source) {
                return R.fail("获取游记详情失败:" + cause.getMessage());
            }

            @Override
            public R<List<Note>> viewnumTop(Long destId) {
                return R.fail("获取目的地游记失败:" + cause.getMessage());
            }

            @Override
            public R<List<Note>> queryBannerList() {
                return R.fail("获取首页轮播游记失败:" + cause.getMessage());
            }

            @Override
            public R<List<Note>> search(String keyword, Integer limit) {
                return R.fail("搜索游记失败:" + cause.getMessage());
            }

            @Override
            public R<?> statisHashMapPersist(String source) {
                return R.fail("游记统计数据持久化失败:" + cause.getMessage());
            }
        };
    }
}