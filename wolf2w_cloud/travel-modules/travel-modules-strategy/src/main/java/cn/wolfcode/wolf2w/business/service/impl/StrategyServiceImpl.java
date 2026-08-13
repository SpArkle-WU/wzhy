package cn.wolfcode.wolf2w.business.service.impl;

import cn.wolfcode.wolf2w.business.api.RemoteDestinationService;
import cn.wolfcode.wolf2w.business.api.domain.*;
import cn.wolfcode.wolf2w.business.mapper.*;
import cn.wolfcode.wolf2w.business.query.StrategyQuery;
import cn.wolfcode.wolf2w.business.service.IStrategyConditionService;
import cn.wolfcode.wolf2w.business.service.IStrategyRankService;
import cn.wolfcode.wolf2w.business.service.IStrategyService;
import cn.wolfcode.wolf2w.business.util.DateUtil;
import cn.wolfcode.wolf2w.business.vo.ThemeVO;
import cn.wolfcode.wolf2w.common.core.utils.DateUtils;
import cn.wolfcode.wolf2w.common.redis.service.RedisService;
import cn.wolfcode.wolf2w.common.redis.util.RedisKeys;
import cn.wolfcode.wolf2w.common.security.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.netty.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 攻略Service业务层处理
 *
 * @author wzh
 * @date 2026-08-06
 */
@Service
@Transactional
public class StrategyServiceImpl extends ServiceImpl<StrategyMapper, Strategy> implements IStrategyService {

    @Autowired
    private StrategyCatalogMapper strategyCatalogMapper;
    @Autowired
    private IStrategyRankService strategyRankService;
    @Autowired
    private IStrategyConditionService strategyConditionService;
    @Autowired
    private StrategyThemeMapper strategyThemeMapper;
    @Autowired
    private RemoteDestinationService remoteDestinationService;
    @Autowired
    private StrategyContentMapper strategyContentMapper;
    @Autowired
    private RedisService redisService;


    // 高级查询
    @Override
    public IPage<Strategy> queryPage(StrategyQuery qo) {
        IPage<Strategy> page = new Page<>(qo.getCurrentPage(), qo.getPageSize());
        // 构建查询条件
        LambdaQueryWrapper<Strategy> queryWrapper = new LambdaQueryWrapper<>();
        if (qo.getType() != null) {
            if (qo.getType().equals(1L) || qo.getType().equals(2L)) {
                queryWrapper.eq(Strategy::getDestId, qo.getRefid());
            } else {
                queryWrapper.eq(Strategy::getThemeId, qo.getRefid());
            }
        }
        // 分页查询
        return page(page, queryWrapper);
    }

    // 查询攻略分类列表
    @Override
    public List<StrategyCatalog> queryCatalogListByDestId(Long destId) {
        LambdaQueryWrapper<StrategyCatalog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StrategyCatalog::getDestId, destId);
        // 查询攻略分类列表
        List<StrategyCatalog> list = strategyCatalogMapper.selectList(queryWrapper);
        // 遍历list，根据分类id，查询攻略列表，设置到list中
        for (StrategyCatalog catalog : list) {
            Long id = catalog.getId();
            List<Strategy> strategies = lambdaQuery().eq(Strategy::getCatalogId, id).list();
            catalog.setStrategies(strategies);
        }
        return list;

    }

    // 查询点击量前3的分类
    @Override
    public List<Strategy> queryViewnnumTop3(Long destId) {
        // 查询攻略分类列表中，点击量前3的分类
        return lambdaQuery()
                .eq(Strategy::getDestId, destId)
                .orderByDesc(Strategy::getViewnum)
                .last("limit 3")
                .list();
    }

    // 统计攻略排名
    @Override
    public void statisRank() {
        // 统一统计时间，当前时间
        Date now = new Date();
        // 统计攻略排名
        System.out.println("攻略排行榜统计排名");

        // 1.查国内攻略排名 (0,2L)
        List<Strategy> domList = lambdaQuery()
                .eq(Strategy::getIsabroad, 0)
                .orderByDesc(Strategy::getViewnum)
                .last("limit 10")
                .list();
        List<StrategyRank> domRank = new ArrayList<>();
        addRank(domList, now, domRank, 2L);
        if (!domRank.isEmpty()) {
            strategyRankService.saveBatch(domRank);
        }

        // 2.查国外攻略排名 (1,1L)
        List<Strategy> abroadList = lambdaQuery()
                .eq(Strategy::getIsabroad, 1)
                .orderByDesc(Strategy::getViewnum)
                .last("limit 10")
                .list();

        List<StrategyRank> abroadRank = new ArrayList<>();
        addRank(abroadList, now, abroadRank, 1L);
        if (!abroadRank.isEmpty()) {
            strategyRankService.saveBatch(abroadRank);
        }

        // 3.查热门攻略排名 (3)
        List<Strategy> hotList = lambdaQuery()
                .orderByDesc(Strategy::getViewnum)
                .last("limit 10")
                .list();
        List<StrategyRank> hotRank = new ArrayList<>();
        addRank(hotList, now, hotRank, 3L);
        if (!hotRank.isEmpty()) {
            strategyRankService.saveBatch(hotRank);
        }
    }

    // 提取攻略排名方法
    private static void addRank(List<Strategy> strategyList, Date now, List<StrategyRank> rankList, Long type) {
        for (Strategy strategy : strategyList) {
            // 转成 StrategyRank 对象
            StrategyRank rank = new StrategyRank();
            rank.setDestId(strategy.getId());
            rank.setDestName(strategy.getDestName());
            rank.setStatisnum(strategy.getViewnum());
            rank.setStatisTime(now);
            rank.setStrategyId(strategy.getId());   // 确认是否真的需要
            rank.setStrategyTitle(strategy.getSubTitle());
            rank.setType(type);
            rankList.add(rank);
        }
    }

    // 查询攻略主题列表 ，根据主题名称分组
    // 方法一：两次查询，第一次根据主题名称分组，第二次根据目的地id分组，设置到ThemeVO中dests中
    // 方法二：一次查询，根据主题名称分组，根据点击量前3的攻略，设置到ThemeVO中
    @Override
    public List<ThemeVO> queryThemeList() {

        /* 方法一:N+1次查询效率低
        List<ThemeVO> themeVOList = new ArrayList<>();
        // 1.查询主题列表
        List<Strategy> themeList = query().select("distinct theme_name").list();
       // 2.根据主题名称分组，查询目的地id列表
        for (Strategy s1 : themeList) {
            String themeName = s1.getThemeName();
            List<Strategy> destList = query().select("distinct dest_id, dest_name")
                    .eq("theme_name", themeName)
                    .list();
           // 3.根据目的地id分组，查询目的地列表
            List<Destination> dests = new ArrayList<>();
            for (Strategy s2 : destList) {
                Destination dest = new Destination();
                dest.setId(s2.getId());
                dest.setName(s2.getDestName());
                dests.add(dest);
            }
            // 4.设置到VO中
            ThemeVO themeVO = new ThemeVO();
            themeVO.setThemeName(themeName);
            themeVO.setDests(dests);
            themeVOList.add(themeVO);
        }
        return themeVOList;
    }*/

        // 方法二：一次查询，利用 MySQL 的 GROUP_CONCAT 聚合
        // 使用 QueryWrapper 支持 SQL 片段
        QueryWrapper<Strategy> wrapper = new QueryWrapper<>();
        wrapper.select(
                "theme_name as themeName",
                "GROUP_CONCAT(DISTINCT dest_id) as ids",
                "GROUP_CONCAT(DISTINCT dest_name) as names"
        );
        wrapper.groupBy("theme_name");

        List<Map<String, Object>> maps = baseMapper.selectMaps(wrapper);
        List<ThemeVO> themeVOList = new ArrayList<>();

        for (Map<String, Object> map : maps) {
            String themeName = (String) map.get("themeName");
            String idsStr = (String) map.get("ids");
            String namesStr = (String) map.get("names");

            ThemeVO themeVO = new ThemeVO();
            themeVO.setThemeName(themeName);

            List<Destination> dests = new ArrayList<>();
            if (idsStr != null && namesStr != null && !idsStr.isEmpty()) {
                String[] idArray = idsStr.split(",");
                String[] nameArray = namesStr.split(",");
                for (int i = 0; i < idArray.length; i++) {
                    Destination dest = new Destination();
                    dest.setId(Long.parseLong(idArray[i].trim()));
                    dest.setName(nameArray[i].trim());
                    dests.add(dest);
                }
            }

            themeVO.setDests(dests);
            themeVOList.add(themeVO);
        }
        return themeVOList;
    }

    // 统计攻略条件导航数据
    @Override
    public void statisCondition() {
        System.out.println("统计攻略条件导航数据");
        // 当前时间
        Date now = new Date();
        List<StrategyCondition> strategyList = new ArrayList<>();

        // 构造条件查询，根据国外攻略分组
        QueryWrapper<Strategy> wrapper = new QueryWrapper<>();
        wrapper.eq("isabroad", 1);
        wrapper.groupBy("dest_id,dest_name");
        wrapper.select("dest_id refid,dest_name name,count(1) count");
        List<Map<String, Object>> maps = baseMapper.selectMaps(wrapper);
        // 1. 查询国外攻略分组，设置到StrategyCondition中
        addContion(maps, 1, now, strategyList);

        wrapper.clear();
        wrapper.eq("isabroad", 0);
        wrapper.groupBy("dest_id,dest_name");
        wrapper.select("dest_id refid,dest_name name,count(1) count");
        maps = baseMapper.selectMaps(wrapper);
        // 2. 查询国内攻略分组，设置到StrategyCondition中
        addContion(maps, 2, now, strategyList);

        wrapper.clear();
        wrapper.groupBy("theme_id,theme_name");
        wrapper.select("theme_id refid,theme_name name,count(1) count");
        maps = baseMapper.selectMaps(wrapper);
        // 3. 查询主题攻略分组，设置到StrategyCondition中
        addContion(maps, 3, now, strategyList);

        // 4. 批量保存到数据库
        if (!strategyList.isEmpty()) {
            strategyConditionService.saveBatch(strategyList);
        }
    }

    // 新增攻略
    @Transactional
    @Override
    public int insert(Strategy strategy) {
        // 1.StrategyThemeMapper 查询主题id
        StrategyTheme strategyTheme = strategyThemeMapper.selectById(strategy.getThemeId());
        if (strategyTheme == null) {
            throw new RuntimeException("主题不存在");
        }
        // 2. 设置主题名称
        strategy.setThemeName(strategyTheme.getName());

        // 3. StrategyCatalogMapper 查询分类id
        StrategyCatalog strategyCatalog = strategyCatalogMapper.selectById(strategy.getCatalogId());
        if (strategyCatalog == null) {
            throw new RuntimeException("分类不存在");
        }
        // 4. 设置分类名称
        strategy.setCatalogName(strategyCatalog.getName());

        // 5. 设置目的id和名称
        strategy.setDestName(strategyCatalog.getDestName());
        strategy.setDestId(strategyCatalog.getDestId());

        // 6.设置时间
        strategy.setCreateTime(new Date());

        // 7.判断国内国外,调用目的地服务判断
        Boolean isAbroad = remoteDestinationService.isAbroad(strategyCatalog.getDestId(), "inner").getData();
        strategy.setIsabroad(isAbroad ? 1L : 0L);

        // 8.设置其他字段
        strategy.setViewnum(0L);
        strategy.setReplynum(0L);
        strategy.setSharenum(0L);
        strategy.setFavornum(0L);
        strategy.setThumbsupnum(0L);

        // 9.插入攻略主表数据
        baseMapper.insert(strategy);

        // 10. 插入攻略内容表数据(大字段分离：共享主键)
        Long id = strategy.getId();
        String content = strategy.getContent().getContent();
        StrategyContent strategyContent = new StrategyContent();
        strategyContent.setId(id);
        strategyContent.setContent(content);
        return strategyContentMapper.insert(strategyContent);

    }

    // 点击量 + 1
    @Override
    public Map<String, Object> viewnumIncr(Long sid) {
        return incrementCount(sid, "viewnum", 1);
    }

    // 评论攻略 + 1
    @Override
    public Map<String, Object> replynumIncr(Long sid) {
        return incrementCount(sid, "replynum", 1);
    }

    // 统计攻略数据持久化到数据库
    @Override
    public void statisHashMapPersist() {
        System.out.println("统计攻略数据持久化到数据库");

        // 1. 拼接Redis键 --> strategy_status_hash:* （匹配所有攻略）
        String key = RedisKeys.STRATEGY_STATIS_HASH.join("*");

        // 2. 获取所有攻略的key
        Collection<String> keys = redisService.keys(key);

        if (keys.isEmpty()) {
            return;
        }

        // 3. 遍历所有攻略的key
        for (String k : keys) {
            // 从缓存中获取完整数据
            Map<String, Object> cacheMap = redisService.getCacheMap(k);
            Long id = (Long) cacheMap.get("id");
            Integer viewnum = (Integer) cacheMap.get("viewnum");
            Integer thumbsupnum = (Integer) cacheMap.get("thumbsupnum");
            Integer replynum = (Integer) cacheMap.get("replynum");
            Integer sharenum = (Integer) cacheMap.get("sharenum");
            Integer favornum = (Integer) cacheMap.get("favornum");

            // 4. 更新到数据库（原子操作）,使用lambdaUpdate链式调用
            lambdaUpdate()
                    .eq(Strategy::getId, id)
                    .set(Strategy::getViewnum, viewnum)
                    .set(Strategy::getThumbsupnum, thumbsupnum)
                    .set(Strategy::getReplynum, replynum)
                    .set(Strategy::getSharenum, sharenum)
                    .set(Strategy::getFavornum, favornum)
                    .update();
        }

    }

    // 攻略收藏 + 1/取消收藏 - 1
    @Override
    public Map<String, Object> favor(Long sid) {

        Long userId = SecurityUtils.getUserId();
        // 用户Id拼接key去Redis检查key是否存在
        String key = RedisKeys.STRATEGY_FAVOR_SET.join(userId.toString());
        // 攻略id拼接key去Redis检查key是否存在
        String statisHashKey = RedisKeys.STRATEGY_STATIS_HASH.join(sid.toString());

        Boolean result = null;
        // 直接判断集合中是否包含该攻略（底层工具类做判空即可）
        if (redisService.isCacheSetContains(key, sid)) {
            // 取消收藏
            redisService.incrementCacheMapValue(statisHashKey, "favornum", -1);
            result = false;
            redisService.deleteCacheSetValue(key, sid); // 确保调用的是删除
        } else {
            // 收藏
            redisService.incrementCacheMapValue(statisHashKey, "favornum", 1);
            result = true;
            redisService.addCacheSetValue(key, sid); // SADD 会自动创建 key
        }

        Map<String, Object> cacheMap = redisService.getCacheMap(statisHashKey);
        cacheMap.put("result", result);
        return cacheMap;
    }

    // 收藏数据初始化
    @Override
    public Boolean isUserFavor(Long sid, Long uid) {

        String key = RedisKeys.STRATEGY_FAVOR_SET.join(uid.toString());
        return redisService.isCacheSetContains(key,sid);
    }

    // 点赞,一天只能点赞5次,过期时间1天
    @Override
    public Map<String, Object> thumbsup(Long sid) {

        Long userId = SecurityUtils.getUserId();
        String key = RedisKeys.USER_STRATEGY_THUMBSUP.join(sid.toString(), userId.toString());
        if ( ! redisService.hasKey(key)) {
            Date now = new Date();
            Date endTime = DateUtil.getEndDate(now);
            long expireTime = DateUtil.getBetweenDate(now, endTime);
            // 如果是0秒，说明是当前时间，设置为1秒,不然会报错redis:expire time is negative，因为过期时间不能为0
            expireTime = expireTime == 0 ? 1 : expireTime;

            redisService.setCacheObject(key, 0, expireTime, TimeUnit.SECONDS);
        }
        Long ret = redisService.incrementCacheObjectValue(key, 1);
        Boolean result = null;
        String statisKey = RedisKeys.STRATEGY_STATIS_HASH.join(sid.toString());
        if(ret > 5){
            //今天点赞操作5次，点赞失败
            result = false;
        }else{
            //点赞成功
            result = true;
            redisService.incrementCacheMapValue(statisKey, "thumbsupnum", 1);
        }
        Map<String, Object> cacheMap = redisService.getCacheMap(statisKey);
        cacheMap.put("result", result);
        return cacheMap;

    }


// ==================== 提取的公共方法 ====================

    /**
     * 通用计数增量（支持 +/- 任意值）
     *
     * @param sid   攻略ID
     * @param field 统计字段名，如 "viewnum"、"thumbsupnum"、"replynum"、"sharenum"、"favornum"
     * @param delta 增量（正数加，负数减）
     * @return 更新后的完整统计 Map（包含 id 及所有计数字段）
     */
    private Map<String, Object> incrementCount(Long sid, String field, int delta) {
        String key = RedisKeys.STRATEGY_STATIS_HASH.join(sid.toString());

        // 1. 获取缓存中的数据，并检查是否完整
        Map<String, Object> map = redisService.getCacheMap(key);
        if (!isMapComplete(map)) {
            // 数据不完整（或不存在），从数据库加载完整数据并写入缓存
            map = loadAndCacheFromDB(sid, key);
        }

        // 2. 原子增加指定字段（底层使用 HINCRBY，保证线程安全）
        redisService.incrementCacheMapValue(key, field, delta);

        // 3. 重新获取最新完整数据返回（确保返回的 Map 包含所有字段的最新值）
        return redisService.getCacheMap(key);
    }

    /**
     * 检查 Map 是否包含所有必要的统计字段和 id
     */
    private boolean isMapComplete(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return false;
        }
        return map.containsKey("id")
                && map.containsKey("viewnum")
                && map.containsKey("thumbsupnum")
                && map.containsKey("replynum")
                && map.containsKey("sharenum")
                && map.containsKey("favornum");
    }

    /**
     * 从数据库加载攻略完整统计信息，并写入 Redis 缓存
     */
    private Map<String, Object> loadAndCacheFromDB(Long sid, String key) {
        Strategy strategy = baseMapper.selectById(sid);
        Map<String, Object> map = new HashMap<>();
        map.put("id", strategy.getId());
        map.put("viewnum", strategy.getViewnum().intValue());
        map.put("thumbsupnum", strategy.getThumbsupnum().intValue());
        map.put("replynum", strategy.getReplynum().intValue());
        map.put("sharenum", strategy.getSharenum().intValue());
        map.put("favornum", strategy.getFavornum().intValue());
        redisService.setCacheMap(key, map);
        return map;
    }

    // 提取方法 遍历maps,设置到StrategyCondition中
    private static void addContion(List<Map<String, Object>> maps, long type, Date now, List<StrategyCondition> strategyList) {
        for (Map<String, Object> map : maps) {
            Long refid = (Long) map.get("refid");
            String name = (String) map.get("name");
            Long count = (Long) map.get("count");
            StrategyCondition strategyCondition = new StrategyCondition();
            strategyCondition.setRefid(refid);
            strategyCondition.setName(name);
            strategyCondition.setCount(count);
            strategyCondition.setType(type);
            strategyCondition.setStatisTime(now);
            strategyList.add(strategyCondition);
        }
    }

}

