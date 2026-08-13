package cn.wolfcode.wolf2w.business.service.impl;

import cn.wolfcode.wolf2w.business.api.domain.Destination;
import cn.wolfcode.wolf2w.business.api.domain.Region;
import cn.wolfcode.wolf2w.business.mapper.DestinationMapper;
import cn.wolfcode.wolf2w.business.mapper.RegionMapper;
import cn.wolfcode.wolf2w.business.query.DestinationQuery;
import cn.wolfcode.wolf2w.business.service.IDestinationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 目的地Service业务层处理
 *
 * @author wzh
 * @date 2026-08-06
 */
@Service
@Transactional
public class DestinationServiceImpl extends ServiceImpl<DestinationMapper, Destination> implements IDestinationService {

    @Autowired
    private RegionMapper regionMapper;

    @Override
    public IPage<Destination> queryPage(DestinationQuery qo) {
        IPage<Destination> page = new Page<>(qo.getCurrentPage(), qo.getPageSize());
        return lambdaQuery()
                .page(page);
    }

    @Override
    public List<Destination> queryDestinationsByRegionId(Long regionId) {

        List<Destination> list = new ArrayList<>();
        if (regionId == -1) {
            list = lambdaQuery()
                    .eq(Destination::getParentId, 1)
                    .list();
        } else {
            Region region = regionMapper.selectById(regionId);
            String refIds = region.getRefIds();
            String[] split = refIds.split(",");
            for (String id : split) {
                list.add(lambdaQuery()
                        .eq(Destination::getId, id)
                        .one());
            }
        }

        // 遍历父级菜单,放入子级目的地集合并显示（取前十个）
        list.forEach(destination -> {
            List<Destination> children = lambdaQuery()
                    .eq(Destination::getParentId, destination.getId())
                    .last("LIMIT 10")  // 在SQL层面限制查询结果数量
                    .list();
            destination.setChildren(children);
        });
        return list;
    }

    @Override
    public List<Destination> queryToastsByDestId(Long destId) {
        // 递归实现
        List<Destination> list = new ArrayList<>();
        createToast(list, destId);
        // 反序输出
        // Collections.reverse(list);
        return list;
    }

    // 自定义递归方法
    private void createToast(List<Destination> list, Long destId) {
        if (destId == null) {
            return;
        }
        Destination destination = lambdaQuery()
                .eq(Destination::getId, destId)
                .one();
        createToast(list, destination.getParentId());
        list.add(destination);
    }
    /* 优化方案:一次性把整张表查出来放到 Map 里，在内存中关联。查询次数降为 1 次，性能提升巨大。
    public List<Destination> queryToastsByDestId(Long destId) {
    // 1. 一次性查询全部数据（或按业务条件过滤）
    List<Destination> allList = lambdaQuery().list();
    // 2. 转成 Map<id, 对象>
    Map<Long, Destination> idMap = allList.stream()
            .collect(Collectors.toMap(Destination::getId, Function.identity()));

    // 3. 从当前节点往上追溯
    List<Destination> result = new ArrayList<>();
    Long currentId = destId;
    while (currentId != null) {
        Destination dest = idMap.get(currentId);
        if (dest == null) break;
        result.add(dest);
        currentId = dest.getParentId();
    }
    Collections.reverse(result);
    return result;
    }
    */
}
