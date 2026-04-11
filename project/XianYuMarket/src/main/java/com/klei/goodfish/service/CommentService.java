package com.klei.goodfish.service;

import com.klei.goodfish.dto.CommentPublishDTO;
import com.klei.goodfish.vo.CommentVO;
import java.util.List;

public interface CommentService {
    // 发表评论
    CommentVO publishComment(CommentPublishDTO dto);

    // 删除评论
    boolean deleteComment(Integer commentId, Integer operatorId);

    // 查询商品的所有评论 - 修复：添加当前用户ID参数
    List<CommentVO> getCommentsByGoodId(Integer goodId, Integer currentUserId);

    // 获取商品评论总数
    int getCommentCount(Integer goodId);

    boolean adminDeleteComment(Integer commentId);
}