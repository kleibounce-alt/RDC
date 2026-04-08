package com.klei.goodfish.mapper;
import com.klei.goodfish.mappercore.Select;
import com.klei.goodfish.entity.User;

public interface UserMapper {

    @Select("SELECT * FROM user WHERE id = ?")
    User findById(int id);

}
