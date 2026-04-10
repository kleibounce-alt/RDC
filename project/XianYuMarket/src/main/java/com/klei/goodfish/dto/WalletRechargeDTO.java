package com.klei.goodfish.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * @author klei
 */
@Data
public class WalletRechargeDTO {
    private Integer userId;
    private BigDecimal amount;
    //备注
    private String remark;
}