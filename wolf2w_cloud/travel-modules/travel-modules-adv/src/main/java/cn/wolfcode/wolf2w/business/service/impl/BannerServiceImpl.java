package cn.wolfcode.wolf2w.business.service.impl;

import cn.wolfcode.wolf2w.business.api.domain.Banner;
import cn.wolfcode.wolf2w.business.mapper.BannerMapper;
import cn.wolfcode.wolf2w.business.query.BannerQuery;
import cn.wolfcode.wolf2w.business.service.IBannerService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 文章推荐Service业务层处理
 * 
 * @author wzh
 * @date 2026-08-06
 */
@Service
@Transactional
public class BannerServiceImpl extends ServiceImpl<BannerMapper,Banner> implements IBannerService {

    @Override
    public IPage<Banner> queryPage(BannerQuery qo) {
        IPage<Banner> page = new Page<>(qo.getCurrentPage(), qo.getPageSize());
        return lambdaQuery()
                .page(page);
    }

    @Override
    public List<Banner> queryBannerByType(Integer type) {
        return lambdaQuery()
                .eq(Banner::getState, 0)            // 只查启用中的
                .eq(Banner::getType, type)              // 按类型过滤：1游记 2攻略
                .orderByAsc(Banner::getSeq)             // 按序号从小到大（后台可配置展示顺序）
                .last("limit 5")                  // banner 最多 5 条
                .list();
    }
}
