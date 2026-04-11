package com.klei.goodfish.service;

import com.klei.goodfish.dto.OrderConfirmDTO;
import com.klei.goodfish.dto.OrderCreateDTO;
import com.klei.goodfish.entity.Order;
import com.klei.goodfish.vo.OrderVO;
import java.util.List;

/**
 * @author klei
 */
public interface OrderService {

    // 创建订单
    Order createOrder(OrderCreateDTO dto);

    // 确认订单
    boolean confirmOrder(OrderConfirmDTO dto);

    // 取消订单
    boolean cancelOrder(OrderConfirmDTO dto);

    // 查询订单详情
    OrderVO getOrderDetail(Integer orderId);

    // 关键：查询我购买的订单（买家视角）
    List<OrderVO> getMyPurchases(Integer buyerId);

    // 关键：查询我出售的订单（卖家视角）
    List<OrderVO> getMySales(Integer sellerId);
}