package cn.wolfcode.wolf2w.business.mapper;

import cn.wolfcode.wolf2w.business.api.domain.Note;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {
}