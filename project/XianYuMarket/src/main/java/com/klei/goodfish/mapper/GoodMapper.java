package com.klei.goodfish.mapper;

import com.klei.goodfish.entity.Good;
import com.klei.goodfish.mappercore.Insert;
import com.klei.goodfish.mappercore.Select;
import com.klei.goodfish.mappercore.Update;
import com.klei.goodfish.mappercore.Delete;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author klei
 */
public interface GoodMapper {

    @Select("SELECT * FROM good WHERE id = ?")
    Good findById(Integer id);

    @Select("SELECT * FROM good WHERE seller_id = ? AND status = 1")
    List<Good> findBySellerId(Integer sellerId);

    // 发布商品
    @Insert("INSERT INTO good(good_name, image, price, description, selling_status, seller_id, status, create_time) " +
            "VALUES(?, ?, ?, ?, '未出售', ?, 1, NOW())")
    void insert(String goodName, String image, BigDecimal price, String description, Integer sellerId);

    // 编辑商品
    @Update("UPDATE good SET good_name = ?, image = ?, price = ?, description = ? WHERE id = ?")
    void update(String goodName, String image, BigDecimal price, String description, Integer id);

    // 删除商品
    @Update("UPDATE good SET status = 0 WHERE id = ?")
    void deleteById(Integer id);

    // 搜索商品（模糊查询，传入时要包百分号，如："%手机%"）
    @Select("SELECT * FROM good WHERE good_name LIKE ? AND status = 1")
    List<Good> searchByName(String keyword);

    // 随机推荐
    @Select("SELECT * FROM good WHERE selling_status = '未出售' AND status = 1 ORDER BY RAND() LIMIT ?")
    List<Good> findRandom(Integer limit);

    // 根据标签ID查商品
    @Select("SELECT g.* FROM good g JOIN good_tag gt ON g.id = gt.good_id WHERE gt.tag_id = ? AND g.status = 1")
    List<Good> findByTagId(Integer tagId);

    // 更新销售状态
    @Update("UPDATE good SET selling_status = ? WHERE id = ?")
    void updateSellingStatus(String sellingStatus, Integer id);
}