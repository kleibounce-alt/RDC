package com.klei.goodfish.vo;

import com.klei.goodfish.entity.Follow;
import com.klei.goodfish.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserFollowVO {
    private User followingUser;
    private LocalDateTime followTime;
}
