package com.klei.goodfish.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GoodPublishDTO {
    private String goodName;
    private String image;
    private BigDecimal price;
    private String description;
    private Integer sellerId;
    // 强制要求标签，至少选择一个
    private List<Integer> tagIds;
}