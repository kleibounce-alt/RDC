package com.klei.goodfish.vo;

import lombok.Data;

@Data
public class GoodTagVO {
    private Integer id;
    private Integer goodId;
    private Integer tagId;
    private String tagName;
}