package cn.wolfcode.wolf2w.business.service.impl;

import cn.wolfcode.wolf2w.business.api.domain.StrategyCatalog;
import cn.wolfcode.wolf2w.business.mapper.StrategyCatalogMapper;
import cn.wolfcode.wolf2w.business.query.StrategyCatalogQuery;
import cn.wolfcode.wolf2w.business.service.IStrategyCatalogService;
import cn.wolfcode.wolf2w.business.vo.CatalogVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 攻略分类Service业务层处理
 * 
 * @author wzh
 * @date 2026-08-06
 */
@Service
@Transactional
public class StrategyCatalogServiceImpl extends ServiceImpl<StrategyCatalogMapper,StrategyCatalog> implements IStrategyCatalogService {

    @Override
    public IPage<StrategyCatalog> queryPage(StrategyCatalogQuery qo) {
        IPage<StrategyCatalog> page = new Page<>(qo.getCurrentPage(), qo.getPageSize());
        return lambdaQuery()
                .page(page);
    }

    @Override
    public List<CatalogVO> listGroup() {
        // 分组查询，根据目的名称分组
        QueryWrapper<StrategyCatalog> wrapper = new QueryWrapper<>();
        wrapper.groupBy("dest_name");
        wrapper.select("dest_name destName, GROUP_CONCAT(id) ids, GROUP_CONCAT(name) names");

        // 使用selectMaps接收自定义查询字段，不能用selectList
        List<Map<String, Object>> mapList = baseMapper.selectMaps(wrapper);
        List<CatalogVO> vos = new ArrayList<>();

        // 遍历map组装VO
        for (Map<String, Object> map : mapList) {
            String destName = (String) map.get("destName");
            String ids = (String) map.get("ids");
            String names = (String) map.get("names");

            // 解析id和name字符串为数组
            String[] idArr = ids.split(",");
            String[] nameArr = names.split(",");

            // 构造StrategyCatalog列表
            List<StrategyCatalog> catalogList = new ArrayList<>();
            for (int i = 0; i < idArr.length; i++) {
                StrategyCatalog catalog = new StrategyCatalog();
                catalog.setId(Long.valueOf(idArr[i]));
                catalog.setName(nameArr[i]);
                catalogList.add(catalog);
            }
            // 构造CatalogVO，根据你的构造方法传参
            CatalogVO vo = new CatalogVO(destName, catalogList);
            vos.add(vo);
        }
        return vos;
    }
}
