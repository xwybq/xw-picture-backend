package com.xiaowang.xwpicturebackend.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class PictureTagCategory {
    /**
     * 标签列表
     */
    private List<String > tagList;
    /**
     * 类别列表
     */
    private List<String > categoryList;
}
