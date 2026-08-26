package cn.wolfcode.wolf2w.business.service;

import cn.wolfcode.wolf2w.business.api.domain.Banner;
import cn.wolfcode.wolf2w.business.query.BannerQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 文章推荐Service接口
 * 
 * @author wzh
 * @date 2026-08-06
 */
public interface IBannerService extends IService<Banner>{
    /**
    * 分页
    * @param qo
    * @return
    */
    IPage<Banner> queryPage(BannerQuery qo);

    /**
     * 按类型查询启用中的 banner 列表（按序号升序，最多 5 条）
     * @param type 类型：1游记 2攻略
     * @return banner 列表
     */
    List<Banner> queryBannerByType(Integer type);
}
