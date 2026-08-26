package cn.wolfcode.wolf2w.business.api.domain;

import lombok.Data;

@Data
public class UserInfoES {

    private Long id;
    private String  nickname;
    private String  info;
    private String  city;
}
