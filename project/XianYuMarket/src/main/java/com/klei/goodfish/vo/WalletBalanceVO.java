package com.klei.goodfish.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class WalletBalanceVO {
    private Integer userId;
    private String userName;
    private BigDecimal balance;
    private BigDecimal totalRecharge;
    private BigDecimal totalSpend;
}