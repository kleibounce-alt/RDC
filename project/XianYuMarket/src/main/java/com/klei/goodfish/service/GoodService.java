package com.klei.goodfish.service;

import com.klei.goodfish.dto.GoodPublishDTO;
import com.klei.goodfish.dto.GoodSearchDTO;
import com.klei.goodfish.dto.GoodUpdateDTO;
import com.klei.goodfish.entity.Good;

import java.util.List;

/**
 * @author klei
 */
public interface GoodService {

    // 发布商品
    Good publishGood(GoodPublishDTO dto);

    // 编辑商品
    Good updateGood(GoodUpdateDTO dto);

    // 删除商品
    boolean deleteGood(Integer goodId, Integer userId);

    // 查询详情
    Good getGoodDetail(Integer goodId);

    // 随机推荐
    List<Good> getRandomGoods(Integer limit);

    // 搜索商品
    List<Good> searchGoods(GoodSearchDTO dto);

    // 根据标签查
    List<Good> getGoodsByTag(Integer tagId);

    // 更新状态
    boolean updateSellingStatus(Integer goodId, String status, Integer userId);

    //查询卖家商品列表
    List<Good> getGoodsBySellerId(Integer sellerId);
}