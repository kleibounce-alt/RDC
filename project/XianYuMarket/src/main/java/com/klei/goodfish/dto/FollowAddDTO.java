package com.klei.goodfish.dto;

import lombok.Data;

/**
 * @author klei
 */
@Data
public class FollowAddDTO {
    //当前用户id
    private Integer followerId;
    private Integer followingId;
}