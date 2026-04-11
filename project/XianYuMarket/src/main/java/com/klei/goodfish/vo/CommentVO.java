package com.klei.goodfish.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentVO {
    private Integer commentId;
    private String content;
    private LocalDateTime createTime;

    // 用户相关信息
    private Integer userId;
    private String userName;
    private String avatar;

    // 点赞数 - 从数据库查询
    private Integer likeCount;

    // 关键：当前用户是否已点赞
    private boolean hasLiked;
}