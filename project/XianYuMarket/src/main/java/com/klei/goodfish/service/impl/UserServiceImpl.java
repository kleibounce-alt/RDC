package com.klei.goodfish.service.impl;

import com.klei.goodfish.dto.*;
import com.klei.goodfish.mappercore.Insert;
import com.klei.goodfish.vo.*;
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
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;
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
    //注册
    public UserRegisterVO register (UserRegisterDTO dto) {
        String error = dto.validate();
        if (error != null) {
            return UserRegisterVO.fail(error);
        }
        UserRegisterVO vo = new UserRegisterVO();

        User user = userMapper.findByName(dto.getUserName());
        if (user != null) {
            return UserRegisterVO.fail("用户名重复！");
        }

        user.setUserName(dto.getUserName());
        //加密
        user.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        user.setAvatar("");
        user.setRole(dto.getRole());
        user.setStatus(1);
        user.setWallet(BigDecimal.ZERO);
        user.setCreateTime(LocalDateTime.now());

        userMapper.insert(
                user.getUserName(),
                user.getPassword(),
                user.getRole(),
                user.getStatus(),
                user.getWallet(),
                user.getCreateTime(),
                user.getAvatar()
        );

        User savedUser = userMapper.findByName(dto.getUserName());

        return UserRegisterVO.success(savedUser);
    }

    //登录
    @Override
    public UserLoginVO login(UserLoginDTO dto) {
        String userName = dto.getUserName();
        User user = userMapper.findByName(userName);
        if (user == null) {
            return UserLoginVO.fail("用户名或密码错误！");
        }
        String password = dto.getPassword();
        boolean check = BCrypt.checkpw(password, user.getPassword());
        if (!check) {
            return UserLoginVO.fail("用户名或密码错误！");
        }
        return UserLoginVO.success(user);
    }

    @Override
    //查看个人信息
    public UserProfileVO getUser (Integer userId) {
        User user = userMapper.findById(userId);

        if (user == null) {
            return null;
        }

        UserProfileVO vo = new UserProfileVO();
        vo.setUserId(user.getId());
        vo.setUserName(user.getUserName());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setWallet(user.getWallet());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    @Override
    //查看个人收藏
    public UserFavoriteVO getUserFavorite (Integer userId) {
        List<Favorite> favoriteList = favoriteMapper.findByUserId(userId);
        if (favoriteList == null || favoriteList.isEmpty()) {
            return null;
        }


        UserFavoriteVO vo = new UserFavoriteVO();

        List<UserFavoriteDTO> dtoList = new ArrayList<>();
        for (Favorite favorite : favoriteList) {
            UserFavoriteDTO dto = new UserFavoriteDTO();
            Good good = goodMapper.findById(favorite.getGoodId());
            dto.setGood(good);
            dto.setFavoriteTime(favorite.getCreateTime());
            dtoList.add(dto);
        }

        vo.setUserId(userId);
        vo.setFavorites(dtoList);
        return vo;
    }

    //查看我的关注
    @Override
    public UserFollowVO getUserFollow (Integer userId) {
        List<Follow> followList = followMapper.findByUserId(userId);
        if (followList == null || followList.isEmpty()) {
            return null;
        }

        UserFollowVO vo = new UserFollowVO();
        List<UserFollowDTO> dtoList = new ArrayList<>();
        for (Follow follow : followList) {
            UserFollowDTO dto = new UserFollowDTO();
            User user = userMapper.findById(follow.getFollowingId());
            dto.setFollowingUser(user);
            dto.setFollowTime(follow.getCreateTime());
            dtoList.add(dto);
        }
        vo.setUserId(userId);
        vo.setFollowings(dtoList);
        return vo;
    }

    //查看我发布的商品
    @Override
    public UserGoodVO getUserGood (Integer userId) {
        List<Good> goodList = goodMapper.findBySellerId(userId);

        if (goodList == null || goodList.isEmpty()) {
            return null;
        }

        UserGoodVO vo = new UserGoodVO();
        List<UserGoodDTO> dtoList = new ArrayList<>();
        for (Good good : goodList) {
            UserGoodDTO dto = new UserGoodDTO();
            dto.setGood(good);
            dto.setReleaseTime(good.getCreateTime());
            dtoList.add(dto);
        }
        vo.setUserId(userId);
        vo.setGoods(dtoList);
        return vo;
    }

    

}
