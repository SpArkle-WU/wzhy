package cn.wolfcode.wolf2w.business.service.impl;

import cn.wolfcode.wolf2w.business.api.domain.NoteContent;
import cn.wolfcode.wolf2w.business.mapper.NoteContentMapper;
import cn.wolfcode.wolf2w.business.service.INoteContentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 游记内容 Service 业务层处理
 *
 * @author wzh
 * @date 2026-08-10
 */
@Service
@Transactional
public class NoteContentServiceImpl extends ServiceImpl<NoteContentMapper, NoteContent> implements INoteContentService {
}
