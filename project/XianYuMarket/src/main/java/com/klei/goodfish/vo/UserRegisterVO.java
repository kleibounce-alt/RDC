package com.klei.goodfish.vo;

import com.klei.goodfish.entity.User;
import lombok.Data;

/**
 * @author klei
 */
@Data
public class UserRegisterVO {
    private boolean success;
    private Integer userId;
    private String message;
    private String userName;
    private String avatar;
    private Integer role;

    public static UserRegisterVO success(User user) {
        UserRegisterVO vo = new UserRegisterVO();
        vo.success = true;
        vo.userId = user.getId();
        vo.userName = user.getUserName();
        vo.message = "注册成功";
        vo.avatar = user.getAvatar();
        vo.role = user.getRole();
        return vo;
    }

    public static UserRegisterVO fail(String msg) {
        UserRegisterVO vo = new UserRegisterVO();
        vo.success = false;
        vo.message = msg;
        return vo;
    }
}
