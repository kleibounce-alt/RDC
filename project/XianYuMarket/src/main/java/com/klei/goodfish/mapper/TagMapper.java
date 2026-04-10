package com.klei.goodfish.mapper;

import com.klei.goodfish.entity.Tag;
import com.klei.goodfish.mappercore.Insert;
import com.klei.goodfish.mappercore.Select;
import com.klei.goodfish.mappercore.Delete;
import java.util.List;

public interface TagMapper {

    // 创建标签
    @Insert("INSERT INTO tag(tag_name) VALUES(?)")
    void insert(String tagName);

    // 根据ID查标签
    @Select("SELECT * FROM tag WHERE id = ?")
    Tag findById(Integer id);

    // 根据名称查标签（查重用）
    @Select("SELECT * FROM tag WHERE tag_name = ?")
    Tag findByName(String tagName);

    // 查所有标签（前端展示用）
    @Select("SELECT * FROM tag")
    List<Tag> findAll();

    // 删除标签（物理删除，关联表有外键或级联删除需考虑，这里先简单处理）
    @Delete("DELETE FROM tag WHERE id = ?")
    void deleteById(Integer id);
}