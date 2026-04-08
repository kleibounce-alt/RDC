package com.klei.goodfish.mapper;

import com.klei.goodfish.entity.Favorite;
import com.klei.goodfish.mappercore.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author klei
 */
public interface FavoriteMapper {
    @Select("SELECT * FROM favorite WHERE user_id = ?" )
    List<Favorite> findByUserId(Integer userId);

}
