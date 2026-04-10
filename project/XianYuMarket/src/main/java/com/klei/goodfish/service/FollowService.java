package com.klei.goodfish.service;

import com.klei.goodfish.dto.FollowAddDTO;
import com.klei.goodfish.entity.Follow;
import com.klei.goodfish.vo.FansVO;
import com.klei.goodfish.vo.FollowVO;
import java.util.List;

/**
 * @author klei
 */
public interface FollowService {
    // 关注用户
    boolean follow(FollowAddDTO dto);

    // 取消关注
    boolean unfollow(FollowAddDTO dto);

    // 获取我的关注列表
    List<FollowVO> getMyFollowings(Integer followerId);

    // 获取我的粉丝列表
    List<FansVO> getMyFans(Integer followingId);

    // 检查是否已关注
    boolean isFollowing(Integer followerId, Integer followingId);

    // 获取关注数
    int getFollowingCount(Integer followerId);

    // 获取粉丝数
    int getFollowersCount(Integer followingId);

    // 查某个用户的所有关注
    List<Follow> getUserFollows(Integer userId);
}