package cn.wolfcode.wolf2w.business.api.factory;

import cn.wolfcode.wolf2w.business.api.RemoteNoteCommentService;
import cn.wolfcode.wolf2w.business.api.domain.NoteComment;
import cn.wolfcode.wolf2w.common.core.domain.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 游记评论 远程服务降级回调
 *
 * @author wzh
 * @date 2026-08-10
 */
@Component
public class RemoteNoteCommentFallbackFactory implements FallbackFactory<RemoteNoteCommentService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteNoteCommentFallbackFactory.class);

    @Override
    public RemoteNoteCommentService create(Throwable cause) {
        log.error("游记评论服务调用失败: {}", cause.getMessage());
        return new RemoteNoteCommentService() {
            @Override
            public R<List<NoteComment>> list(String source) {
                return R.fail("查询游记评论列表失败:" + cause.getMessage());
            }

            @Override
            public R<NoteComment> getOne(Long id, String source) {
                return R.fail("查询游记评论详情失败:" + cause.getMessage());
            }
        };
    }
}
