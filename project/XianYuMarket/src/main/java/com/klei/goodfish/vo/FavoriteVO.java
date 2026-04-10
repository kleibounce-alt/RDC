package com.klei.goodfish.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author klei
 */
@Data
public class FavoriteVO {
    private Integer favoriteId;
    private Integer goodId;
    private String goodName;
    private String image;
    private BigDecimal price;
    private LocalDateTime createTime;
}