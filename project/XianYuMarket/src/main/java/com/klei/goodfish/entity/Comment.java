package com.klei.goodfish.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author klei
 */
@Data
public class Comment {
    private Integer id;
    private Integer goodId;
    private Integer userId;
    private String content;
    private Integer status;
    private LocalDateTime createTime;
}
