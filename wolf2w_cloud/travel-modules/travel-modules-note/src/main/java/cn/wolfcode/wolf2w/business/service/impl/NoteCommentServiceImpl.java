package cn.wolfcode.wolf2w.business.service.impl;

import cn.wolfcode.wolf2w.business.api.domain.Note;
import cn.wolfcode.wolf2w.business.api.domain.NoteComment;
import cn.wolfcode.wolf2w.business.mapper.NoteCommentMapper;
import cn.wolfcode.wolf2w.business.query.NoteCommentQuery;
import cn.wolfcode.wolf2w.business.service.INoteCommentService;
import cn.wolfcode.wolf2w.business.service.INoteService;
import cn.wolfcode.wolf2w.common.security.utils.SecurityUtils;
import cn.wolfcode.wolf2w.member.api.RemoteUserInfoService;
import cn.wolfcode.wolf2w.member.api.domain.UserInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

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
    private INoteService noteService;

    @Override
    public IPage<NoteComment> queryPage(NoteCommentQuery qo) {
        IPage<NoteComment> page = new Page<>(qo.getCurrentPage(), qo.getPageSize());
        // 构建查询条件
        LambdaQueryWrapper<NoteComment> wrapper = new LambdaQueryWrapper<>();
        // 游记id
        if (qo.getNoteId() != null) {
            wrapper.eq(NoteComment::getNoteId, qo.getNoteId());
        }
        // 用户id
        if (qo.getUserId() != null) {
            wrapper.eq(NoteComment::getUserId, qo.getUserId());
        }
        // 评论类型
        if (qo.getType() != null) {
            wrapper.eq(NoteComment::getType, qo.getType());
        }
        // 评论状态
        if (qo.getStatus() != null) {
            wrapper.eq(NoteComment::getStatus, qo.getStatus());
        }
        // 按创建时间倒序
        wrapper.orderByDesc(NoteComment::getCreateTime);
        baseMapper.selectPage(page, wrapper);
        // 关联用户信息
        for (NoteComment comment : page.getRecords()) {
            Long userId = comment.getUserId();
            if (userId != null) {
                UserInfo userInfo = remoteUserInfoService.getOne(userId, "inner").getData();
                comment.setUser(userInfo);
            }
        }
        return page;
    }

    @Override
    public void addContent(NoteComment comment) {
        // 设置当前登录用户id
        Long userId = SecurityUtils.getUserId();
        comment.setUserId(userId);
        comment.setCreateTime(new Date());
        // 评论状态：0 正常
        comment.setStatus("0");
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

