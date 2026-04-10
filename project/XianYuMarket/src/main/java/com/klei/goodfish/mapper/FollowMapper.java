package com.klei.goodfish.mapper;

import com.klei.goodfish.entity.Follow;
import com.klei.goodfish.mappercore.Delete;
import com.klei.goodfish.mappercore.Insert;
import com.klei.goodfish.mappercore.Select;

import java.util.List;

/**
 * @author klei
 */
public interface FollowMapper {
    //查询我的关注列表
    @Select("SELECT * FROM follow WHERE follower_id = ?")
    List<Follow> findByFollowerId(Integer followerId);

    //查询我的粉丝列表（谁关注了我）
    @Select("SELECT * FROM follow WHERE following_id = ?")
    List<Follow> findByFollowingId(Integer followingId);

    //查询特定关注记录
    @Select("SELECT * FROM follow WHERE follower_id = ? AND following_id = ?")
    Follow findByBothId(Integer followerId, Integer followingId);

    //添加关注
    @Insert("INSERT INTO follow(follower_id, following_id, create_time) VALUES(?, ?, NOW())")
    void insert(Integer followerId, Integer followingId);

    //取消关注
    @Delete("DELETE FROM follow WHERE follower_id = ? AND following_id = ?")
    void delete(Integer followerId, Integer followingId);

    //关注数
    @Select("SELECT COUNT(*) FROM follow WHERE follower_id = ?")
    int countFollowing(Integer followerId);

    //粉丝数
    @Select("SELECT COUNT(*) FROM follow WHERE following_id = ?")
    int countFollowers(Integer followingId);
}
