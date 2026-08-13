package cn.wolfcode.wolf2w.business.service;

import cn.wolfcode.wolf2w.business.api.domain.Strategy;
import cn.wolfcode.wolf2w.business.api.domain.StrategyCatalog;
import cn.wolfcode.wolf2w.business.query.StrategyQuery;
import cn.wolfcode.wolf2w.business.vo.ThemeVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 攻略Service接口
 * 
 * @author wzh
 * @date 2026-08-06
 */
public interface IStrategyService extends IService<Strategy>{
    /**
    * 分页
    * @param qo
    * @return
    */
    IPage<Strategy> queryPage(StrategyQuery qo);

    /**
     * 攻略列表
     * @param destId 目标id
     * @return 攻略列表
     */
    List<StrategyCatalog> queryCatalogListByDestId(Long destId);

    /**
     * 查询攻略分类列表中，点击量前3的分类
     *
     * @param destId 目标id
     * @return 查询分类列表中，点击量前3的分类
     */
    List<Strategy> queryViewnnumTop3(Long destId);

    /**
     * 统计攻略排名
     * 需要根据策略的点击量、收藏量、评论量等指标进行统计
     */
    void statisRank();

    /**
     * 查询攻略主题列表
     * @return 查询攻略主题列表
     */
    List<ThemeVO> queryThemeList();

    /**
     * 统计攻略条件导航数据
     */
    void statisCondition();

    /**
     * 新增攻略
     * @param strategy 新增的攻略
     */
    int insert(Strategy strategy);

    /**
     * 点击量 + 1
     * @param sid 策略id
     */
    Map<String, Object> viewnumIncr(Long sid);

    /**
     * 评论攻略 + 1
     * @param sid 攻略id
     */
     Map<String, Object> replynumIncr(Long sid);

     /**
      * 统计攻略数据持久化到数据库
      */
     void statisHashMapPersist();

     /**
      * 攻略收藏 + 1/取消收藏 - 1
      * @param sid 攻略id
      *
      */
    Map<String, Object> favor(Long sid);

     /**
      * 收藏数据初始化
      * @param sid 攻略id
      * @param uid 用户id
      * @return 是否收藏
      */
    Boolean isUserFavor(Long sid, Long uid);

     /**
      * 攻略点赞 + 1/取消点赞 - 1
      * @param sid 攻略id
      * @return 点赞结果
      */
    Map<String, Object> thumbsup(Long sid);
}
