package cn.wolfcode.wolf2w.business.service.impl;

import cn.wolfcode.wolf2w.business.api.domain.StrategyComment;
import cn.wolfcode.wolf2w.business.mapper.StrategyCommentMapper;
import cn.wolfcode.wolf2w.business.query.StrategyCommentQuery;
import cn.wolfcode.wolf2w.business.service.IStrategyCommentService;
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
 * 攻略评论Service业务层处理
 * 
 * @author wzh
 * @date 2026-08-06
 */
@Service
@Transactional
public class StrategyCommentServiceImpl extends ServiceImpl<StrategyCommentMapper,StrategyComment> implements IStrategyCommentService {

    @Autowired
    private RemoteUserInfoService remoteUserInfoService;

    @Override
    public IPage<StrategyComment> queryPage(StrategyCommentQuery qo) {
        IPage<StrategyComment> page = new Page<>(qo.getCurrentPage(), qo.getPageSize());
        // 分页查询评论列表
        LambdaQueryWrapper<StrategyComment> wrapper = new LambdaQueryWrapper<StrategyComment>();
        wrapper.eq(StrategyComment::getStrategyId, qo.getStrategyId());
        baseMapper.selectPage(page, wrapper);
        // 关联用户信息
        for (StrategyComment comment : page.getRecords()) {
            Long userId = comment.getUserId();
            // 查询用户信息
            UserInfo userInfo = remoteUserInfoService.getOne(userId,"inner").getData();
            // 设置用户信息到评论对象
            comment.setUser(userInfo);
        }
        return page;
    }

    @Override
    public void addContent(StrategyComment strategyComment) {
        Long userId = SecurityUtils.getUserId();
        strategyComment.setUserId(userId);
        strategyComment.setCreateTime(new Date());
        strategyComment.setContent(strategyComment.getContent());
        strategyComment.setState(1L);
        strategyComment.setThumbnum(0L);
        baseMapper.insert(strategyComment);
    }
}
