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
import com.klei.goodfish.mapper.WalletLogMapper;
import com.klei.goodfish.mappercore.proxy.MapperProxy;
import com.klei.goodfish.service.OrderService;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.util.DBUtil;
import com.klei.goodfish.vo.OrderVO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderServiceImpl implements OrderService {

    private OrderMapper orderMapper = MapperProxy.getMapper(OrderMapper.class);
    private GoodMapper goodMapper = MapperProxy.getMapper(GoodMapper.class);
    private UserMapper userMapper = MapperProxy.getMapper(UserMapper.class);
    private WalletLogMapper walletLogMapper = MapperProxy.getMapper(WalletLogMapper.class);

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

        try {
            // 关键修复：开启事务
            DBUtil.beginTransaction();

            // 1. 原子性占用商品（防止并发重复购买）
            int updatedRows = goodMapper.markAsSold(dto.getGoodId());
            if (updatedRows == 0) {
                throw new BusinessException(400, "商品已被其他买家购买，请选择其他商品");
            }

            // 2. 检查余额（在事务内查询，确保数据一致性）
            User buyer = userMapper.findById(dto.getBuyerId());
            System.out.println("[Order] 买家ID: " + dto.getBuyerId() + ", 当前余额: " + buyer.getWallet() + ", 商品价格: " + good.getGoodPrice());

            if (buyer.getWallet().compareTo(good.getGoodPrice()) < 0) {
                throw new BusinessException(400, "余额不足，当前余额：" + buyer.getWallet() + "，需要：" + good.getGoodPrice());
            }

            // 3. 买家扣款（平扣台托管）- 直接在事务内操作，不调用外部Service避免连接不一致
            BigDecimal before = buyer.getWallet();
            BigDecimal after = before.subtract(good.getGoodPrice());
            userMapper.updateWallet(after, dto.getBuyerId());

            // 记录流水（type=2 购买，金额为负数表示支出）
            walletLogMapper.insert(dto.getBuyerId(),
                    good.getGoodPrice().negate(),
                    before,
                    after,
                    2,
                    null);
            System.out.println("[Order] 扣款成功: " + good.getGoodPrice());

            // 4. 创建订单
            orderMapper.insert(dto.getGoodId(), dto.getBuyerId(), good.getSellerId(), good.getGoodPrice());
            System.out.println("[Order] 订单创建成功");

            // 5. 查询刚创建的订单
            Order newOrder = orderMapper.findByGoodId(dto.getGoodId());

            if (newOrder == null) {
                throw new BusinessException(500, "订单创建后查询失败");
            }

            // 提交事务（关键修复）
            DBUtil.commit();
            System.out.println("[Order] 事务提交成功，订单创建完成");

            return newOrder;

        } catch (BusinessException e) {
            // 业务异常，回滚事务
            DBUtil.rollback();
            System.err.println("[Order] 业务异常，事务回滚: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            // 系统异常，回滚事务
            DBUtil.rollback();
            System.err.println("[Order] 系统异常，事务回滚: " + e.getMessage());
            e.printStackTrace();
            throw new BusinessException(500, "创建订单失败：" + e.getMessage());
        }
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

        // 只有卖家能确认订单
        if (!order.getSellerId().equals(dto.getUserId())) {
            throw new BusinessException(403, "无权确认此订单");
        }

        // 检查状态（只能确认待确认的订单）
        if (order.getStatus() != 0) {
            throw new BusinessException(400, "订单状态异常，无法确认");
        }

        try {
            // 关键修复：开启事务
            DBUtil.beginTransaction();

            System.out.println("[Order] 确认订单，给卖家打款: sellerId=" + order.getSellerId() + ", amount=" + order.getPrice());

            // 1. 给卖家打款（直接在事务内操作）
            User seller = userMapper.findById(order.getSellerId());
            if (seller == null) {
                throw new BusinessException(404, "卖家不存在");
            }

            BigDecimal sellerBefore = seller.getWallet();
            BigDecimal sellerAfter = sellerBefore.add(order.getPrice());
            userMapper.updateWallet(sellerAfter, order.getSellerId());

            // 记录收入流水（type=3 收入）
            walletLogMapper.insert(order.getSellerId(),
                    order.getPrice(),
                    sellerBefore,
                    sellerAfter,
                    3,
                    order.getId());
            System.out.println("[Order] 打款成功");

            // 2. 更新订单状态为已完成（1）- 必须在同一个事务中！
            orderMapper.updateStatus(1, dto.getOrderId());
            System.out.println("[Order] 订单状态更新为已完成");

            // 提交事务（关键修复：确保打款和状态更新同时成功或同时失败）
            DBUtil.commit();
            System.out.println("[Order] 事务提交成功，订单确认完成");

            return true;

        } catch (BusinessException e) {
            DBUtil.rollback();
            System.err.println("[Order] 业务异常，事务回滚: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            DBUtil.rollback();
            System.err.println("[Order] 系统异常，事务回滚: " + e.getMessage());
            throw new BusinessException(500, "确认订单失败：" + e.getMessage());
        }
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

        try {
            // 关键修复：开启事务
            DBUtil.beginTransaction();

            // 1. 退款给买家
            User buyer = userMapper.findById(order.getBuyerId());
            BigDecimal before = buyer.getWallet();
            BigDecimal after = before.add(order.getPrice());
            userMapper.updateWallet(after, order.getBuyerId());

            // 记录退款流水（type=1 充值，表示余额增加）
            walletLogMapper.insert(order.getBuyerId(),
                    order.getPrice(),
                    before,
                    after,
                    1,
                    order.getId());
            System.out.println("[Order] 退款给买家: " + order.getBuyerId() + ", 金额: " + order.getPrice());

            // 2. 恢复商品状态为未出售
            goodMapper.markAsAvailable(order.getGoodId());
            System.out.println("[Order] 商品状态恢复为未出售");

            // 3. 更新订单状态为已取消（2）
            orderMapper.updateStatus(2, dto.getOrderId());
            System.out.println("[Order] 订单状态更新为已取消");

            // 提交事务
            DBUtil.commit();
            System.out.println("[Order] 事务提交成功，订单取消完成");

            return true;

        } catch (Exception e) {
            DBUtil.rollback();
            System.err.println("[Order] 取消订单异常，事务回滚: " + e.getMessage());
            throw new BusinessException(500, "取消订单失败：" + e.getMessage());
        }
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