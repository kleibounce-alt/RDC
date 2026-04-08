package com.klei.goodfish.dto;

import com.klei.goodfish.entity.Favorite;
import com.klei.goodfish.entity.Good;
import com.klei.goodfish.vo.UserFavoriteVO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author klei
 */
@Data
public class UserFavoriteDTO {
    private Integer userId;
    private List<UserFavoriteVO> favorites;
}
