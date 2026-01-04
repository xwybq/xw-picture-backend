package com.xiaowang.xwpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class PictureUpdateRequest implements Serializable {
    private static final long serialVersionUID = -7231170286101138375L;
    /**
     * id
     */
    private Long id;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 图片介绍
     */
    private String introduction;

    /**
     * 图片分类
     */
    private String category;

    /**
     * 图片标签 (逗号分隔，格式为JSON数组)
     */
    private List<String> tags;
}
