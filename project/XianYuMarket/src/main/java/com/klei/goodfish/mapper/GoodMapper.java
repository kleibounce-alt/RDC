package com.klei.goodfish.mapper;

import com.klei.goodfish.entity.Good;
import com.klei.goodfish.mappercore.Select;

import java.util.List;

public interface GoodMapper {
    @Select("SELECT * FROM good WHERE id = ?")
    Good findById(Integer id);

    @Select("SELECT * FROM good WHERE seller_id = ?")
    List<Good> findBySellerId(Integer sellerId);
}
