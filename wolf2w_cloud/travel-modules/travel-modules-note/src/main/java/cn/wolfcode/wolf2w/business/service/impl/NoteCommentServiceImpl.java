package cn.wolfcode.wolf2w.business.service.impl;

import cn.wolfcode.wolf2w.business.api.domain.Note;
import cn.wolfcode.wolf2w.business.api.domain.NoteComment;
import cn.wolfcode.wolf2w.business.mapper.NoteCommentMapper;
import cn.wolfcode.wolf2w.business.query.NoteCommentQuery;
import cn.wolfcode.wolf2w.business.service.INoteCommentService;
import cn.wolfcode.wolf2w.business.service.INoteService;
import cn.wolfcode.wolf2w.common.core.domain.R;
import cn.wolfcode.wolf2w.common.security.utils.SecurityUtils;
import cn.wolfcode.wolf2w.member.api.RemoteUserInfoService;
import cn.wolfcode.wolf2w.member.api.domain.UserInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 游记评论Service业务层处理
 *
 * @author wzh
 * @date 2026-08-10
 */
@Service
@Transactional
public class NoteCommentServiceImpl extends ServiceImpl<NoteCommentMapper, NoteComment> implements INoteCommentService {

    @Autowired
    private RemoteUserInfoService remoteUserInfoService;
    @Autowired
    @Lazy  // 打破 NoteService ↔ NoteCommentService 双向循环依赖
    private INoteService noteService;

    @Override
    public IPage<NoteComment> queryPage(NoteCommentQuery qo) {
        // 当指定了游记 id（前台详情页评论分页）时按顶级评论分页 + 子回复组装
        if (qo.getNoteId() != null && qo.getUserId() == null) {
            return queryCommentsWithTree(qo);
        }
        // 后台管理或我的评论：简单分页，不做树结构，只填充用户信息
        return queryPageSimple(qo);
    }

    /**
     * 前台游记详情页：按顶级评论分页 + 每条顶级评论下挂载其回复（二层树形）
     * 同时填充回复的 refUser（被回复用户信息），用于"回复@xxx"展示
     */
    private IPage<NoteComment> queryCommentsWithTree(NoteCommentQuery qo) {
        // 第一步：构造条件，只针对 noteId 匹配
        LambdaQueryWrapper<NoteComment> commonWrapper = new LambdaQueryWrapper<>();
        commonWrapper.eq(NoteComment::getNoteId, qo.getNoteId());
        if (qo.getStatus() != null) {
            commonWrapper.eq(NoteComment::getStatus, qo.getStatus());
        }
        // 按创建时间倒序
        commonWrapper.orderByDesc(NoteComment::getCreateTime);

        // 第二步：一次把该游记所有（含状态过滤后的）评论拉出来，用于构建顶级分页 + 子回复
        // （小项目直接全量拉，评论数一般不会太大；若量大可改为两步 SQL：先顶级分页，再按顶级 id 集查回复）
        List<NoteComment> all = baseMapper.selectList(commonWrapper);
        if (all == null || all.isEmpty()) {
            return new Page<>(qo.getCurrentPage(), qo.getPageSize(), 0);
        }

        // 构建 userId -> UserInfo 缓存（批量查用户，减少 N+1 Feign 调用）
        Map<Long, UserInfo> userCache = buildUserCache(all);

        // 第三步：建立评论索引，构建顶级评论和其下全部层级的回复。
        // 数据库的 refId 指向被回复的那条评论，因此回复回复时不能只按顶级评论的
        // id 分组，否则第二层及更深层的回复会从结果中丢失。
        Map<Long, NoteComment> commentById = new HashMap<>();
        for (NoteComment comment : all) {
            if (comment.getId() != null) {
                commentById.put(comment.getId(), comment);
            }
        }
        // 先填充全部评论的作者，避免按时间倒序遍历时回复先于被回复评论处理。
        for (NoteComment comment : all) {
            if (comment.getUserId() == null) {
                continue;
            }
            UserInfo user = userCache.get(comment.getUserId());
            if (user == null) {
                user = new UserInfo();
                user.setId(comment.getUserId());
                user.setNickname("匿名用户");
                user.setLevel(1);
                user.setState(0);
                userCache.put(comment.getUserId(), user);
            }
            comment.setUser(user);
        }
        List<NoteComment> topList = new ArrayList<>();
        Map<Long, List<NoteComment>> repliesByTopId = new HashMap<>();
        for (NoteComment c : all) {
            boolean isTop = (c.getRefId() == null || c.getRefId() == 0L);
            if (isTop) {
                topList.add(c);
            } else {
                NoteComment referencedComment = commentById.get(c.getRefId());
                if (referencedComment != null) {
                    c.setRefUser(referencedComment.getUser());
                }
                Long topId = findTopCommentId(c, commentById);
                if (topId == null) {
                    // 被引用的评论已被删除或形成异常环路时，仍展示该评论，避免静默丢数据。
                    topList.add(c);
                } else {
                    repliesByTopId.computeIfAbsent(topId, k -> new ArrayList<>()).add(c);
                }
            }
        }

        // 第四步：将回复挂到对应的顶级评论 comments 集合中。回复时间正序，
        // refUser 已按其实际被回复的评论填充，而不是一律使用顶级评论作者。
        for (NoteComment top : topList) {
            List<NoteComment> replies = repliesByTopId.get(top.getId());
            if (replies != null && !replies.isEmpty()) {
                replies.sort(Comparator.comparing(NoteComment::getCreateTime,
                        Comparator.nullsLast(Date::compareTo)));
                top.setComments(replies);
            }
        }

        // 第五步：对顶级评论做"手动分页"——取 currentPage 对应子集（注意 topList 是按创建时间倒序的）
        long total = topList.size();
        long current = qo.getCurrentPage() < 1 ? 1 : qo.getCurrentPage();
        long size = qo.getPageSize() < 1 ? 10 : qo.getPageSize();
        long startIdx = (current - 1) * size;
        long endIdx = Math.min(startIdx + size, total);
        List<NoteComment> pageRecords;
        if (startIdx >= total) {
            pageRecords = new ArrayList<>();
        } else {
            pageRecords = topList.subList((int) startIdx, (int) endIdx);
        }
        Page<NoteComment> page = new Page<>(current, size, total);
        page.setRecords(pageRecords);
        return page;
    }

    private Long findTopCommentId(NoteComment comment, Map<Long, NoteComment> commentById) {
        Set<Long> visited = new HashSet<>();
        NoteComment current = comment;
        while (current.getRefId() != null && current.getRefId() != 0L) {
            if (current.getId() == null || !visited.add(current.getId())) {
                return null;
            }
            current = commentById.get(current.getRefId());
            if (current == null) {
                return null;
            }
        }
        return current.getId();
    }

    /**
     * 后台/我的评论：简单分页，只填充每条评论的发布者用户信息（降级兜底）
     */
    private IPage<NoteComment> queryPageSimple(NoteCommentQuery qo) {
        IPage<NoteComment> page = new Page<>(qo.getCurrentPage(), qo.getPageSize());
        LambdaQueryWrapper<NoteComment> wrapper = new LambdaQueryWrapper<>();
        if (qo.getNoteId() != null) {
            wrapper.eq(NoteComment::getNoteId, qo.getNoteId());
        }
        if (qo.getUserId() != null) {
            wrapper.eq(NoteComment::getUserId, qo.getUserId());
        }
        if (qo.getType() != null) {
            wrapper.eq(NoteComment::getType, qo.getType());
        }
        if (qo.getStatus() != null) {
            wrapper.eq(NoteComment::getStatus, qo.getStatus());
        }
        wrapper.orderByDesc(NoteComment::getCreateTime);
        baseMapper.selectPage(page, wrapper);

        // 构建用户缓存一次性填充 user 信息
        Map<Long, UserInfo> userCache = buildUserCache(page.getRecords());
        for (NoteComment c : page.getRecords()) {
            if (c.getUserId() != null && userCache.containsKey(c.getUserId())) {
                c.setUser(userCache.get(c.getUserId()));
            } else {
                // fallback：构造匿名用户，防止模板 NPE
                UserInfo fallback = new UserInfo();
                fallback.setId(c.getUserId());
                fallback.setNickname("匿名用户");
                fallback.setLevel(1);
                fallback.setState(0);
                c.setUser(fallback);
            }
        }
        return page;
    }

    /**
     * 根据评论列表中的 userId / refId 指向的 userId 集合，批量查用户并缓存
     * 用 Feign 远程服务查，失败则返回空 Map（上层继续走 fallback 逻辑）
     */
    private Map<Long, UserInfo> buildUserCache(Collection<NoteComment> comments) {
        Map<Long, UserInfo> cache = new HashMap<>();
        if (comments == null || comments.isEmpty()) {
            return cache;
        }
        // 收集所有 user id：评论者 + 回复 refId 指向的评论作者也需要（但这里我们只能先拿到评论者的 id，
        // refUser 将在父循环中从已缓存的父评论作者中取）
        Set<Long> uids = comments.stream()
                .map(NoteComment::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (uids.isEmpty()) {
            return cache;
        }
        // 因为 RemoteUserInfoService 没有批量接口，使用 getOne 循环调用 + try/catch 降级
        for (Long uid : uids) {
            if (cache.containsKey(uid)) {
                continue;
            }
            try {
                R<UserInfo> r = remoteUserInfoService.getOne(uid, "inner");
                if (r != null && r.getCode() == R.SUCCESS && r.getData() != null) {
                    cache.put(uid, r.getData());
                }
            } catch (Exception ignored) {
                // Feign 失败就跳过，后续用 fallback
            }
        }
        return cache;
    }

    @Override
    public void addContent(NoteComment comment) {
        // 设置当前登录用户id
        Long userId = SecurityUtils.getUserId();
        comment.setUserId(userId);
        comment.setCreateTime(new Date());
        // 评论状态：0 正常
        if (comment.getStatus() == null || comment.getStatus().trim().isEmpty()) {
            comment.setStatus("0");
        }
        // 冗余存储游记标题
        if (comment.getNoteId() != null) {
            Note note = noteService.getById(comment.getNoteId());
            if (note != null) {
                comment.setNoteTitle(note.getTitle());
            }
        }
        baseMapper.insert(comment);
        // 更新游记回复数 replynum + 1（走 Redis，异步刷库）
        if (comment.getNoteId() != null) {
            noteService.replynumIncr(comment.getNoteId());
        }
    }
}
