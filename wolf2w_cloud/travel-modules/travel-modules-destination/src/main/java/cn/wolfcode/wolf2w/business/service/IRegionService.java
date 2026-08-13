package cn.wolfcode.wolf2w.business.service;

import cn.wolfcode.wolf2w.business.api.domain.Region;
import cn.wolfcode.wolf2w.business.query.RegionQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 区域Service接口
 * 
 * @author wzh
 * @date 2026-08-06
 */
public interface IRegionService extends IService<Region>{
    /**
    * 分页
    * @param qo
    * @return
    */
    IPage<Region> queryPage(RegionQuery qo);

    /**
     * 查询热门区域
     *
     * @param isHot 是否热门
     * @return 列表
     */
    List<Region> queryRegions(Long ishot);
}
