package com.klei.goodfish.dto;

import lombok.Data;

/**
 * @author klei
 */
@Data
public class UserRegisterDTO {
    private String userName;
    private String password;
    private String confirmPassword;
    private Integer role;

    public String validate() {
        if (userName == null || userName.length() < 3) {
            return "用户名至少3位";
        }
        if (password == null || password.length() < 6) {
            return "密码至少6位";
        }
        if (!password.equals(confirmPassword)) {
            return "两次密码不一致";
        }
        return null;
    }
}
