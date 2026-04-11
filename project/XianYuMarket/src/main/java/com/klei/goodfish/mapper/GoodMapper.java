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

    // 使用别名映射到实体类字段
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

    // 发布商品
    @Insert("INSERT INTO good(good_name, image, price, description, selling_status, seller_id, status, create_time) " +
            "VALUES(?, ?, ?, ?, '未出售', ?, 1, NOW())")
    void insert(String goodName, String image, BigDecimal price, String description, Integer sellerId);

    // 编辑商品
    @Update("UPDATE good SET good_name = ?, image = ?, price = ?, description = ? WHERE id = ?")
    void update(String goodName, String image, BigDecimal price, String description, Integer id);

    // 删除商品（逻辑删除）
    @Update("UPDATE good SET status = 0 WHERE id = ?")
    void deleteById(Integer id);

    // 搜索商品（模糊查询）
    @Select("SELECT id, good_name as goodName, image as goodImage, " +
            "price as goodPrice, description, selling_status as sellingStatus, " +
            "seller_id as sellerId, status, create_time as createTime " +
            "FROM good WHERE good_name LIKE ? AND status = 1")
    List<Good> searchByName(String keyword);

    // 随机推荐（修复：使用"未出售" 替换乱码）
    @Select("SELECT id, good_name as goodName, image as goodImage, " +
            "price as goodPrice, description, selling_status as sellingStatus, " +
            "seller_id as sellerId, status, create_time as createTime " +
            "FROM good WHERE selling_status = '未出售' AND status = 1 ORDER BY RAND() LIMIT ?")
    List<Good> findRandom(Integer limit);

    // 根据标签ID查商品
    @Select("SELECT g.id, g.good_name as goodName, g.image as goodImage, " +
            "g.price as goodPrice, g.description, g.selling_status as sellingStatus, " +
            "g.seller_id as sellerId, g.status, g.create_time as createTime " +
            "FROM good g JOIN good_tag gt ON g.id = gt.good_id WHERE gt.tag_id = ? AND g.status = 1")
    List<Good> findByTagId(Integer tagId);

    // 更新销售状态（修复：使用"未出售"/"已出售" 替换乱码）
    @Update("UPDATE good SET selling_status = ? WHERE id = ?")
    void updateSellingStatus(String sellingStatus, Integer id);

    // ★★★ 关键新增：原子性更新商品为已出售（防止并发重复购买）★★★
    // 只有在 selling_status = '未出售' 时才更新，返回影响行数（1=成功，0=已被买走）
    @Update("UPDATE good SET selling_status = '已出售' WHERE id = ? AND selling_status = '未出售' AND status = 1")
    int markAsSold(Integer goodId);

    // ★★★ 关键新增：将商品状态恢复为未出售（用于扣款失败时回滚）★★★
    @Update("UPDATE good SET selling_status = '未出售' WHERE id = ? AND selling_status = '已出售'")
    int markAsAvailable(Integer goodId);
}