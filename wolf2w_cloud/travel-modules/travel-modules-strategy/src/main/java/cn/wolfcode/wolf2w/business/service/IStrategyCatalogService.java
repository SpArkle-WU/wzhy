package cn.wolfcode.wolf2w.business.service;

import cn.wolfcode.wolf2w.business.api.domain.StrategyCatalog;
import cn.wolfcode.wolf2w.business.query.StrategyCatalogQuery;
import cn.wolfcode.wolf2w.business.vo.CatalogVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 攻略分类Service接口
 * 
 * @author wzh
 * @date 2026-08-06
 */
public interface IStrategyCatalogService extends IService<StrategyCatalog>{
    /**
    * 分页
    * @param qo
    * @return 分页结果
    */
    IPage<StrategyCatalog> queryPage(StrategyCatalogQuery qo);

    /**
     * 查询所有攻略分类
     * @return 分类列表
     */
    List<CatalogVO> listGroup();
}
