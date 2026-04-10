package com.klei.goodfish.mapper;

import com.klei.goodfish.entity.WalletLog;
import com.klei.goodfish.mappercore.Insert;
import com.klei.goodfish.mappercore.Select;

import java.math.BigDecimal;
import java.util.List;

public interface WalletLogMapper {

    // 插入流水记录
    @Insert("INSERT INTO wallet_log(user_id, amount, balance_before, balance_after, type, related_id, create_time) " +
            "VALUES(?, ?, ?, ?, ?, ?, NOW())")
    void insert(Integer userId, BigDecimal amount, BigDecimal balanceBefore,
                BigDecimal balanceAfter, Integer type, Integer relatedId);

    // 查询用户的所有流水
    @Select("SELECT * FROM wallet_log WHERE user_id = ? ORDER BY create_time DESC")
    List<WalletLog> findByUserId(Integer userId);

    // 查询特定类型的流水
    @Select("SELECT * FROM wallet_log WHERE user_id = ? AND type = ? ORDER BY create_time DESC")
    List<WalletLog> findByUserIdAndType(Integer userId, Integer type);
}