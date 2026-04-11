package com.klei.goodfish.service.impl;

import com.klei.goodfish.dto.FavoriteAddDTO;
import com.klei.goodfish.entity.Favorite;
import com.klei.goodfish.entity.Good;
import com.klei.goodfish.mapper.FavoriteMapper;
import com.klei.goodfish.mapper.GoodMapper;
import com.klei.goodfish.mappercore.proxy.MapperProxy;
import com.klei.goodfish.service.FavoriteService;
import com.klei.goodfish.util.BusinessException;

import java.util.List;

/**
 * @author klei
 */
public class FavoriteServiceImpl implements FavoriteService {

    private FavoriteMapper favoriteMapper = MapperProxy.getMapper(FavoriteMapper.class);
    private GoodMapper goodMapper = MapperProxy.getMapper(GoodMapper.class);

    @Override
    public boolean addFavorite(FavoriteAddDTO dto) {
        if (dto.getUserId() == null || dto.getGoodId() == null) {
            throw new BusinessException(400, "参数不能为空");
        }

        // 校验商品是否存在且未下架
        Good good = goodMapper.findById(dto.getGoodId());
        if (good == null || good.getStatus() == 0) {
            throw new BusinessException(404, "商品不存在或已下架");
        }

        // 检查是否已收藏（不能重复收藏）
        Favorite exist = favoriteMapper.findByUserIdAndGoodId(dto.getUserId(), dto.getGoodId());
        if (exist != null) {
            throw new BusinessException(400, "已收藏该商品，请勿重复收藏");
        }

        // 添加收藏
        favoriteMapper.insert(dto.getUserId(), dto.getGoodId());
        return true;
    }

    @Override
    public boolean cancelFavorite(FavoriteAddDTO dto) {
        if (dto.getUserId() == null || dto.getGoodId() == null) {
            throw new BusinessException(400, "参数不能为空");
        }

        // 检查是否已收藏（没收藏过不能取消）
        Favorite exist = favoriteMapper.findByUserIdAndGoodId(dto.getUserId(), dto.getGoodId());
        if (exist == null) {
            throw new BusinessException(400, "未收藏该商品，无法取消");
        }

        // 删除收藏记录（物理删除）
        favoriteMapper.delete(dto.getUserId(), dto.getGoodId());
        return true;
    }

    @Override
    public boolean isFavorited(Integer userId, Integer goodId) {
        if (userId == null || goodId == null) {
            return false;
        }
        return favoriteMapper.findByUserIdAndGoodId(userId, goodId) != null;
    }

    @Override
    // 获取用户收藏总数
    public int getFavoriteCount(Integer userId) {
        return favoriteMapper.countByUserId(userId);
    }


    @Override
    // 查某个用户的所有收藏
    public List<Favorite> getUserFavorites(Integer userId) {
        if (userId == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        return favoriteMapper.findByUserId(userId);
    }


}