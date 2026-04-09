package com.klei.goodfish.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * @author klei
 */
@Data
public class GoodUpdateDTO {
    private Integer goodId;
    private String goodName;
    private String image;
    private BigDecimal price;
    private String description;
    private Integer userId;
}