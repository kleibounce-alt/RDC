package com.klei.goodfish.service;

import com.klei.goodfish.dto.CommentLikeDTO;
import com.klei.goodfish.vo.LikeStatusVO;
import java.util.List;

/**
 * @author klei
 */
public interface CommentLikeService {

    // 点赞
    boolean like(CommentLikeDTO dto);

    // 取消点赞
    boolean unlike(CommentLikeDTO dto);

    // 查询点赞状态
    LikeStatusVO getLikeStatus(Integer commentId, Integer userId);

    // 获取评论的点赞数
    int getLikeCount(Integer commentId);

    // 获取用户点赞的所有评论ID
    List<Integer> getLikedCommentIds(Integer userId);
}