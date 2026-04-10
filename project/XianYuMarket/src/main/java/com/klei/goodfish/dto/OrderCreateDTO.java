package com.klei.goodfish.dto;

import lombok.Data;

/**
 * @author klei
 */
@Data
public class OrderCreateDTO {
    private Integer buyerId;
    private Integer goodId;
}