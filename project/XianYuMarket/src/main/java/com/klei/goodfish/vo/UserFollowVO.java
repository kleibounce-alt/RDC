package com.klei.goodfish.vo;

import com.klei.goodfish.dto.UserFollowDTO;
import lombok.Data;

import java.util.List;

@Data
public class UserFollowVO {
    private Integer userId;
    private List<UserFollowDTO> followings;
}
