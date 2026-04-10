package com.klei.goodfish.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author klei
 */
@Data
public class FollowVO {
    private Integer followId;
    //被关注者
    private Integer followingId;
    private String followingName;
    private String followingAvatar;
    private LocalDateTime followTime;
}