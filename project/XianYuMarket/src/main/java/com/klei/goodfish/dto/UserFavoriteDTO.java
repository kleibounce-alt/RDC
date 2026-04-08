package com.klei.goodfish.dto;

import com.klei.goodfish.entity.Good;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserFavoriteDTO {
    private Good good;
    private LocalDateTime favoriteTime;
}
