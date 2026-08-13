package cn.wolfcode.wolf2w.business.controller;

import cn.wolfcode.wolf2w.business.api.RemoteRegionService;
import cn.wolfcode.wolf2w.business.api.domain.Region;
import cn.wolfcode.wolf2w.business.service.IRegionService;
import cn.wolfcode.wolf2w.common.core.constant.SecurityConstants;
import cn.wolfcode.wolf2w.common.core.domain.R;
import cn.wolfcode.wolf2w.common.security.annotation.InnerAuth;
import cn.wolfcode.wolf2w.business.api.domain.Destination;
import cn.wolfcode.wolf2w.business.query.DestinationQuery;
import cn.wolfcode.wolf2w.business.service.IDestinationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 目的地 Controller
 * 
 * @author wzh
 * @date 2026-08-06
 */
@RestController
@RequestMapping("destinations")
public class DestinationController {
    @Autowired
    private IDestinationService destinationService;
    @Autowired
    private IRegionService regionService;
    /**
     * 目的地详情
     */
    @GetMapping("/detail/{id}")
    public R<Destination> detail(@PathVariable Long id) {
        Destination destination = destinationService.getById(id);
        return R.ok(destination);
    }
    /**
     * 目的地 列表
     */
    @GetMapping("/query")
    public R<IPage<Destination>> query(DestinationQuery qo) {
        IPage<Destination> page = destinationService.queryPage(qo);
        return R.ok(page);
    }

    /**
     * 获取热门目的地
     * @param ishot 是否热门
     * @return 列表
     */
    @GetMapping("/regions")
    public R<List<Region>> regions(Long ishot) {
        return R.ok(regionService.queryRegions(ishot));
    }

    /**
     * 搜索目的地
     * @param regionId 区域id
     * @return 列表
     */
    @GetMapping("/search")
    public R<List<Destination>> search(Long regionId) {
        return R.ok(destinationService.queryDestinationsByRegionId(regionId));
    }

    /**
     * 获取所有目的地列表 toasts
     * @return 列表
     */
    @GetMapping("/toasts")
    public R<List<Destination>> toasts(Long destId) {
        return R.ok(destinationService.queryToastsByDestId(destId));
    }


    /*****************************************对外暴露Fegin接口**********************************************/
    /**
     * Feign 接口
     */
    @GetMapping("/feign/list")
    public R<List<Destination>> feignList() {
        return R.ok(destinationService.list());
    }
    @InnerAuth
    @GetMapping("/feign/{id}")
    public R<Destination> feignGet(@PathVariable Long id) {
        return R.ok(destinationService.getById(id));
    }

    // 判断国内国外(StrategyCatalogAdminController调用)
    @GetMapping("/feign/isabroad/{id}")
    public R<Boolean> isAbroad(@PathVariable("id") Long id) {
        // 调用ToastService获取目的地详情
        List<Destination> toasts = destinationService.queryToastsByDestId(id);
        // 取出第一个
        Destination toast = toasts.get(0);
        return R.ok(toast.getName().equals("中国")? Boolean.FALSE : Boolean.TRUE);
    }

}
