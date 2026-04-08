package com.klei.goodfish.service;

import com.klei.goodfish.dto.UserFavoriteDTO;
import com.klei.goodfish.dto.UserFollowDTO;
import com.klei.goodfish.dto.UserGoodDTO;
import com.klei.goodfish.dto.UserProfileDTO;
import com.klei.goodfish.entity.User;

/**
 * @author klei
 */
public interface UserService {

    //注册


    //查看个人信息
    UserProfileDTO getUser (Integer userId);

    //查看个人收藏
    UserFavoriteDTO getUserFavorite (Integer userId);

    //查看个人关注
    UserFollowDTO getUserFollow (Integer userId);

    //查看我发布的商品
    UserGoodDTO getUserGood (Integer userId);
}
