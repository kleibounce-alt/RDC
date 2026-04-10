package com.klei.goodfish.service.impl;

import com.klei.goodfish.dto.GoodTagDTO;
import com.klei.goodfish.dto.TagCreateDTO;
import com.klei.goodfish.entity.Good;
import com.klei.goodfish.entity.GoodTag;
import com.klei.goodfish.entity.Tag;
import com.klei.goodfish.entity.User;
import com.klei.goodfish.mapper.GoodMapper;
import com.klei.goodfish.mapper.GoodTagMapper;
import com.klei.goodfish.mapper.TagMapper;
import com.klei.goodfish.mapper.UserMapper;
import com.klei.goodfish.mappercore.proxy.MapperProxy;
import com.klei.goodfish.service.TagService;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.vo.GoodTagVO;
import com.klei.goodfish.vo.TagVO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author klei
 */
public class TagServiceImpl implements TagService {

    private TagMapper tagMapper = MapperProxy.getMapper(TagMapper.class);
    private GoodTagMapper goodTagMapper = MapperProxy.getMapper(GoodTagMapper.class);
    private GoodMapper goodMapper = MapperProxy.getMapper(GoodMapper.class);
    private UserMapper userMapper = MapperProxy.getMapper(UserMapper.class);

    @Override
    public TagVO createTag(TagCreateDTO dto) {
        if (dto.getTagName() == null || dto.getTagName().trim().isEmpty()) {
            throw new BusinessException(400, "标签名称不能为空");
        }

        // 权限校验：只有管理员(role=1)或商家(role=2)能创建标签
        User user = userMapper.findById(dto.getUserId());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (user.getRole() != 1 && user.getRole() != 2) {
            throw new BusinessException(403, "无权创建标签");
        }

        // 查重：标签名唯一
        Tag exist = tagMapper.findByName(dto.getTagName().trim());
        if (exist != null) {
            throw new BusinessException(400, "标签已存在");
        }

        // 创建标签
        tagMapper.insert(dto.getTagName().trim());

        // 查询返回（根据名称查ID）
        Tag newTag = tagMapper.findByName(dto.getTagName().trim());
        if (newTag == null) {
            throw new BusinessException(500, "创建失败");
        }

        TagVO vo = new TagVO();
        vo.setTagId(newTag.getId());
        vo.setTagName(newTag.getTagName());
        return vo;
    }

    @Override
    public boolean addTagToGood(GoodTagDTO dto) {
        if (dto.getGoodId() == null || dto.getTagId() == null || dto.getUserId() == null) {
            throw new BusinessException(400, "参数不能为空");
        }

        // 校验商品是否存在且未删除
        Good good = goodMapper.findById(dto.getGoodId());
        if (good == null || good.getStatus() == 0) {
            throw new BusinessException(404, "商品不存在或已下架");
        }

        // 校验标签是否存在
        Tag tag = tagMapper.findById(dto.getTagId());
        if (tag == null) {
            throw new BusinessException(404, "标签不存在");
        }

        // 权限校验：只能给自家商品打标签，或管理员
        User operator = userMapper.findById(dto.getUserId());
        if (operator == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!good.getSellerId().equals(dto.getUserId()) && operator.getRole() != 1) {
            throw new BusinessException(403, "无权给其他用户商品打标签");
        }

        // 查重：不能重复打标签
        GoodTag exist = goodTagMapper.findByBothId(dto.getGoodId(), dto.getTagId());
        if (exist != null) {
            throw new BusinessException(400, "该商品已拥有此标签");
        }

        // 插入关联记录
        goodTagMapper.insert(dto.getGoodId(), dto.getTagId());
        return true;
    }

    @Override
    public boolean removeTagFromGood(GoodTagDTO dto) {
        if (dto.getGoodId() == null || dto.getTagId() == null || dto.getUserId() == null) {
            throw new BusinessException(400, "参数不能为空");
        }

        // 校验商品
        Good good = goodMapper.findById(dto.getGoodId());
        if (good == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 权限校验
        User operator = userMapper.findById(dto.getUserId());
        if (operator == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!good.getSellerId().equals(dto.getUserId()) && operator.getRole() != 1) {
            throw new BusinessException(403, "无权操作");
        }

        // 检查是否存在关联
        GoodTag exist = goodTagMapper.findByBothId(dto.getGoodId(), dto.getTagId());
        if (exist == null) {
            throw new BusinessException(400, "该商品未拥有此标签");
        }

        // 删除关联
        goodTagMapper.delete(dto.getGoodId(), dto.getTagId());
        return true;
    }

    @Override
    public List<TagVO> getAllTags() {
        List<Tag> tags = tagMapper.findAll();
        List<TagVO> voList = new ArrayList<>();
        for (Tag tag : tags) {
            TagVO vo = new TagVO();
            vo.setTagId(tag.getId());
            vo.setTagName(tag.getTagName());
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public List<GoodTagVO> getTagsByGoodId(Integer goodId) {
        if (goodId == null) {
            throw new BusinessException(400, "商品ID不能为空");
        }

        List<GoodTag> list = goodTagMapper.findByGoodId(goodId);
        List<GoodTagVO> voList = new ArrayList<>();

        for (GoodTag gt : list) {
            Tag tag = tagMapper.findById(gt.getTagId());
            if (tag != null) {
                GoodTagVO vo = new GoodTagVO();
                vo.setId(gt.getId());
                vo.setGoodId(gt.getGoodId());
                vo.setTagId(gt.getTagId());
                vo.setTagName(tag.getTagName());
                voList.add(vo);
            }
        }
        return voList;
    }
}