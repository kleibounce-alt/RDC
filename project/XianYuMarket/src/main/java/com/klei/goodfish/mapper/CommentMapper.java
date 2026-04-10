package com.klei.goodfish.mapper;

import com.klei.goodfish.entity.Comment;
import com.klei.goodfish.mappercore.Insert;
import com.klei.goodfish.mappercore.Select;
import com.klei.goodfish.mappercore.Update;
import java.util.List;

/**
 * @author klei
 */
public interface CommentMapper {

    // 插入评论
    @Insert("INSERT INTO comment(good_id, user_id, content, status, create_time) " +
            "VALUES(?, ?, ?, 1, NOW())")
    void insert(Integer goodId, Integer userId, String content);

    // 根据ID查询
    @Select("SELECT * FROM comment WHERE id = ? AND status = 1")
    Comment findById(Integer id);

    // 根据商品ID查所有评论
    @Select("SELECT * FROM comment WHERE good_id = ? AND status = 1 ORDER BY create_time DESC")
    List<Comment> findByGoodId(Integer goodId);

    // 删除评论
    @Update("UPDATE comment SET status = 0 WHERE id = ?")
    void deleteById(Integer id);

    // 统计评论数
    @Select("SELECT COUNT(*) FROM comment WHERE good_id = ? AND status = 1")
    int countByGoodId(Integer goodId);
}