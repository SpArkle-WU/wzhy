package cn.wolfcode.wolf2w.business.query;

import cn.wolfcode.wolf2w.common.core.query.QueryObject;
import cn.wolfcode.wolf2w.member.api.domain.UserInfo;
import lombok.Data;

@Data
public class SearchQuery extends QueryObject {

    // 搜索类型(-1:全部,0:目的地,1:攻略 2:游记 3:找人)
    private int  type = -1;
}
