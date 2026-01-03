package com.xiaowang.xwpicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 图片
 *
 * @TableName picture
 */
@TableName(value = "picture")
@Data
public class Picture implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1112717306047708470L;
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 图片url
     */
    private String url;

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
    private String tag;

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
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;


}