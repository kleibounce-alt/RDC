package com.klei.goodfish.dto;

public class AdminDeleteDTO {
    private Integer goodId;
    private Integer commentId;

    public Integer getGoodId() { return goodId; }
    public void setGoodId(Integer goodId) { this.goodId = goodId; }
    public Integer getCommentId() { return commentId; }
    public void setCommentId(Integer commentId) { this.commentId = commentId; }
}