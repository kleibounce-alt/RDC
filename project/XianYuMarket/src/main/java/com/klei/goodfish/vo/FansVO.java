package com.klei.goodfish.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FansVO {
    private Integer followId;
    //粉丝ID
    private Integer followerId;
    private String followerName;
    private String followerAvatar;
    private LocalDateTime followTime;
}