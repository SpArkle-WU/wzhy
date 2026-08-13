package cn.wolfcode.wolf2w.business.vo;

import cn.wolfcode.wolf2w.business.api.domain.StrategyCatalog;
import cn.wolfcode.wolf2w.business.api.domain.StrategyTheme;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
// 前端要的数据结构
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CatalogVO {

    /** 目的id */
    private String destName;
    /** 分类列表 */
    private List<StrategyCatalog> catalogList;
}
