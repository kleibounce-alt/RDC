package com.klei.goodfish.vo;

import com.klei.goodfish.entity.User;
import lombok.Data;

/**
 * @author klei
 */
@Data
public class UserLoginVO {
    private Integer userId;
    private String userName;
    private Integer role;
    private boolean success;
    private String avatar;
    private String message;

    public static UserLoginVO success(User user) {
        UserLoginVO vo = new UserLoginVO();
        vo.success = true;
        vo.userId = user.getId();
        vo.userName = user.getUserName();
        vo.message = "登录成功";
        vo.avatar = user.getAvatar();
        vo.role = user.getRole();
        return vo;
    }

    public static UserLoginVO fail(String msg) {
        UserLoginVO vo = new UserLoginVO();
        vo.success = false;
        vo.message = msg;
        return vo;
    }
}
