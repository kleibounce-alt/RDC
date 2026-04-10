package com.klei.goodfish.service.impl;

import com.klei.goodfish.dto.OrderConfirmDTO;
import com.klei.goodfish.dto.OrderCreateDTO;
import com.klei.goodfish.dto.WalletPayDTO;
import com.klei.goodfish.entity.Good;
import com.klei.goodfish.entity.Order;
import com.klei.goodfish.entity.User;
import com.klei.goodfish.mapper.GoodMapper;
import com.klei.goodfish.mapper.OrderMapper;
import com.klei.goodfish.mapper.UserMapper;
import com.klei.goodfish.mappercore.proxy.MapperProxy;
import com.klei.goodfish.service.OrderService;
import com.klei.goodfish.service.WalletService;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.vo.OrderVO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderServiceImpl implements OrderService {

    private OrderMapper orderMapper = MapperProxy.getMapper(OrderMapper.class);
    private GoodMapper goodMapper = MapperProxy.getMapper(GoodMapper.class);
    private UserMapper userMapper = MapperProxy.getMapper(UserMapper.class);
    private WalletService walletService = new WalletServiceImpl();

    @Override
    public Order createOrder(OrderCreateDTO dto) {
        if (dto.getBuyerId() == null || dto.getGoodId() == null) {
            throw new BusinessException(400, "参数不能为空");
        }

        // 不能买自己的商品
        Good good = goodMapper.findById(dto.getGoodId());
        if (good == null || good.getStatus() == 0) {
            throw new BusinessException(404, "商品不存在或已下架");
        }
        if (good.getSellerId().equals(dto.getBuyerId())) {
            throw new BusinessException(400, "不能购买自己的商品");
        }

        // 检查商品是否已售出
        if ("已出售".equals(good.getSellingStatus())) {
            throw new BusinessException(400, "商品已售出");
        }

        // 检查是否已有未取消的订单
        Order existOrder = orderMapper.findByGoodId(dto.getGoodId());
        if (existOrder != null) {
            throw new BusinessException(400, "商品已被他人下单，请刷新重试");
        }

        // 买家扣款
        WalletPayDTO payDTO = new WalletPayDTO();
        payDTO.setUserId(dto.getBuyerId());
        payDTO.setAmount(good.getGoodPrice());
        payDTO.setRelatedId(null);
        payDTO.setRemark("购买商品：" + good.getGoodName());

        boolean paySuccess = walletService.pay(payDTO);
        if (!paySuccess) {
            throw new BusinessException(500, "支付失败");
        }

        // 创建订单
        orderMapper.insert(dto.getGoodId(), dto.getBuyerId(), good.getSellerId(), good.getGoodPrice());

        // 查询刚创建的订单
        Order newOrder = orderMapper.findByGoodId(dto.getGoodId());
        if (newOrder == null) {
            throw new BusinessException(500, "订单创建失败");
        }

        // 改商品状态为"已出售"
        goodMapper.updateSellingStatus("已出售", dto.getGoodId());

        return newOrder;
    }

    @Override
    public boolean confirmOrder(OrderConfirmDTO dto) {
        if (dto.getOrderId() == null || dto.getUserId() == null) {
            throw new BusinessException(400, "参数不能为空");
        }

        Order order = orderMapper.findById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 只有卖家能确认订单（或买家确认收货，这里简化只有卖家确认）
        if (!order.getSellerId().equals(dto.getUserId())) {
            throw new BusinessException(403, "无权确认此订单");
        }

        // 检查状态（只能确认待确认的订单）
        if (order.getStatus() != 0) {
            throw new BusinessException(400, "订单状态异常，无法确认");
        }

        // 给卖家打钱
        walletService.income(order.getSellerId(), order.getPrice(), order.getId(), "出售商品收入");

        // 更新订单状态为已完成
        orderMapper.updateStatus(1, dto.getOrderId());

        return true;
    }

    @Override
    public boolean cancelOrder(OrderConfirmDTO dto) {
        if (dto.getOrderId() == null || dto.getUserId() == null) {
            throw new BusinessException(400, "参数不能为空");
        }

        Order order = orderMapper.findById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 买家或卖家都能取消（未确认前）
        if (!order.getBuyerId().equals(dto.getUserId()) && !order.getSellerId().equals(dto.getUserId())) {
            throw new BusinessException(403, "无权操作此订单");
        }

        // 只能取消待确认的订单
        if (order.getStatus() != 0) {
            throw new BusinessException(400, "订单已确认或已取消，无法操作");
        }

        // 退款给买家
        User buyer = userMapper.findById(order.getBuyerId());
        BigDecimal before = buyer.getWallet();
        BigDecimal after = before.add(order.getPrice());
        userMapper.updateWallet(after, order.getBuyerId());

        // 商品状态改回"未出售"
        goodMapper.updateSellingStatus("未出售", order.getGoodId());

        // 更新订单状态为已取消
        orderMapper.updateStatus(2, dto.getOrderId());

        return true;
    }

    @Override
    public OrderVO getOrderDetail(Integer orderId) {
        if (orderId == null) {
            throw new BusinessException(400, "订单ID不能为空");
        }

        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        return convertToVO(order);
    }

    @Override
    public List<OrderVO> getMyPurchases(Integer buyerId) {
        if (buyerId == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }

        List<Order> orders = orderMapper.findByBuyerId(buyerId);
        List<OrderVO> voList = new ArrayList<>();
        for (Order order : orders) {
            voList.add(convertToVO(order));
        }
        return voList;
    }

    @Override
    public List<OrderVO> getMySales(Integer sellerId) {
        if (sellerId == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }

        List<Order> orders = orderMapper.findBySellerId(sellerId);
        List<OrderVO> voList = new ArrayList<>();
        for (Order order : orders) {
            voList.add(convertToVO(order));
        }
        return voList;
    }

    // 内部转换方法
    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setOrderId(order.getId());
        vo.setGoodId(order.getGoodId());
        vo.setPrice(order.getPrice());
        vo.setStatus(order.getStatus());
        vo.setCreateTime(order.getCreateTime());

        // 状态转中文
        switch (order.getStatus()) {
            case 0: vo.setStatusName("待确认"); break;
            case 1: vo.setStatusName("已完成"); break;
            case 2: vo.setStatusName("已取消"); break;
            default: vo.setStatusName("未知");
        }

        // 查商品信息
        Good good = goodMapper.findById(order.getGoodId());
        if (good != null) {
            vo.setGoodName(good.getGoodName());
            vo.setGoodImage(good.getGoodImage());
        }

        // 查买家信息
        User buyer = userMapper.findById(order.getBuyerId());
        if (buyer != null) {
            vo.setBuyerId(buyer.getId());
            vo.setBuyerName(buyer.getUserName());
        }

        // 查卖家信息
        User seller = userMapper.findById(order.getSellerId());
        if (seller != null) {
            vo.setSellerId(seller.getId());
            vo.setSellerName(seller.getUserName());
        }

        return vo;
    }
}