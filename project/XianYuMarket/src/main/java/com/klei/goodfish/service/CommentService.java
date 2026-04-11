package com.klei.goodfish.service;

import com.klei.goodfish.dto.CommentPublishDTO;
import com.klei.goodfish.vo.CommentVO;
import java.util.List;

/**
 * @author klei
 */
public interface CommentService {
    // 发表评论，返回带用户信息的VO
    CommentVO publishComment(CommentPublishDTO dto);

    // 删除评论，返回是否成功
    boolean deleteComment(Integer commentId, Integer operatorId);

    // 查商品的所有评论
    List<CommentVO> getCommentsByGoodId(Integer goodId);

    // 获取商品评论总数
    int getCommentCount(Integer goodId);

    boolean adminDeleteComment(Integer commentId);
}