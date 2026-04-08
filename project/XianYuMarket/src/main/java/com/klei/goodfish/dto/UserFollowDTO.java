package com.klei.goodfish.dto;

import com.klei.goodfish.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserFollowDTO {
    private User followingUser;
    private LocalDateTime followTime;
}
