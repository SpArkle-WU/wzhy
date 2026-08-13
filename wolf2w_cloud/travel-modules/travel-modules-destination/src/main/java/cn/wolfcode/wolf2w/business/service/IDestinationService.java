package cn.wolfcode.wolf2w.business.service;

import cn.wolfcode.wolf2w.business.api.domain.Destination;
import cn.wolfcode.wolf2w.business.query.DestinationQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 目的地Service接口
 * 
 * @author wzh
 * @date 2026-08-06
 */
public interface IDestinationService extends IService<Destination>{
    /**
    * 分页
    * @param qo
    * @return
    */
    IPage<Destination> queryPage(DestinationQuery qo);

    /**
     *  查询目的地
     * @param regionId 区域id
     * @return 列表
     */
    List<Destination> queryDestinationsByRegionId(Long regionId);

    /**
     * 查询目的地 toast
     * @param destId 目的地id
     * @return 列表
     */
    List<Destination> queryToastsByDestId(Long destId);
}
