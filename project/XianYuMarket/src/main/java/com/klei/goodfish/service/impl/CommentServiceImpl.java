package com.klei.goodfish.service.impl;

import com.klei.goodfish.dto.CommentPublishDTO;
import com.klei.goodfish.entity.Comment;
import com.klei.goodfish.entity.Good;
import com.klei.goodfish.entity.User;
import com.klei.goodfish.mapper.CommentMapper;
import com.klei.goodfish.mapper.CommentLikeMapper;
import com.klei.goodfish.mapper.GoodMapper;
import com.klei.goodfish.mapper.UserMapper;
import com.klei.goodfish.mappercore.proxy.MapperProxy;
import com.klei.goodfish.service.CommentService;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.vo.CommentVO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommentServiceImpl implements CommentService {

    private CommentMapper commentMapper = MapperProxy.getMapper(CommentMapper.class);
    private UserMapper userMapper = MapperProxy.getMapper(UserMapper.class);
    private CommentLikeMapper likeMapper = MapperProxy.getMapper(CommentLikeMapper.class);
    private GoodMapper goodMapper = MapperProxy.getMapper(GoodMapper.class);

    @Override
    public CommentVO publishComment(CommentPublishDTO dto) {
        // 参数校验
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

        try {
            // 插入评论
            System.out.println("[CommentService] 准备插入评论：goodId=" + dto.getGoodId() + ", userId=" + dto.getUserId());
            commentMapper.insert(dto.getGoodId(), dto.getUserId(), dto.getContent().trim());
            System.out.println("[CommentService] 评论插入成功");

            // 查询刚插入的评论（通过倒序查找当前用户的最新评论）
            List<Comment> comments = commentMapper.findByGoodId(dto.getGoodId());
            System.out.println("[CommentService] 插入后查询到评论数：" + (comments != null ? comments.size() : 0));

            Comment newComment = null;

            // 倒序查找最新的匹配评论
            for (int i = comments.size() - 1; i >= 0; i--) {
                Comment c = comments.get(i);
                // 关键修复：防止空指针，使用Objects.equals 或确保不为null
                if (c != null && c.getUserId() != null && c.getUserId().equals(dto.getUserId())) {
                    // 再验证内容是否匹配（防止并发时拿到其他评论）
                    if (dto.getContent().trim().equals(c.getContent())) {
                        newComment = c;
                        System.out.println("[CommentService] 找到刚插入的评论，id=" + c.getId());
                        break;
                    }
                }
            }

            // 如果找不到（极端情况），构造一个返回对象，避免500错误
            if (newComment == null) {
                System.err.println("[CommentService] 警告：未能查询到刚插入的评论，返回构造对象");
                CommentVO vo = new CommentVO();
                vo.setContent(dto.getContent().trim());
                vo.setUserId(dto.getUserId());
                vo.setUserName(user.getUserName());
                vo.setAvatar(user.getAvatar());
                // 由于没有ID，设置为0或特殊值
                vo.setCommentId(0);
                vo.setLikeCount(0);
                vo.setHasLiked(false);
                return vo;
            }

            // 组装VO
            return convertToVO(newComment, user, dto.getUserId());
        } catch (Exception e) {
            System.err.println("[CommentService] 发布评论异常: " + e.getMessage());
            e.printStackTrace();
            throw new BusinessException(500, "评论发布失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteComment(Integer commentId, Integer operatorId) {
        if (commentId == null || operatorId == null) {
            throw new BusinessException(400, "参数不能为空");
        }

        Comment comment = commentMapper.findById(commentId);
        if (comment == null || comment.getStatus() == 0) {
            throw new BusinessException(404, "评论不存在或已删除");
        }

        User operator = userMapper.findById(operatorId);
        if (operator == null) {
            throw new BusinessException(404, "操作者不存在");
        }

        // 权限校验：只能删除自己的评论，或管理员(role=1)
        if (!operatorId.equals(comment.getUserId()) && operator.getRole() != 1) {
            throw new BusinessException(403, "无权删除他人评论");
        }

        commentMapper.deleteById(commentId);
        return true;
    }

    @Override
    public List<CommentVO> getCommentsByGoodId(Integer goodId, Integer currentUserId) {
        if (goodId == null) {
            throw new BusinessException(400, "商品ID不能为空");
        }

        List<Comment> comments = commentMapper.findByGoodId(goodId);

        // 关键新增：详细诊断日志
        System.out.println("[CommentService] 从数据库查询到原始评论数: " + (comments != null ? comments.size() : 0) + ", goodId=" + goodId);

        List<CommentVO> voList = new ArrayList<>();

        if (comments == null) {
            System.out.println("[CommentService] 查询结果为 null");
            return voList;
        }

        for (int i = 0; i < comments.size(); i++) {
            Comment comment = comments.get(i);
            try {
                // 关键新增：打印原始评论对象的所有字段，用于诊断
                System.out.println("[CommentService] 第 " + i + " 条评论原始数据: id=" + comment.getId()
                        + ", goodId=" + comment.getGoodId()
                        + ", userId=" + comment.getUserId()  // 如果这个值是 null，说明 MapperProxy 映射失败！
                        + ", content=" + (comment.getContent() != null ? comment.getContent().substring(0, Math.min(30, comment.getContent().length())) : "null")
                        + ", status=" + comment.getStatus()
                        + ", createTime=" + comment.getCreateTime());

                if (comment == null) {
                    System.out.println("[CommentService] 第 " + i + " 条评论对象为 null，跳过");
                    continue;
                }

                // 检查评论的 userId 是否存在
                if (comment.getUserId() == null) {
                    System.err.println("[CommentService] 严重警告：评论ID " + comment.getId() + " 的 userId 为 null！");
                    System.err.println("[CommentService] 这通常意味着 MapperProxy 字段映射失败（下划线转驼峰失败）");
                    continue;
                }

                User user = userMapper.findById(comment.getUserId());
                if (user != null) {
                    CommentVO vo = convertToVO(comment, user, currentUserId);
                    if (vo != null) {
                        voList.add(vo);
                        System.out.println("[CommentService] 第 " + i + " 条评论转换成功");
                    }
                } else {
                    System.err.println("[CommentService] 警告：未找到评论ID " + comment.getId() + " 对应的用户" + comment.getUserId());
                }
            } catch (Exception e) {
                System.err.println("[CommentService] 处理第 " + i + " 条评论异常: " + e.getMessage());
                e.printStackTrace();
                continue;
            }
        }

        System.out.println("[CommentService] 最终返回前端的 VO 数: " + voList.size());
        return voList;
    }

    // 关键修复：增加 null 检查
    private CommentVO convertToVO(Comment comment, User user, Integer currentUserId) {
        if (comment == null || user == null) {
            System.err.println("[CommentService] convertToVO 收到 null 参数: comment=" + comment + ", user=" + user);
            return null;
        }

        try {
            CommentVO vo = new CommentVO();
            vo.setCommentId(comment.getId());
            vo.setContent(comment.getContent());
            vo.setCreateTime(comment.getCreateTime());
            vo.setUserId(user.getId());
            vo.setUserName(user.getUserName());
            vo.setAvatar(user.getAvatar());

            // 查询真实点赞数
            int likeCount = likeMapper.countByCommentId(comment.getId());
            vo.setLikeCount(likeCount);

            // 查询当前用户是否已点赞
            if (currentUserId != null) {
                boolean hasLiked = likeMapper.findByBothId(comment.getId(), currentUserId) != null;
                vo.setHasLiked(hasLiked);
            } else {
                vo.setHasLiked(false);
            }

            return vo;
        } catch (Exception e) {
            System.err.println("[CommentService] convertToVO 异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getCommentCount(Integer goodId) {
        if (goodId == null) {
            return 0;
        }
        return commentMapper.countByGoodId(goodId);
    }

    @Override
    public boolean adminDeleteComment(Integer commentId) {
        if (commentId == null) {
            throw new BusinessException(400, "评论ID不能为空");
        }

        Comment comment = commentMapper.findById(commentId);
        if (comment == null || comment.getStatus() == 0) {
            throw new BusinessException(404, "评论不存在或已删除");
        }

        commentMapper.deleteById(commentId);
        return true;
    }
}