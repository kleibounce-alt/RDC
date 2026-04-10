package com.klei.goodfish.mapper;
import com.klei.goodfish.mappercore.Insert;
import com.klei.goodfish.mappercore.Select;
import com.klei.goodfish.entity.User;
import com.klei.goodfish.mappercore.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author klei
 */
public interface UserMapper {

    @Select("SELECT * FROM user WHERE id = ?")
    User findById(int id);

    @Select("SELECT * FROM user WHERE user_name = ?")
    User findByName(String name);

    @Insert("INSERT INTO user(user_name, password, role, status, wallet, create_time, avatar) " +
            "VALUES(?, ?, ?, ?, ?, ?, ?)")
    void insert(String userName, String password, int role, int status,
                BigDecimal wallet, LocalDateTime createTime, String avatar);

    @Update("UPDATE user SET wallet = ? WHERE id = ?")
    void updateWallet(BigDecimal newBalance, Integer userId);

}

