package com.xiaowang.xwpicturebackend.model.dto.picture;

import com.xiaowang.xwpicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
public class PictureQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 4718741591046740693L;
    /**
     * id
     */
    private Long id;


    /**
     * 用户id
     */
    private Long userId;

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

    /**
     * 图片大小 (字节)
     */
    private Long picSize;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片缩放比例
     */
    private Double picScale;

    /**
     * 图片格式 (jpg/png等)
     */
    private String picFormat;


    /**
     * 搜索文本
     */
    private String searchText;

}
