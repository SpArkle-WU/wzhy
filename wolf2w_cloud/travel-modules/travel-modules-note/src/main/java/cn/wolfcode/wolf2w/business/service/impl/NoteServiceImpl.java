// NoteServiceImpl.java
package cn.wolfcode.wolf2w.business.service.impl;

import cn.wolfcode.wolf2w.business.api.RemoteDestinationService;
import cn.wolfcode.wolf2w.business.api.RemoteStrategyService;
import cn.wolfcode.wolf2w.business.api.domain.Destination;
import cn.wolfcode.wolf2w.business.api.domain.Note;
import cn.wolfcode.wolf2w.business.api.domain.NoteComment;
import cn.wolfcode.wolf2w.business.api.domain.NoteContent;
import cn.wolfcode.wolf2w.business.api.domain.Strategy;
import cn.wolfcode.wolf2w.business.mapper.NoteContentMapper;
import cn.wolfcode.wolf2w.business.mapper.NoteMapper;
import cn.wolfcode.wolf2w.business.query.NoteCommentQuery;
import cn.wolfcode.wolf2w.business.query.NoteQuery;
import cn.wolfcode.wolf2w.business.service.INoteCommentService;
import cn.wolfcode.wolf2w.business.service.INoteService;
import cn.wolfcode.wolf2w.business.util.DateUtil;
import cn.wolfcode.wolf2w.business.vo.NoteDetailVO;
import cn.wolfcode.wolf2w.common.core.domain.R;
import cn.wolfcode.wolf2w.common.redis.service.RedisService;
import cn.wolfcode.wolf2w.common.redis.util.RedisKeys;
import cn.wolfcode.wolf2w.common.security.utils.SecurityUtils;
import cn.wolfcode.wolf2w.member.api.RemoteUserInfoService;
import cn.wolfcode.wolf2w.member.api.domain.UserInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements INoteService {

    @Autowired
    private NoteContentMapper noteContentMapper;
    @Autowired
    private RemoteUserInfoService remoteUserInfoService;
    @Autowired
    private RemoteDestinationService remoteDestinationService;
    @Autowired
    private RemoteStrategyService remoteStrategyService;
    @Autowired
    private INoteCommentService noteCommentService;
    @Autowired
    private RedisService redisService;

    @Override
    public IPage<Note> queryPage(NoteQuery qo) {
        IPage<Note> page = new Page<>(qo.getCurrentPage(), qo.getPageSize());
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();

        // 后台管理查询时不过滤 is_public/status（可查看草稿/待审核/拒绝的游记）
        // 前台查询时只查公开的（is_public 兼容 '1' 和 'true' 两种历史数据）
        if (!Boolean.TRUE.equals(qo.getAdmin())) {
            wrapper.in(Note::getIsPublic, "1", "true");
        } else {
            // 后台可按状态精确筛选
            if (qo.getStatus() != null) {
                wrapper.eq(Note::getStatus, qo.getStatus());
            }
        }
        // 目的地筛选
        if (qo.getDestId() != null) {
            wrapper.eq(Note::getDestId, qo.getDestId());
        }
        // 作者筛选（我的游记）
        if (qo.getAuthorId() != null) {
            wrapper.eq(Note::getAuthorId, qo.getAuthorId());
        }
        // 出发时间段筛选：travelTimeType 1-6 对应 1-2月、3-4月、5-6月、7-8月、9-10月、11-12月
        if (qo.getTravelTimeType() != null && !"-1".equals(qo.getTravelTimeType())) {
            try {
                int type = Integer.parseInt(qo.getTravelTimeType());
                if (type >= 1 && type <= 6) {
                    int monthStart = (type - 1) * 2 + 1;
                    int monthEnd = type * 2;
                    wrapper.apply("MONTH(travel_time) BETWEEN {0} AND {1}", monthStart, monthEnd);
                }
            } catch (NumberFormatException ignored) {
                // Ignore invalid filter values rather than failing the whole list request.
            }
        }
        // 人均花费筛选：consumeType 1-4 对应 1-999、1K-6K、6K-20K、20K以上
        if (qo.getConsumeType() != null && !"-1".equals(qo.getConsumeType())) {
            switch (qo.getConsumeType()) {
                case "1":
                    wrapper.between(Note::getAvgConsume, 1, 999);
                    break;
                case "2":
                    wrapper.between(Note::getAvgConsume, 1000, 6000);
                    break;
                case "3":
                    wrapper.between(Note::getAvgConsume, 6000, 20000);
                    break;
                case "4":
                    wrapper.ge(Note::getAvgConsume, 20000);
                    break;
            }
        }
        // 出行天数筛选：dayType 1-4 对应 3天以下、4-7天、8-14天、15天以上
        if (qo.getDayType() != null && !"-1".equals(qo.getDayType())) {
            switch (qo.getDayType()) {
                case "1":
                    wrapper.le(Note::getDays, 3);
                    break;
                case "2":
                    wrapper.between(Note::getDays, 4, 7);
                    break;
                case "3":
                    wrapper.between(Note::getDays, 8, 14);
                    break;
                case "4":
                    wrapper.ge(Note::getDays, 15);
                    break;
            }
        }
        // 排序：viewnum 最热（浏览数倒序）/ create_time 最新（创建时间倒序）
        if ("viewnum".equals(qo.getOrderBy())) {
            wrapper.orderByDesc(Note::getViewnum);
        } else {
            // 默认按创建时间倒序（历史数据 release_time 全为 NULL，故改用 create_time）
            wrapper.orderByDesc(Note::getCreateTime);
        }
        // 使用 baseMapper.selectPage 确保分页插件正确统计 total
        baseMapper.selectPage(page, wrapper);
        // 填充作者信息（列表页展示头像与昵称）
        fillAuthorInfo(page.getRecords());
        return page;
    }

    /**
     * 批量填充游记列表的作者信息
     * 优先通过 Feign 调用 userInfo 服务查询，失败则用 Note 冗余字段构造 fallback UserInfo
     */
    private void fillAuthorInfo(List<Note> noteList) {
        if (noteList == null || noteList.isEmpty()) {
            return;
        }
        for (Note note : noteList) {
            if (note.getAuthorId() == null) {
                continue;
            }
            // 优先远程查询
            UserInfo author = null;
            try {
                R<UserInfo> r = remoteUserInfoService.getOne(note.getAuthorId(), "inner");
                if (r != null && r.getCode() == R.SUCCESS && r.getData() != null) {
                    author = r.getData();
                }
            } catch (Exception ignored) {
            }
            // fallback：用冗余字段构造
            if (author == null) {
                author = new UserInfo();
                author.setId(note.getAuthorId());
                author.setNickname(note.getAuthorNickname() != null ? note.getAuthorNickname() : "匿名用户");
                author.setHeadImgUrl(note.getAuthorHeadImgUrl());
                author.setLevel(1);
                author.setState(0);
            }
            note.setAuthor(author);
            // 同步更新冗余字段（便于后续不需要 author 对象时直接使用）
            if (note.getAuthorNickname() == null) {
                note.setAuthorNickname(author.getNickname());
            }
            if (note.getAuthorHeadImgUrl() == null) {
                note.setAuthorHeadImgUrl(author.getHeadImgUrl());
            }
        }
    }

    @Override
    public NoteDetailVO detail(Long id) {
        Note note = getById(id);
        NoteDetailVO vo = new NoteDetailVO();
        if (note == null || !isPublic(note.getIsPublic())) {
            return vo;
        }

        // 1. 用 BeanUtils 拷贝 Note 所有基础字段到 VO（VO extends Note，字段名相同就会自动复制）
        //    这就是"不扁平展开"的核心 —— 不用手动写 20 多个 setXxx()
        BeanUtils.copyProperties(note, vo);

        // 2. 补 VO 特有的额外字段
        vo.setDay(note.getDays());                              // 前端模板用 detailData.day，提供别名
        vo.setPersonDisplay(mapPersonDisplay(note.getPerson())); // person 中文映射

        // 3. 富文本内容
        NoteContent nc = noteContentMapper.selectById(id);
        if (nc == null) {
            nc = new NoteContent();
            nc.setId(id);
            nc.setContent("");
        }
        vo.setContent(nc);

        // 4. 作者信息（优先从 userInfo 服务查，失败则用 Note 冗余字段兜底）
        if (note.getAuthorId() != null) {
            try {
                R<cn.wolfcode.wolf2w.member.api.domain.UserInfo> r
                        = remoteUserInfoService.getOne(note.getAuthorId(), "inner");
                if (r != null && r.getCode() == R.SUCCESS) {
                    vo.setAuthor(r.getData());
                }
            } catch (Exception ignored) {
            }
        }
        if (vo.getAuthor() == null) {
            cn.wolfcode.wolf2w.member.api.domain.UserInfo fallback = new cn.wolfcode.wolf2w.member.api.domain.UserInfo();
            fallback.setId(note.getAuthorId());
            fallback.setNickname(note.getAuthorNickname() != null ? note.getAuthorNickname() : "匿名用户");
            fallback.setHeadImgUrl(note.getAuthorHeadImgUrl());
            fallback.setLevel(1);
            fallback.setState(0);
            vo.setAuthor(fallback);
        }

        // 5. 收藏状态（从 Redis Set 中查询用户是否已收藏）
        try {
            Long userId = SecurityUtils.getUserId();
            vo.setFavorFlag(this.isUserFavor(id, userId));
        } catch (Exception e) {
            // 未登录时默认 false
            vo.setFavorFlag(false);
        }
        vo.setIsFollow(false);

        // 6. 浏览量 +1（写入 Redis，异步刷库）
        this.viewnumIncr(id);

        // 7. 通过 Feign 调用 destination 服务查关联目的地
        if (note.getDestId() != null) {
            try {
                R<Destination> destR = remoteDestinationService.getOne(note.getDestId(), "inner");
                if (destR != null && destR.getCode() == R.SUCCESS && destR.getData() != null) {
                    vo.setDest(destR.getData());
                }
            } catch (Exception ignored) {
                // Feign 调用失败时静默降级
            }
        }

        // 8. 通过 Feign 调用 strategy 服务查同目的地阅读量 top 攻略（取前 3 条）
        if (note.getDestId() != null) {
            try {
                // 查全量攻略，在内存中按 destId 过滤，按 viewnum 倒序取前 3
                R<List<Strategy>> strategyR = remoteStrategyService.list("inner");
                if (strategyR != null && strategyR.getCode() == R.SUCCESS && strategyR.getData() != null) {
                    List<Strategy> topStrategies = strategyR.getData().stream()
                            .filter(s -> note.getDestId().equals(s.getDestId()))
                            .sorted((a, b) -> {
                                Long va = a.getViewnum() != null ? a.getViewnum() : 0L;
                                Long vb = b.getViewnum() != null ? b.getViewnum() : 0L;
                                return Long.compare(vb, va);
                            })
                            .limit(3)
                            .collect(java.util.stream.Collectors.toList());
                    vo.setStrategies(topStrategies);
                }
            } catch (Exception ignored) {
                // Feign 调用失败时静默降级，使用默认空集合
            }
        }

        // 9. 查询同目的地阅读量 top 游记（取前 3 条，排除当前游记）
        if (note.getDestId() != null) {
            try {
                List<Note> topNotes = lambdaQuery()
                        .eq(Note::getDestId, note.getDestId())
                        .ne(Note::getId, id)
                        .in(Note::getIsPublic, "1", "true")
                        .orderByDesc(Note::getViewnum)
                        .last("limit 3")
                        .list();
                fillAuthorInfo(topNotes);
                vo.setTravels(topNotes);
            } catch (Exception ignored) {
                // 查库失败时静默降级
            }
        }

        // 10. 查询游记评论列表（第一页 10 条，按创建时间倒序）
        try {
            NoteCommentQuery commentQo = new NoteCommentQuery();
            commentQo.setNoteId(id);
            commentQo.setCurrentPage(1);
            commentQo.setPageSize(10);
            commentQo.setStatus("0");  // 只查正常状态的评论
            IPage<NoteComment> commentPage = noteCommentService.queryPage(commentQo);
            if (commentPage != null && commentPage.getRecords() != null) {
                vo.setComments(commentPage.getRecords());
            }
        } catch (Exception ignored) {
            // 查评论失败时静默降级，使用默认空集合
        }

        return vo;
    }

    /**
     * person 数字映射为前端展示文本
     * 1 亲子 / 2 情侣 / 3 独自一人 / 4 家庭 / 5 朋友
     */
    private String mapPersonDisplay(String person) {
        if (person == null) {
            return "";
        }
        switch (person) {
            case "1":
                return "亲子出游";
            case "2":
                return "情侣出行";
            case "3":
                return "独自一人";
            case "4":
                return "家庭出行";
            case "5":
                return "和朋友";
            default:
                return person;
        }
    }

    private boolean isPublic(String isPublic) {
        return "1".equals(isPublic) || "true".equalsIgnoreCase(isPublic);
    }

    @Override
    @Transactional
    public boolean saveNote(Note note, String content) {
        note.setCreateTime(new Date());
        note.setUpdateTime(new Date());
        if ("0".equals(note.getStatus())) {
            // 草稿
        } else if ("1".equals(note.getStatus())) {
            // 待发布
            note.setReleaseTime(new Date());
        }
        if (!save(note)) {
            return false;
        }

        // 保存内容（id 与游记 id 一致）
        NoteContent noteContent = new NoteContent();
        noteContent.setId(note.getId());
        noteContent.setContent(content);
        noteContentMapper.insert(noteContent);

        return true;
    }

    @Override
    @Transactional
    public boolean updateNote(Note note, String content) {
        if (note.getId() == null) {
            return false;
        }
        note.setUpdateTime(new Date());
        if ("1".equals(note.getStatus()) && note.getReleaseTime() == null) {
            note.setReleaseTime(new Date());
        }
        if (!updateById(note)) {
            return false;
        }

        // content 为 null 表示本次只修改基础信息，不能覆盖已有富文本。
        if (content != null) {
            NoteContent noteContent = new NoteContent();
            noteContent.setId(note.getId());
            noteContent.setContent(content);
            if (noteContentMapper.updateById(noteContent) == 0) {
                noteContentMapper.insert(noteContent);
            }
        }

        return true;
    }

    @Override
    public boolean audit(Long id, String status) {
        Note note = new Note();
        note.setId(id);
        note.setStatus(status);
        note.setUpdateTime(new Date());
        return updateById(note);
    }

    @Override
    public List<Note> queryByDestId(Long destId, int limit) {
        return lambdaQuery()
                .eq(Note::getDestId, destId)
                .in(Note::getIsPublic, "1", "true")
                .orderByDesc(Note::getThumbsupnum)
                .last("limit " + limit)
                .list();
    }

    @Override
    public List<Note> queryBannerList(int limit) {
        // 近2个月内创建、公开的游记，按收藏数倒序取前 N
        // （历史数据 release_time 全为 NULL，故改用 create_time）
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -2);
        Date twoMonthsAgo = calendar.getTime();
        return lambdaQuery()
                .in(Note::getIsPublic, "1", "true")
                .ge(Note::getCreateTime, twoMonthsAgo)
                .orderByDesc(Note::getFavornum)
                .last("limit " + limit)
                .list();
    }

    @Override
    public List<Note> search(String keyword, Integer limit) {
        // 关键字匹配标题或概要，只搜公开的游记
        return lambdaQuery()
                .in(Note::getIsPublic, "1", "true")
                .and(w -> w.like(Note::getTitle, keyword)
                        .or()
                        .like(Note::getSummary, keyword))
                .orderByDesc(Note::getCreateTime)
                .last(limit != null ? "limit " + limit : "")
                .list();
    }

    // ==================== Redis 计数相关 ====================

    /**
     * 浏览量 + 1
     */
    @Override
    public Map<String, Object> viewnumIncr(Long nid) {
        return incrementCount(nid, "viewnum", 1);
    }

    /**
     * 评论数 + 1
     */
    @Override
    public Map<String, Object> replynumIncr(Long nid) {
        return incrementCount(nid, "replynum", 1);
    }

    /**
     * 收藏
     */
    @Override
    public void favor(Long nid) {
        Long userId = SecurityUtils.getUserId();
        // 用户收藏集合 key
        String favorSetKey = RedisKeys.NOTE_FAVOR_SET.join(userId.toString());
        // 游记统计 Hash key
        String statisHashKey = RedisKeys.NOTE_STATIS_HASH.join(nid.toString());
        // 检查缓存是否完整，不完整则从 DB 加载（防止残缺 Hash 导致持久化时 id 为 null）
        ensureCacheComplete(nid, statisHashKey);

        // 关键：先判断是否已收藏，避免重复 +1
        Boolean favorited = redisService.isCacheSetContains(favorSetKey, nid);
        if (Boolean.FALSE.equals(favorited)) {
            redisService.addCacheSetValue(favorSetKey, nid);
            redisService.incrementCacheMapValue(statisHashKey, "favornum", 1);
        }
        // 如果已经收藏，就什么都不做（静默幂等），不要抛异常 — 前端只判断 200
        this.statisHashMapPersist();
    }
    // 取消收藏
    @Override
    public void unFavor(Long nid) {

        Long userId = SecurityUtils.getUserId();
        String favorSetKey = RedisKeys.NOTE_FAVOR_SET.join(userId.toString());
        String statisHashKey = RedisKeys.NOTE_STATIS_HASH.join(nid.toString());
        ensureCacheComplete(nid, statisHashKey);

        Boolean favorited = redisService.isCacheSetContains(favorSetKey, nid);
        if (Boolean.TRUE.equals(favorited)) {
            redisService.incrementCacheMapValue(statisHashKey, "favornum", -1);
            redisService.deleteCacheSetValue(favorSetKey, nid);
        }
        // 持久化收藏结果
        this.statisHashMapPersist();
    }


    /**
     * 点赞（每天最多 5 次）
     */
    @Override
    public Map<String, Object> thumbsup(Long nid) {
        Long userId = SecurityUtils.getUserId();
        // 点赞限流 key：user_note_thumbsup:{nid}:{uid}，24小时过期
        String key = RedisKeys.USER_NOTE_THUMBSUP.join(nid.toString(), userId.toString());

        if (!redisService.hasKey(key)) {
            // 首次点赞，设置 key，过期时间为今天剩余时间（到 23:59:59）
            Date now = new Date();
            Date endTime = DateUtil.getEndDate(now);
            long expireTime = DateUtil.getBetweenDate(now, endTime);
            expireTime = expireTime == 0 ? 1 : expireTime;
            redisService.setCacheObject(key, 0, expireTime, TimeUnit.SECONDS);
        }

        Long ret = redisService.incrementCacheObjectValue(key, 1);
        Boolean result;
        String statisKey = RedisKeys.NOTE_STATIS_HASH.join(nid.toString());
        if (ret > 5) {
            // 今天点赞已满 5 次
            result = false;
        } else {
            result = true;
            // 检查缓存是否完整，不完整则从 DB 加载（防止残缺 Hash）
            ensureCacheComplete(nid, statisKey);
            redisService.incrementCacheMapValue(statisKey, "thumbsupnum", 1);
        }
        Map<String, Object> cacheMap = redisService.getCacheMap(statisKey);
        cacheMap.put("result", result );
        // 持久化点赞结果,小项目直接用更直观的方法，不建议在生产环境使用，因为会阻塞主线程
        // this.statisHashMapPersist();
        return cacheMap;
    }

    /**
     * Redis 统计数据持久化到数据库
     */
    @Override
    public void statisHashMapPersist() {
        System.out.println("游记统计数据持久化到数据库");

        // 1. 拼接 Redis 键 --> note_statis_hash:* （匹配所有游记）
        String key = RedisKeys.NOTE_STATIS_HASH.join("*");
        Collection<String> keys = redisService.keys(key);
        if (keys == null || keys.isEmpty()) {
            return;
        }

        // 2. 遍历所有游记的 key，将 Redis 中的累计值刷回 DB
        for (String k : keys) {
            Map<String, Object> cacheMap = redisService.getCacheMap(k);
            Long id = toLong(cacheMap.get("id"));
            // id 为 null 说明是残缺 Hash（可能由 Redis 重启等异常导致），跳过避免破坏 DB 数据
            if (id == null) {
                continue;
            }
            Integer viewnum = toInteger(cacheMap.get("viewnum"));
            Integer thumbsupnum = toInteger(cacheMap.get("thumbsupnum"));
            Integer replynum = toInteger(cacheMap.get("replynum"));
            Integer sharenum = toInteger(cacheMap.get("sharenum"));
            Integer favornum = toInteger(cacheMap.get("favornum"));

            lambdaUpdate()
                    .eq(Note::getId, id)
                    .set(Note::getViewnum, viewnum != null ? viewnum : 0)
                    .set(Note::getThumbsupnum, thumbsupnum != null ? thumbsupnum : 0)
                    .set(Note::getReplynum, replynum != null ? replynum : 0)
                    .set(Note::getSharenum, sharenum != null ? sharenum : 0)
                    .set(Note::getFavornum, favornum != null ? favornum : 0)
                    .update();
        }
    }


    // ==================== 提取的公共方法 ====================

    /**
     * 查询用户是否已收藏
     */
    private Boolean isUserFavor(Long nid, Long uid) {
        String key = RedisKeys.NOTE_FAVOR_SET.join(uid.toString());
        return redisService.isCacheSetContains(key, nid);
    }
    /**
     * 确保缓存完整，不完整则从 DB 加载
     * 供 favor/thumbsup 等方法在 HINCRBY 之前调用，防止残缺 Hash
     */
    private void ensureCacheComplete(Long nid, String key) {
        Map<String, Object> map = redisService.getCacheMap(key);
        if (!isMapComplete(map)) {
            loadAndCacheFromDB(nid, key);
        }
    }

    /**
     * 通用计数增量（支持 +/- 任意值）
     *
     * @param nid   游记ID
     * @param field 统计字段名，如 "viewnum"、"thumbsupnum"、"replynum"、"sharenum"、"favornum"
     * @param delta 增量（正数加，负数减）
     * @return 更新后的完整统计 Map（包含 id 及所有计数字段）
     */
    private Map<String, Object> incrementCount(Long nid, String field, int delta) {
        String key = RedisKeys.NOTE_STATIS_HASH.join(nid.toString());

        // 1. 获取缓存中的数据，并检查是否完整
        Map<String, Object> map = redisService.getCacheMap(key);
        if (!isMapComplete(map)) {
            // 数据不完整（或不存在），从数据库加载完整数据并写入缓存
            map = loadAndCacheFromDB(nid, key);
        }

        // 2. 原子增加指定字段（底层使用 HINCRBY，保证线程安全）
        redisService.incrementCacheMapValue(key, field, delta);

        // 3. 重新获取最新完整数据返回
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
     * 从数据库加载游记完整统计信息，并写入 Redis 缓存
     */
    private Map<String, Object> loadAndCacheFromDB(Long nid, String key) {
        Note note = baseMapper.selectById(nid);
        if (note == null) {
            // 游记不存在（可能已被删除），返回空 Map 避免后续 NPE
            return new HashMap<>();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("id", note.getId());
        // Note 实体字段为 Integer，直接放入即可
        map.put("viewnum", note.getViewnum() != null ? note.getViewnum() : 0);
        map.put("thumbsupnum", note.getThumbsupnum() != null ? note.getThumbsupnum() : 0);
        map.put("replynum", note.getReplynum() != null ? note.getReplynum() : 0);
        map.put("sharenum", note.getSharenum() != null ? note.getSharenum() : 0);
        map.put("favornum", note.getFavornum() != null ? note.getFavornum() : 0);
        redisService.setCacheMap(key, map);
        return map;
    }

    private Long toLong(Object value) {
        return value instanceof Number ? ((Number) value).longValue() : null;
    }

    private Integer toInteger(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : null;
    }
}
