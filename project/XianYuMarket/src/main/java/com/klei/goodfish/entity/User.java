package com.klei.goodfish.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author klei
 */
@Data
public class User {
    private Integer id;
    private String userName;
    private String password;
    private String avatar;
    private Integer role;
    private Integer status;
    private BigDecimal wallet;
    private LocalDateTime createTime;
}
