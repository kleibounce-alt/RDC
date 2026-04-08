package com.klei.goodfish.vo;

import com.klei.goodfish.dto.UserFavoriteDTO;
import lombok.Data;

import java.util.List;

/**
 * @author klei
 */
@Data
public class UserFavoriteVO {
    private Integer userId;
    private List<UserFavoriteDTO> favorites;
}
