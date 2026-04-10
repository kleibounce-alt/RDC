package com.klei.goodfish.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author klei
 */
@Data
public class CommentLikeVO {
    private Integer likeId;
    private Integer commentId;
    private Integer userId;
    private LocalDateTime likeTime;
}