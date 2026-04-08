package com.klei.goodfish.dto;

import com.klei.goodfish.vo.UserFollowVO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserFollowDTO {
    private Integer userId;
    private List<UserFollowVO> followings;
}
