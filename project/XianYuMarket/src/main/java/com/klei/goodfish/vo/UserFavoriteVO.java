package com.klei.goodfish.vo;

import com.klei.goodfish.entity.Favorite;
import com.klei.goodfish.entity.Good;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserFavoriteVO {
    private Good good;
    private LocalDateTime favoriteTime;
}
