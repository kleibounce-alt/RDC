package com.klei.goodfish.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GoodPublishDTO {
    private String goodName;
    private String image;
    private BigDecimal price;
    private String description;
    private Integer sellerId;
}