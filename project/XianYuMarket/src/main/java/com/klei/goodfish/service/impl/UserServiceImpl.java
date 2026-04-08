package com.klei.goodfish.service.impl;

import com.klei.goodfish.dto.UserFavoriteDTO;
import com.klei.goodfish.dto.UserFollowDTO;
import com.klei.goodfish.dto.UserGoodDTO;
import com.klei.goodfish.dto.UserProfileDTO;
import com.klei.goodfish.entity.Favorite;
import com.klei.goodfish.entity.Follow;
import com.klei.goodfish.entity.Good;
import com.klei.goodfish.entity.User;
import com.klei.goodfish.mapper.FavoriteMapper;
import com.klei.goodfish.mapper.FollowMapper;
import com.klei.goodfish.mapper.GoodMapper;
import com.klei.goodfish.mapper.UserMapper;
import com.klei.goodfish.mappercore.proxy.MapperProxy;
import com.klei.goodfish.service.UserService;
import com.klei.goodfish.vo.UserFavoriteVO;
import com.klei.goodfish.vo.UserFollowVO;
import com.klei.goodfish.vo.UserGoodVO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author klei
 */
public class UserServiceImpl implements UserService {

    private UserMapper userMapper = MapperProxy.getMapper(UserMapper.class);

    private FavoriteMapper favoriteMapper = MapperProxy.getMapper(FavoriteMapper.class);

    private GoodMapper goodMapper = MapperProxy.getMapper(GoodMapper.class);

    private FollowMapper followMapper = MapperProxy.getMapper(FollowMapper.class);

    @Override
    //查看个人信息
    public UserProfileDTO getUser (Integer userId) {
        User user = userMapper.findById(userId);

        if (user == null) {
            return null;
        }

        UserProfileDTO dto = new UserProfileDTO();
        dto.setUserId(user.getId());
        dto.setUserName(user.getUserName());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setWallet(user.getWallet());
        dto.setCreateTime(user.getCreateTime());

        return dto;
    }

    @Override
    //查看个人收藏
    public UserFavoriteDTO getUserFavorite (Integer userId) {
        List<Favorite> favoriteList = favoriteMapper.findByUserId(userId);
        if (favoriteList == null || favoriteList.isEmpty()) {
            return null;
        }


        UserFavoriteDTO dto = new UserFavoriteDTO();

        List<UserFavoriteVO> voList = new ArrayList<>();
        for (Favorite favorite : favoriteList) {
            UserFavoriteVO vo = new UserFavoriteVO();
            Good good = goodMapper.findById(favorite.getGoodId());
            vo.setGood(good);
            vo.setFavoriteTime(favorite.getCreateTime());
            voList.add(vo);
        }

        dto.setUserId(userId);
        dto.setFavorites(voList);
        return dto;
    }

    //查看我的关注
    @Override
    public UserFollowDTO getUserFollow (Integer userId) {
        List<Follow> followList = followMapper.findByUserId(userId);
        if (followList == null || followList.isEmpty()) {
            return null;
        }

        UserFollowDTO dto = new UserFollowDTO();
        List<UserFollowVO> voList = new ArrayList<>();
        for (Follow follow : followList) {
            UserFollowVO vo = new UserFollowVO();
            User user = userMapper.findById(follow.getFollowingId());
            vo.setFollowingUser(user);
            vo.setFollowTime(follow.getCreateTime());
            voList.add(vo);
        }
        dto.setUserId(userId);
        dto.setFollowings(voList);
        return dto;
    }

    //查看我发布的商品
    @Override
    public UserGoodDTO getUserGood (Integer userId) {
        List<Good> goodList = goodMapper.findBySellerId(userId);

        if (goodList == null || goodList.isEmpty()) {
            return null;
        }

        UserGoodDTO dto = new UserGoodDTO();
        List<UserGoodVO> voList = new ArrayList<>();
        for (Good good : goodList) {
            UserGoodVO vo = new UserGoodVO();
            vo.setGood(good);
            vo.setReleaseTime(good.getCreateTime());
            voList.add(vo);
        }
        dto.setUserId(userId);
        dto.setGoods(voList);
        return dto;
    }


}
