package com.klei.goodfish.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author klei
 */
@Data
public class WalletLog {
    private Integer id;
    private Integer userId;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    // 1充值 2购买 3收入
    private Integer type;
    private Integer relatedId;
    private LocalDateTime createTime;
}