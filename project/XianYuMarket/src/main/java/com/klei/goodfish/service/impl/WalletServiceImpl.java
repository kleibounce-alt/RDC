package com.klei.goodfish.service.impl;

import com.klei.goodfish.dto.WalletPayDTO;
import com.klei.goodfish.dto.WalletRechargeDTO;
import com.klei.goodfish.entity.User;
import com.klei.goodfish.entity.WalletLog;
import com.klei.goodfish.mapper.UserMapper;
import com.klei.goodfish.mapper.WalletLogMapper;
import com.klei.goodfish.mappercore.proxy.MapperProxy;
import com.klei.goodfish.service.WalletService;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.vo.WalletBalanceVO;
import com.klei.goodfish.vo.WalletLogVO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class WalletServiceImpl implements WalletService {

    private WalletLogMapper walletLogMapper = MapperProxy.getMapper(WalletLogMapper.class);
    private UserMapper userMapper = MapperProxy.getMapper(UserMapper.class);

    @Override
    public boolean recharge(WalletRechargeDTO dto) {
        if (dto.getUserId() == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "充值金额必须大于0");
        }

        // 查询用户
        User user = userMapper.findById(dto.getUserId());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 计算新余额
        BigDecimal before = user.getWallet();
        BigDecimal after = before.add(dto.getAmount());

        // 更新用户余额
        userMapper.updateWallet(after, dto.getUserId());

        // 记录流水（type=1 充值）
        walletLogMapper.insert(dto.getUserId(), dto.getAmount(), before, after, 1, null);

        return true;
    }

    @Override
    public boolean pay(WalletPayDTO dto) {
        if (dto.getUserId() == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "支付金额必须大于0");
        }

        // 查询用户
        User user = userMapper.findById(dto.getUserId());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 检查余额是否充足
        BigDecimal before = user.getWallet();
        if (before.compareTo(dto.getAmount()) < 0) {
            throw new BusinessException(400, "余额不足，请先充值");
        }

        // 计算新余额（扣款）
        BigDecimal after = before.subtract(dto.getAmount());

        // 更新余额
        userMapper.updateWallet(after, dto.getUserId());

        // 记录流水（type=2 购买，负数表示支出）
        // 注意：amount 存负数还是正数？建议存实际变动值（负数），但balance_before/after正常记录
        // 或者存正数，用type区分。这里存负数表示支出，更直观
        walletLogMapper.insert(dto.getUserId(),
                dto.getAmount().negate(), // 转为负数表示支出
                before,
                after,
                2,
                dto.getRelatedId());

        return true;
    }

    @Override
    public WalletBalanceVO getBalance(Integer userId) {
        if (userId == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }

        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        WalletBalanceVO vo = new WalletBalanceVO();
        vo.setUserId(userId);
        vo.setBalance(user.getWallet());

        // 可选：统计累计充值和消费（查流水表汇总，数据量大时建议单独字段存储，这里简单查）
        // 暂时只返回当前余额，统计功能可后续扩展
        vo.setTotalRecharge(BigDecimal.ZERO);
        vo.setTotalSpend(BigDecimal.ZERO);   

        return vo;
    }

    @Override
    public List<WalletLogVO> getWalletLogs(Integer userId) {
        if (userId == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }

        List<WalletLog> logs = walletLogMapper.findByUserId(userId);
        return convertToVOList(logs);
    }

    @Override
    public List<WalletLogVO> getWalletLogsByType(Integer userId, Integer type) {
        if (userId == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }

        List<WalletLog> logs = walletLogMapper.findByUserIdAndType(userId, type);
        return convertToVOList(logs);
    }

    // 内部转换方法
    private List<WalletLogVO> convertToVOList(List<WalletLog> logs) {
        List<WalletLogVO> voList = new ArrayList<>();
        for (WalletLog log : logs) {
            WalletLogVO vo = new WalletLogVO();
            vo.setLogId(log.getId());
            vo.setAmount(log.getAmount());
            vo.setBalanceBefore(log.getBalanceBefore());
            vo.setBalanceAfter(log.getBalanceAfter());
            vo.setType(log.getType());
            vo.setRelatedId(log.getRelatedId());
            vo.setCreateTime(log.getCreateTime());

            // type 转中文
            switch (log.getType()) {
                case 1: vo.setTypeName("充值"); break;
                case 2: vo.setTypeName("购买"); break;
                case 3: vo.setTypeName("收入"); break;
                default: vo.setTypeName("其他");
            }

            voList.add(vo);
        }
        return voList;
    }
}