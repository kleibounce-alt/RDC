package com.klei.goodfish.service;

import com.klei.goodfish.dto.WalletPayDTO;
import com.klei.goodfish.dto.WalletRechargeDTO;
import com.klei.goodfish.vo.WalletBalanceVO;
import com.klei.goodfish.vo.WalletLogVO;
import java.util.List;

/**
 * @author klei
 */
public interface WalletService {

    // 充值（直接加余额，记录流水）
    boolean recharge(WalletRechargeDTO dto);

    // 支付/扣款（检查余额，扣款，记录流水）
    boolean pay(WalletPayDTO dto);

    // 查询余额
    WalletBalanceVO getBalance(Integer userId);

    // 查询流水记录
    List<WalletLogVO> getWalletLogs(Integer userId);

    // 查询特定类型流水（1充值 2购买 3收入）
    List<WalletLogVO> getWalletLogsByType(Integer userId, Integer type);
}