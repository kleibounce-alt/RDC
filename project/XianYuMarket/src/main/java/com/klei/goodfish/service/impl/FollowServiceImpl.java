package com.klei.goodfish.service.impl;

import com.klei.goodfish.dto.FollowAddDTO;
import com.klei.goodfish.entity.Follow;
import com.klei.goodfish.entity.User;
import com.klei.goodfish.mapper.FollowMapper;
import com.klei.goodfish.mapper.UserMapper;
import com.klei.goodfish.mappercore.proxy.MapperProxy;
import com.klei.goodfish.service.FollowService;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.vo.FansVO;
import com.klei.goodfish.vo.FollowVO;

import java.util.ArrayList;
import java.util.List;

public class FollowServiceImpl implements FollowService {

    private FollowMapper followMapper = MapperProxy.getMapper(FollowMapper.class);
    private UserMapper userMapper = MapperProxy.getMapper(UserMapper.class);

    @Override
    public boolean follow(FollowAddDTO dto) {
        if (dto.getFollowerId() == null || dto.getFollowingId() == null) {
            throw new BusinessException(400, "参数不能为空");
        }

        // 不能关注自己
        if (dto.getFollowerId().equals(dto.getFollowingId())) {
            throw new BusinessException(400, "不能关注自己");
        }

        // 校验被关注者是否存在
        User target = userMapper.findById(dto.getFollowingId());
        if (target == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 检查是否已关注（不能重复关注）
        Follow exist = followMapper.findByBothId(dto.getFollowerId(), dto.getFollowingId());
        if (exist != null) {
            throw new BusinessException(400, "已关注该用户，请勿重复关注");
        }

        // 添加关注
        followMapper.insert(dto.getFollowerId(), dto.getFollowingId());
        return true;
    }

    @Override
    public boolean unfollow(FollowAddDTO dto) {
        if (dto.getFollowerId() == null || dto.getFollowingId() == null) {
            throw new BusinessException(400, "参数不能为空");
        }

        // 检查是否已关注（没关注过不能取消）
        Follow exist = followMapper.findByBothId(dto.getFollowerId(), dto.getFollowingId());
        if (exist == null) {
            throw new BusinessException(400, "未关注该用户，无法取消");
        }

        // 取消关注
        followMapper.delete(dto.getFollowerId(), dto.getFollowingId());
        return true;
    }

    @Override
    public List<FollowVO> getMyFollowings(Integer followerId) {
        if (followerId == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }

        List<Follow> follows = followMapper.findByFollowerId(followerId);
        List<FollowVO> voList = new ArrayList<>();

        for (Follow f : follows) {
            User user = userMapper.findById(f.getFollowingId());
            if (user != null) {
                FollowVO vo = new FollowVO();
                vo.setFollowId(f.getId());
                vo.setFollowingId(user.getId());
                vo.setFollowingName(user.getUserName());
                vo.setFollowingAvatar(user.getAvatar());
                vo.setFollowTime(f.getCreateTime());
                voList.add(vo);
            }
        }
        return voList;
    }

    @Override
    public List<FansVO> getMyFans(Integer followingId) {
        if (followingId == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }

        List<Follow> fans = followMapper.findByFollowingId(followingId);
        List<FansVO> voList = new ArrayList<>();

        for (Follow f : fans) {
            User user = userMapper.findById(f.getFollowerId());
            if (user != null) {
                FansVO vo = new FansVO();
                vo.setFollowId(f.getId());
                vo.setFollowerId(user.getId());
                vo.setFollowerName(user.getUserName());
                vo.setFollowerAvatar(user.getAvatar());
                vo.setFollowTime(f.getCreateTime());
                voList.add(vo);
            }
        }
        return voList;
    }

    @Override
    public boolean isFollowing(Integer followerId, Integer followingId) {
        if (followerId == null || followingId == null) {
            return false;
        }
        return followMapper.findByBothId(followerId, followingId) != null;
    }

    @Override
    public int getFollowingCount(Integer followerId) {
        if (followerId == null) {
            return 0;
        }
        return followMapper.countFollowing(followerId);
    }

    @Override
    public int getFollowersCount(Integer followingId) {
        if (followingId == null) {
            return 0;
        }
        return followMapper.countFollowers(followingId);
    }

    @Override
    public List<Follow> getUserFollows(Integer userId) {
        if (userId == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        return followMapper.findByFollowerId(userId);
    }
}