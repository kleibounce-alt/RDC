package com.klei.goodfish.vo;

import com.klei.goodfish.entity.Good;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserGoodVO {
    private Good good;
    private LocalDateTime releaseTime;
}
