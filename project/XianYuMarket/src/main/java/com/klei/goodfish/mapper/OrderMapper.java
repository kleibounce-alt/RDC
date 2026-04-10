package com.klei.goodfish.mapper;

import com.klei.goodfish.entity.Order;
import com.klei.goodfish.mappercore.Insert;
import com.klei.goodfish.mappercore.Select;
import com.klei.goodfish.mappercore.Update;

import java.math.BigDecimal;
import java.util.List;

public interface OrderMapper {

    // 创建订单
    @Insert("INSERT INTO `order`(good_id, buyer_id, seller_id, price, status, create_time) " +
            "VALUES(?, ?, ?, ?, 0, NOW())")
    void insert(Integer goodId, Integer buyerId, Integer sellerId, BigDecimal price);

    // 根据ID查询订单
    @Select("SELECT * FROM `order` WHERE id = ?")
    Order findById(Integer id);

    // 查询买家的所有订单（我购买的）
    @Select("SELECT * FROM `order` WHERE buyer_id = ? ORDER BY create_time DESC")
    List<Order> findByBuyerId(Integer buyerId);

    // 查询卖家的所有订单（我卖出的）
    @Select("SELECT * FROM `order` WHERE seller_id = ? ORDER BY create_time DESC")
    List<Order> findBySellerId(Integer sellerId);

    // 更新订单状态
    @Update("UPDATE `order` SET status = ? WHERE id = ?")
    void updateStatus(Integer status, Integer orderId);

    // 根据商品ID查订单
    @Select("SELECT * FROM `order` WHERE good_id = ? AND status != 2")
    Order findByGoodId(Integer goodId);
}