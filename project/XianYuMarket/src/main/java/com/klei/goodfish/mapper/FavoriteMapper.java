package com.klei.goodfish.mapper;

import com.klei.goodfish.entity.Favorite;
import com.klei.goodfish.mappercore.Insert;
import com.klei.goodfish.mappercore.Select;
import com.klei.goodfish.mappercore.Delete;
import java.util.List;

/**
 * @author klei
 */
public interface FavoriteMapper {

    // 查询用户的所有收藏
    @Select("SELECT * FROM favorite WHERE user_id = ?")
    List<Favorite> findByUserId(Integer userId);

    // 查询特定收藏记录
    @Select("SELECT * FROM favorite WHERE user_id = ? AND good_id = ?")
    Favorite findByUserIdAndGoodId(Integer userId, Integer goodId);

    // 添加收藏
    @Insert("INSERT INTO favorite(user_id, good_id, create_time) VALUES(?, ?, NOW())")
    void insert(Integer userId, Integer goodId);

    // 取消收藏
    @Delete("DELETE FROM favorite WHERE user_id = ? AND good_id = ?")
    void delete(Integer userId, Integer goodId);

    // 统计用户收藏数
    @Select("SELECT COUNT(*) FROM favorite WHERE user_id = ?")
    int countByUserId(Integer userId);
}