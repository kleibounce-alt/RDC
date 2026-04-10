package com.klei.goodfish.service.impl;

import com.klei.goodfish.dto.CommentLikeDTO;
import com.klei.goodfish.entity.Comment;
import com.klei.goodfish.entity.CommentLike;
import com.klei.goodfish.entity.User;
import com.klei.goodfish.mapper.CommentLikeMapper;
import com.klei.goodfish.mapper.CommentMapper;
import com.klei.goodfish.mapper.UserMapper;
import com.klei.goodfish.mappercore.proxy.MapperProxy;
import com.klei.goodfish.service.CommentLikeService;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.vo.LikeStatusVO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author klei
 */
public class CommentLikeServiceImpl implements CommentLikeService {

    private CommentLikeMapper likeMapper = MapperProxy.getMapper(CommentLikeMapper.class);
    private CommentMapper commentMapper = MapperProxy.getMapper(CommentMapper.class);
    private UserMapper userMapper = MapperProxy.getMapper(UserMapper.class);

    @Override
    public boolean like(CommentLikeDTO dto) {
        if (dto.getCommentId() == null || dto.getUserId() == null) {
            throw new BusinessException(400, "参数不能为空");
        }

        // 校验评论是否存在且未删除
        Comment comment = commentMapper.findById(dto.getCommentId());
        if (comment == null || comment.getStatus() == 0) {
            throw new BusinessException(404, "评论不存在或已删除");
        }

        // 校验用户是否存在
        User user = userMapper.findById(dto.getUserId());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 查重：是否已点赞
        CommentLike exist = likeMapper.findByBothId(dto.getCommentId(), dto.getUserId());
        if (exist != null) {
            throw new BusinessException(400, "已点赞，请勿重复点赞");
        }

        // 插入点赞记录
        likeMapper.insert(dto.getCommentId(), dto.getUserId());
        return true;
    }

    @Override
    public boolean unlike(CommentLikeDTO dto) {
        if (dto.getCommentId() == null || dto.getUserId() == null) {
            throw new BusinessException(400, "参数不能为空");
        }

        // 查重：是否已点赞（没点赞过不能取消）
        CommentLike exist = likeMapper.findByBothId(dto.getCommentId(), dto.getUserId());
        if (exist == null) {
            throw new BusinessException(400, "未点赞，无法取消");
        }

        // 删除记录
        likeMapper.delete(dto.getCommentId(), dto.getUserId());
        return true;
    }

    @Override
    public LikeStatusVO getLikeStatus(Integer commentId, Integer userId) {
        if (commentId == null) {
            throw new BusinessException(400, "评论ID不能为空");
        }

        LikeStatusVO vo = new LikeStatusVO();

        // 查询总点赞数
        int count = likeMapper.countByCommentId(commentId);
        vo.setLikeCount(count);

        // 查询当前用户是否已点赞（userId为null时默认未点赞）
        if (userId != null) {
            CommentLike like = likeMapper.findByBothId(commentId, userId);
            vo.setHasLiked(like != null);
        } else {
            vo.setHasLiked(false);
        }

        return vo;
    }

    @Override
    public int getLikeCount(Integer commentId) {
        if (commentId == null) {
            return 0;
        }
        return likeMapper.countByCommentId(commentId);
    }

    @Override
    public List<Integer> getLikedCommentIds(Integer userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        List<CommentLike> likes = likeMapper.findByUserId(userId);
        List<Integer> commentIds = new ArrayList<>();
        for (CommentLike like : likes) {
            commentIds.add(like.getCommentId());
        }
        return commentIds;
    }
}