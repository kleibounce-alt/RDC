package com.klei.goodfish.service;

import com.klei.goodfish.dto.FavoriteAddDTO;
import com.klei.goodfish.entity.Favorite;

import java.util.List;

/**
 * @author klei
 */
public interface FavoriteService {
    // 添加收藏
    boolean addFavorite(FavoriteAddDTO dto);

    // 取消收藏
    boolean cancelFavorite(FavoriteAddDTO dto);

    // 检查是否已收藏
    boolean isFavorited(Integer userId, Integer goodId);

    // 获取用户收藏总数
    int getFavoriteCount(Integer userId);

    
}