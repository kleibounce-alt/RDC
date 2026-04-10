package com.klei.goodfish.service;

import com.klei.goodfish.dto.GoodTagDTO;
import com.klei.goodfish.dto.TagCreateDTO;
import com.klei.goodfish.vo.GoodTagVO;
import com.klei.goodfish.vo.TagVO;
import java.util.List;

public interface TagService {

    // 创建标签（管理员或商家权限）
    TagVO createTag(TagCreateDTO dto);

    // 给商品打标签（只能给自家商品打）
    boolean addTagToGood(GoodTagDTO dto);

    // 取消商品标签
    boolean removeTagFromGood(GoodTagDTO dto);

    // 获取所有标签列表（前端展示）
    List<TagVO> getAllTags();

    // 获取商品的所有标签
    List<GoodTagVO> getTagsByGoodId(Integer goodId);
}