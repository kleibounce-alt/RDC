package com.klei.goodfish.vo;

import com.klei.goodfish.dto.UserGoodDTO;
import lombok.Data;

import java.util.List;

@Data
public class UserGoodVO {
    private Integer userId;
    private List<UserGoodDTO> goods;
}
