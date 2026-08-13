// RemoteNoteService.java
package cn.wolfcode.wolf2w.business.api;

import cn.wolfcode.wolf2w.business.api.domain.Note;
import cn.wolfcode.wolf2w.business.api.factory.RemoteNoteFallbackFactory;
import cn.wolfcode.wolf2w.common.core.constant.SecurityConstants;
import cn.wolfcode.wolf2w.common.core.constant.ServiceNameConstants;
import cn.wolfcode.wolf2w.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        contextId = "RemoteNoteService",
        name = ServiceNameConstants.NOTE_SERVICE,
        fallbackFactory = RemoteNoteFallbackFactory.class
)
public interface RemoteNoteService {

    @GetMapping("/notes/feign/list")
    R<List<Note>> list(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/notes/feign/{id}")
    R<Note> getOne(@PathVariable("id") Long id, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/notes/viewnumTop")
    R<List<Note>> viewnumTop(@RequestParam("destId") Long destId);

    @GetMapping("/notes/bannerList")
    R<List<Note>> queryBannerList();

    @GetMapping("/notes/search")
    R<List<Note>> search(@RequestParam("keyword") String keyword,
                         @RequestParam(value = "limit", required = false) Integer limit);

    /**
     * 游记统计数据持久化到数据库
     */
    @PostMapping("/notes/feign/statisHashMapPersist")
    R<?> statisHashMapPersist(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}