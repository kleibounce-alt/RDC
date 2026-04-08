package com.klei.goodfish.service;

import com.klei.goodfish.dto.UserFavoriteDTO;
import com.klei.goodfish.dto.UserLoginDTO;
import com.klei.goodfish.dto.UserRegisterDTO;
import com.klei.goodfish.vo.*;

/**
 * @author klei
 */
public interface UserService {

    //注册
    UserRegisterVO register(UserRegisterDTO dto);

    //登录
    UserLoginVO login(UserLoginDTO dto);

    //查看个人信息
    UserProfileVO getUser (Integer userId);

    //查看个人收藏
    UserFavoriteVO getUserFavorite (Integer userId);

    //查看个人关注
    UserFollowVO getUserFollow (Integer userId);

    //查看我发布的商品
    UserGoodVO getUserGood (Integer userId);
}
