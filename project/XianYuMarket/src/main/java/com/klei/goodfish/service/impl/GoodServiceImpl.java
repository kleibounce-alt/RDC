package com.klei.goodfish.service.impl;

import com.klei.goodfish.dto.GoodPublishDTO;
import com.klei.goodfish.dto.GoodSearchDTO;
import com.klei.goodfish.dto.GoodUpdateDTO;
import com.klei.goodfish.entity.Good;
import com.klei.goodfish.entity.GoodTag;
import com.klei.goodfish.entity.Tag;
import com.klei.goodfish.entity.User;
import com.klei.goodfish.mapper.GoodMapper;
import com.klei.goodfish.mapper.GoodTagMapper;
import com.klei.goodfish.mapper.TagMapper;
import com.klei.goodfish.mapper.UserMapper;
import com.klei.goodfish.mappercore.proxy.MapperProxy;
import com.klei.goodfish.service.GoodService;
import com.klei.goodfish.util.BusinessException;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author klei
 */
public class GoodServiceImpl implements GoodService {

    private GoodMapper goodMapper = MapperProxy.getMapper(GoodMapper.class);
    private UserMapper userMapper = MapperProxy.getMapper(UserMapper.class);
    private TagMapper tagMapper = MapperProxy.getMapper(TagMapper.class);
    private GoodTagMapper goodTagMapper = MapperProxy.getMapper(GoodTagMapper.class);

    @Override
    public Good publishGood(GoodPublishDTO dto) {
        // 参数校验
        if (dto.getGoodName() == null || dto.getGoodName().trim().isEmpty()) {
            throw new BusinessException(400, "商品名称不能为空");
        }
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "商品价格必须大于0");
        }
        if (dto.getSellerId() == null) {
            throw new BusinessException(400, "卖家ID不能为空");
        }

        // ★★★ 强制要求选择标签 ★★★
        if (dto.getTagIds() == null || dto.getTagIds().isEmpty()) {
            throw new BusinessException(400, "请至少选择一个商品标签");
        }

        // 校验标签数量不超过3个
        if (dto.getTagIds().size() > 3) {
            throw new BusinessException(400, "最多只能选择3个标签");
        }

        // 校验卖家是否存在
        User seller = userMapper.findById(dto.getSellerId());
        if (seller == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 校验标签是否都存在
        for (Integer tagId : dto.getTagIds()) {
            Tag tag = tagMapper.findById(tagId);
            if (tag == null) {
                throw new BusinessException(404, "标签ID " + tagId + " 不存在");
            }
        }

        // 插入商品
        goodMapper.insert(
                dto.getGoodName(),
                dto.getImage(),
                dto.getPrice(),
                dto.getDescription(),
                dto.getSellerId()
        );

        // 查询刚插入的商品
        List<Good> goods = goodMapper.findBySellerId(dto.getSellerId());
        Good newGood = null;
        // 倒序查找最新的（根据ID最大或时间最新）
        for (int i = goods.size() - 1; i >= 0; i--) {
            if (goods.get(i).getGoodName().equals(dto.getGoodName())) {
                newGood = goods.get(i);
                break;
            }
        }

        if (newGood == null) {
            throw new BusinessException(500, "发布失败，数据写入异常");
        }

        // ★★★ 关联商品和标签 ★★★
        try {
            for (Integer tagId : dto.getTagIds()) {
                // 检查是否已关联（理论上新商品不会重复，但保险起见）
                GoodTag exist = goodTagMapper.findByBothId(newGood.getId(), tagId);
                if (exist == null) {
                    goodTagMapper.insert(newGood.getId(), tagId);
                }
            }
        } catch (Exception e) {
            // 标签关联失败不影响商品已发布，但记录日志
            System.err.println("[GoodService] 商品标签关联失败: " + e.getMessage());
            // 这里可以选择抛出异常回滚，或者仅记录日志
            // throw new BusinessException(500, "标签关联失败");
        }

        return newGood;
    }

    @Override
    public Good updateGood(GoodUpdateDTO dto) {
        if (dto.getGoodId() == null) {
            throw new BusinessException(400, "商品ID不能为空");
        }
        if (dto.getUserId() == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }

        // 查询商品是否存在
        Good good = goodMapper.findById(dto.getGoodId());
        if (good == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 校验商品状态
        if (good.getStatus() == 0) {
            throw new BusinessException(400, "商品已删除，无法编辑");
        }

        // 校验权限：只能编辑自己的商品（管理员除外，管理员role=1）
        User operator = userMapper.findById(dto.getUserId());
        if (operator == null) {
            throw new BusinessException(404, "操作者不存在");
        }
        if (!good.getSellerId().equals(dto.getUserId()) && operator.getRole() != 1) {
            throw new BusinessException(403, "无权编辑他人商品");
        }

        // 执行更新
        goodMapper.update(
                dto.getGoodName(),
                dto.getImage(),
                dto.getPrice(),
                dto.getDescription(),
                dto.getGoodId()
        );

        // 返回更新后的数据
        return goodMapper.findById(dto.getGoodId());
    }

    @Override
    public boolean deleteGood(Integer goodId, Integer userId) {
        if (goodId == null || userId == null) {
            throw new BusinessException(400, "参数不能为空");
        }

        Good good = goodMapper.findById(goodId);
        if (good == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 已删除的无需重复删除
        if (good.getStatus() == 0) {
            return true;
        }

        // 校验权限
        User operator = userMapper.findById(userId);
        if (operator == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!good.getSellerId().equals(userId) && operator.getRole() != 1) {
            throw new BusinessException(403, "无权删除他人商品");
        }

        // 逻辑删除（改status=0）
        goodMapper.deleteById(goodId);
        return true;
    }

    @Override
    public Good getGoodDetail(Integer goodId) {
        if (goodId == null) {
            throw new BusinessException(400, "商品ID不能为空");
        }

        Good good = goodMapper.findById(goodId);
        if (good == null || good.getStatus() == 0) {
            throw new BusinessException(404, "商品不存在或已下架");
        }

        return good;
    }

    @Override
    public List<Good> getRandomGoods(Integer limit) {
        // 默认推荐8个
        if (limit == null || limit <= 0) {
            limit = 8;
        }
        // 最多推荐20个，防止limit过大
        if (limit > 20) {
            limit = 20;
        }

        return goodMapper.findRandom(limit);
    }

    @Override
    public List<Good> searchGoods(GoodSearchDTO dto) {
        if (dto == null || dto.getKeyword() == null || dto.getKeyword().trim().isEmpty()) {
            throw new BusinessException(400, "搜索关键词不能为空");
        }

        // 模糊查询
        String keyword = "%" + dto.getKeyword().trim() + "%";
        return goodMapper.searchByName(keyword);
    }

    @Override
    public List<Good> getGoodsByTag(Integer tagId) {
        if (tagId == null) {
            throw new BusinessException(400, "标签ID不能为空");
        }
        return goodMapper.findByTagId(tagId);
    }

    @Override
    public boolean updateSellingStatus(Integer goodId, String status, Integer userId) {
        if (goodId == null || status == null || userId == null) {
            throw new BusinessException(400, "参数不能为空");
        }

        // 校验状态值
        if (!"未出售".equals(status) && !"已出售".equals(status)) {
            throw new BusinessException(400, "状态只能是'未出售'或'已出售'");
        }

        Good good = goodMapper.findById(goodId);
        if (good == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 校验权限
        User operator = userMapper.findById(userId);
        if (operator == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!good.getSellerId().equals(userId) && operator.getRole() != 1) {
            throw new BusinessException(403, "无权操作他人商品");
        }

        goodMapper.updateSellingStatus(status, goodId);
        return true;
    }

    @Override
    public List<Good> getGoodsBySellerId(Integer sellerId) {
        if (sellerId == null) {
            throw new BusinessException(400, "卖家ID不能为空");
        }

        // 查询该卖家的所有商品
        return goodMapper.findBySellerId(sellerId);
    }

    @Override
    public boolean adminDeleteGood(Integer goodId) {
        if (goodId == null) {
            throw new BusinessException(400, "商品ID不能为空");
        }

        Good good = goodMapper.findById(goodId);
        if (good == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 已删除的无需重复删除
        if (good.getStatus() == 0) {
            return true;
        }

        // 直接逻辑删除（管理员无需再校验权限，直接删除）
        goodMapper.deleteById(goodId);
        return true;
    }
}