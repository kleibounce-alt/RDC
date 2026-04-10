package com.klei.goodfish.vo;

import com.klei.goodfish.entity.Good;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author klei
 */
@Data
public class OrderVO {
    private Integer orderId;
    private Integer goodId;
    private String goodName;
    private String goodImage;
    // 成交价格
    private BigDecimal price;

    private Integer buyerId;
    private String buyerName;

    private Integer sellerId;
    private String sellerName;

    // 0待确认 1已完成 2已取消
    private Integer status;
    private String statusName;

    private LocalDateTime createTime;
}