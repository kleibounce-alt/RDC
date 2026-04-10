package com.klei.goodfish.mapper;

import com.klei.goodfish.entity.CommentLike;
import com.klei.goodfish.mappercore.Insert;
import com.klei.goodfish.mappercore.Select;
import com.klei.goodfish.mappercore.Delete;
import java.util.List;

/**
 * @author klei
 */
public interface CommentLikeMapper {

    // 点赞
    @Insert("INSERT INTO comment_like(comment_id, user_id, create_time) VALUES(?, ?, NOW())")
    void insert(Integer commentId, Integer userId);

    // 取消点赞
    @Delete("DELETE FROM comment_like WHERE comment_id = ? AND user_id = ?")
    void delete(Integer commentId, Integer userId);

    // 查询特定点赞记录
    @Select("SELECT * FROM comment_like WHERE comment_id = ? AND user_id = ?")
    CommentLike findByBothId(Integer commentId, Integer userId);

    // 查询评论的所有点赞
    @Select("SELECT * FROM comment_like WHERE comment_id = ?")
    List<CommentLike> findByCommentId(Integer commentId);

    // 统计评论的点赞数
    @Select("SELECT COUNT(*) FROM comment_like WHERE comment_id = ?")
    int countByCommentId(Integer commentId);

    // 统计用户的所有点赞
    @Select("SELECT * FROM comment_like WHERE user_id = ?")
    List<CommentLike> findByUserId(Integer userId);
}