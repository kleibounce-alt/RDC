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

    // 关键修复：使用别名 price as goodPrice，确保映射到实体类
    @Select("SELECT id, good_name as goodName, image as goodImage, " +
            "price as goodPrice, description, selling_status as sellingStatus, " +
            "seller_id as sellerId, status, create_time as createTime " +
            "FROM good WHERE id = ?")
    Good findById(Integer id);

    @Select("SELECT id, good_name as goodName, image as goodImage, " +
            "price as goodPrice, description, selling_status as sellingStatus, " +
            "seller_id as sellerId, status, create_time as createTime " +
            "FROM good WHERE seller_id = ? AND status = 1")
    List<Good> findBySellerId(Integer sellerId);

    // 发布商品（插入没问题，字段名就是对应数据库列）
    @Insert("INSERT INTO good(good_name, image, price, description, selling_status, seller_id, status, create_time) " +
            "VALUES(?, ?, ?, ?, '未出售', ?, 1, NOW())")
    void insert(String goodName, String image, BigDecimal price, String description, Integer sellerId);

    // 编辑商品（更新没问题）
    @Update("UPDATE good SET good_name = ?, image = ?, price = ?, description = ? WHERE id = ?")
    void update(String goodName, String image, BigDecimal price, String description, Integer id);

    // 删除商品（逻辑删除）
    @Update("UPDATE good SET status = 0 WHERE id = ?")
    void deleteById(Integer id);

    // 搜索商品（模糊查询）- 关键修复
    @Select("SELECT id, good_name as goodName, image as goodImage, " +
            "price as goodPrice, description, selling_status as sellingStatus, " +
            "seller_id as sellerId, status, create_time as createTime " +
            "FROM good WHERE good_name LIKE ? AND status = 1")
    List<Good> searchByName(String keyword);

    // 随机推荐（关键修复）
    @Select("SELECT id, good_name as goodName, image as goodImage, " +
            "price as goodPrice, description, selling_status as sellingStatus, " +
            "seller_id as sellerId, status, create_time as createTime " +
            "FROM good WHERE selling_status = '未出售' AND status = 1 ORDER BY RAND() LIMIT ?")
    List<Good> findRandom(Integer limit);

    // 根据标签ID查商品（关键修复）
    @Select("SELECT g.id, g.good_name as goodName, g.image as goodImage, " +
            "g.price as goodPrice, g.description, g.selling_status as sellingStatus, " +
            "g.seller_id as sellerId, g.status, g.create_time as createTime " +
            "FROM good g JOIN good_tag gt ON g.id = gt.good_id WHERE gt.tag_id = ? AND g.status = 1")
    List<Good> findByTagId(Integer tagId);

    // 更新销售状态
    @Update("UPDATE good SET selling_status = ? WHERE id = ?")
    void updateSellingStatus(String sellingStatus, Integer id);
}