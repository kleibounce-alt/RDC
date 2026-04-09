package com.klei.goodfish.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author klei
 */
@Data
public class Order {
    private Integer id;
    private Integer goodId;
    private Integer buyerId;
    private Integer sellerId;
    private BigDecimal price;
    // 0待确认 1已完成 2已取消
    private Integer status;
    private LocalDateTime createTime;
}