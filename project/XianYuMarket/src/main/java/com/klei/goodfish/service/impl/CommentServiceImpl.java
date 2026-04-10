package com.klei.goodfish.service.impl;

import com.klei.goodfish.dto.CommentPublishDTO;
import com.klei.goodfish.entity.Comment;
import com.klei.goodfish.entity.Good;
import com.klei.goodfish.entity.User;
import com.klei.goodfish.mapper.CommentMapper;
import com.klei.goodfish.mapper.GoodMapper;
import com.klei.goodfish.mapper.UserMapper;
import com.klei.goodfish.mappercore.proxy.MapperProxy;
import com.klei.goodfish.service.CommentService;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.vo.CommentVO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author klei
 */
public class CommentServiceImpl implements CommentService {

    private CommentMapper commentMapper = MapperProxy.getMapper(CommentMapper.class);
    private UserMapper userMapper = MapperProxy.getMapper(UserMapper.class);
    private GoodMapper goodMapper = MapperProxy.getMapper(GoodMapper.class);

    @Override
    public CommentVO publishComment(CommentPublishDTO dto) {
        // 校验参数
        if (dto.getGoodId() == null) {
            throw new BusinessException(400, "商品ID不能为空");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new BusinessException(400, "评论内容不能为空");
        }
        if (dto.getUserId() == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }

        // 校验商品是否存在且未下架
        Good good = goodMapper.findById(dto.getGoodId());
        if (good == null || good.getStatus() == 0) {
            throw new BusinessException(404, "商品不存在或已下架");
        }

        // 校验用户是否存在
        User user = userMapper.findById(dto.getUserId());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 插入评论
        commentMapper.insert(dto.getGoodId(), dto.getUserId(), dto.getContent().trim());

        // 查询刚插入的评论（根据用户ID和商品ID查最新的）
        List<Comment> comments = commentMapper.findByGoodId(dto.getGoodId());
        Comment newComment = null;
        for (Comment c : comments) {
            if (c.getUserId().equals(dto.getUserId())) {
                newComment = c;
                break;
            }
        }

        if (newComment == null) {
            throw new BusinessException(500, "评论发表失败");
        }

        // 组装 VO（包含用户信息）
        return convertToVO(newComment, user);
    }

    @Override
    public boolean deleteComment(Integer commentId, Integer operatorId) {
        if (commentId == null || operatorId == null) {
            throw new BusinessException(400, "参数不能为空");
        }

        // 查询评论是否存在
        Comment comment = commentMapper.findById(commentId);
        if (comment == null || comment.getStatus() == 0) {
            throw new BusinessException(404, "评论不存在或已删除");
        }

        // 查询操作者（判断权限）
        User operator = userMapper.findById(operatorId);
        if (operator == null) {
            throw new BusinessException(404, "操作者不存在");
        }

        // 权限校验：只能删自己的评论，或管理员（role=1）可以删任何人的
        if (!comment.getUserId().equals(operatorId) && operator.getRole() != 1) {
            throw new BusinessException(403, "无权删除他人评论");
        }

        // 逻辑删除
        commentMapper.deleteById(commentId);
        return true;
    }

    @Override
    public List<CommentVO> getCommentsByGoodId(Integer goodId) {
        if (goodId == null) {
            throw new BusinessException(400, "商品ID不能为空");
        }

        // 查询所有评论
        List<Comment> comments = commentMapper.findByGoodId(goodId);

        // 转换为 VO 列表
        List<CommentVO> voList = new ArrayList<>();
        for (Comment comment : comments) {
            User user = userMapper.findById(comment.getUserId());
            if (user != null) {
                voList.add(convertToVO(comment, user));
            }
        }

        return voList;
    }

    // 内部方法：Comment + User -> CommentVO
    private CommentVO convertToVO(Comment comment, User user) {
        CommentVO vo = new CommentVO();
        vo.setCommentId(comment.getId());
        vo.setContent(comment.getContent());
        vo.setCreateTime(comment.getCreateTime());
        vo.setUserId(user.getId());
        vo.setUserName(user.getUserName());
        vo.setAvatar(user.getAvatar());
        vo.setLikeCount(0);
        return vo;
    }
}