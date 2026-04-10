package com.klei.goodfish.mapper;

import com.klei.goodfish.entity.GoodTag;
import com.klei.goodfish.mappercore.Insert;
import com.klei.goodfish.mappercore.Select;
import com.klei.goodfish.mappercore.Delete;
import java.util.List;

public interface GoodTagMapper {

    // 给商品打标签（插入关联记录）
    @Insert("INSERT INTO good_tag(good_id, tag_id) VALUES(?, ?)")
    void insert(Integer goodId, Integer tagId);

    // 取消标签（删除关联记录）
    @Delete("DELETE FROM good_tag WHERE good_id = ? AND tag_id = ?")
    void delete(Integer goodId, Integer tagId);

    // 查商品的所有标签ID（关联表只存ID，查详情需要联查或再查Tag表）
    @Select("SELECT * FROM good_tag WHERE good_id = ?")
    List<GoodTag> findByGoodId(Integer goodId);

    // 查某个标签关联了多少商品（统计用，可选）
    @Select("SELECT COUNT(*) FROM good_tag WHERE tag_id = ?")
    int countByTagId(Integer tagId);

    // 查特定关联（判重用）
    @Select("SELECT * FROM good_tag WHERE good_id = ? AND tag_id = ?")
    GoodTag findByBothId(Integer goodId, Integer tagId);
}