package cn.wolfcode.wolf2w.business.controller;

import cn.wolfcode.wolf2w.business.api.domain.Note;
import cn.wolfcode.wolf2w.business.api.domain.Region;
import cn.wolfcode.wolf2w.business.api.RemoteRegionService;
import cn.wolfcode.wolf2w.business.query.NoteQuery;
import cn.wolfcode.wolf2w.business.service.INoteService;
import cn.wolfcode.wolf2w.common.core.domain.R;
import cn.wolfcode.wolf2w.common.security.annotation.RequiresLogin;
import cn.wolfcode.wolf2w.common.security.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 前台路径兼容 Controller
 * 为前端 note 模块提供两个特殊路径：
 *   GET /travels/user/query   —— 我的游记列表
 *   GET /destinations/regions —— 写游记目的地区域
 * 之所以不在 NoteController（前缀 notes）中放，是因为这俩路径前缀与 notes 不同。
 * 网关统一前缀 /note 会将所有 /note/** 路由到此模块，因此实际
 *   /note/travels/user/query 和 /note/destinations/regions 会命中这里。
 */
@RestController
public class TravelsAliasController {

    @Autowired
    private INoteService noteService;
    @Autowired
    private RemoteRegionService remoteRegionService;

    /**
     * 我的游记——前端 GET /note/travels/user/query
     * 返回当前登录用户发布的游记（分页，走 NoteQuery + authorId 注入）
     */
    @RequiresLogin
    @GetMapping("/travels/user/query")
    public R<IPage<Note>> myTravels(NoteQuery qo) {
        // 强制使用当前用户，避免通过 authorId/admin 查询到其他用户的草稿。
        qo.setAuthorId(SecurityUtils.getUserId());
        qo.setAdmin(true);
        IPage<Note> page = noteService.queryPage(qo);
        return R.ok(page);
    }

    /**
     * 写游记-区域列表——前端 GET /note/destinations/regions
     * 默认返回热门区域 ishot = 1
     * 若 ishot = null 或 != 1，则返回全部区域（RemoteRegionService 没有按 ishot 筛选接口，
     * 这里查全量后在 JVM 过滤即可，区域数据量很小）
     */
    @GetMapping("/destinations/regions")
    public R<List<Region>> regions(@RequestParam(required = false) Long ishot) {
        R<List<Region>> r = remoteRegionService.list("inner");
        if (r == null || r.getCode() != R.SUCCESS || r.getData() == null) {
            return R.ok(java.util.Collections.emptyList());
        }
        List<Region> data = r.getData();
        if (ishot != null && ishot == 1L) {
            data = data.stream()
                    .filter(e -> e.getIshot() != null && e.getIshot() == 1L)
                    .collect(Collectors.toList());
        }
        return R.ok(data);
    }
}
