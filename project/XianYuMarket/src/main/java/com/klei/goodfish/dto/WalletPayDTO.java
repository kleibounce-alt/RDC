package com.klei.goodfish.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * @author klei
 */
@Data
public class WalletPayDTO {
    private Integer userId;
    private BigDecimal amount;
    private Integer relatedId;
    //评论
    private String remark;
}