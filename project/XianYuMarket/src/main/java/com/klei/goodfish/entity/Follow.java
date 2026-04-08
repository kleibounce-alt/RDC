package com.klei.goodfish.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author klei
 */
@Data
public class Follow {
    private Integer id;
    private Integer followerId;
    private Integer followingId;
    private LocalDateTime createTime;
}
