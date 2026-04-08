package com.klei.goodfish.mapper;

import com.klei.goodfish.entity.Follow;
import com.klei.goodfish.mappercore.Select;

import java.util.List;

/**
 * @author klei
 */
public interface FollowMapper {
    @Select("SELECT * FROM follow WHERE user_id = ?")
    List<Follow> findByUserId(Integer userId);
}
