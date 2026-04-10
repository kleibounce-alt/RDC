package com.klei.goodfish.dto;

import lombok.Data;

@Data
public class TagCreateDTO {
    private String tagName;
    private Integer userId;
}