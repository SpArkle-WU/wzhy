package cn.wolfcode.wolf2w.business.vo;

import cn.wolfcode.wolf2w.business.api.domain.Destination;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// 封装ThemeVO 主题VO 用于查询攻略主题列表 返回给前端

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThemeVO {

    // 主题名称
    private String themeName;

    // 目的地列表
    private List<Destination> dests = new ArrayList<>();

}
