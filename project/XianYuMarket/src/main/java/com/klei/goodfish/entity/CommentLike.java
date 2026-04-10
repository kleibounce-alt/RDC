package com.klei.goodfish.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author klei
 */
@Data
public class CommentLike {
    private Integer id;
    private Integer commentId;
    private Integer userId;
    private LocalDateTime createTime;
}
