package com.klei.goodfish.dto;

import lombok.Data;

/**
 * @author klei
 */
@Data
public class CommentPublishDTO {
    private Integer goodId;
    private String content;
    private Integer userId;
}