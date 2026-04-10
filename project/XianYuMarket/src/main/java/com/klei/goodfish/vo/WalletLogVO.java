package com.klei.goodfish.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author klei
 */
@Data
public class WalletLogVO {
    private Integer logId;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    // 1充值 2购买 3收入（退款）
    private Integer type;
    // 中文描述（充值/购买/收入）
    private String typeName;
    private Integer relatedId;
    private String remark;
    private LocalDateTime createTime;
}