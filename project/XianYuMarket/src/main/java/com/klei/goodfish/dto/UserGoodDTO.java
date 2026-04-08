package com.klei.goodfish.dto;

import com.klei.goodfish.entity.User;
import com.klei.goodfish.vo.UserFavoriteVO;
import com.klei.goodfish.vo.UserGoodVO;
import lombok.Data;

import java.util.List;

@Data
public class UserGoodDTO {
    private Integer userId;
    private List<UserGoodVO> goods;
}
