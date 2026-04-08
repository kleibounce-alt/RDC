package com.klei.goodfish.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author klei
 */
@Data
public class UserProfileDTO {
    private Integer userId;
    private String userName;
    private Integer role;
    private Integer status;
    private BigDecimal wallet;
    private LocalDateTime createTime;
}
