package com.xiaowang.xwpicturebackend.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xiaowang.xwpicturebackend.model.entity.Picture;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class PictureVO implements Serializable {

    private static final long serialVersionUID = 3056690630896913387L;
    /**
     * id
     */
    private Long id;


    /**
     * 用户id
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
     * * 创建用户信息
     */
    private UserVO user;


    /**
     * 将VO转换为实体类
     *
     * @param pictureVO 要转换的VO
     * @return 转换后的实体类
     */
    public static Picture voToObj(PictureVO pictureVO) {
        if (pictureVO == null) {
            return null;
        }
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureVO, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureVO.getTags())); // 将标签列表转换为JSON字符串
        return picture;
    }

    /**
     * 将实体类转换为VO
     *
     * @param picture 实体类
     * @return 转换后的VO
     */
    public static PictureVO objToVo(Picture picture) {
        if (picture == null) {
            return null;
        }
        PictureVO pictureVO = new PictureVO();
        BeanUtil.copyProperties(picture, pictureVO);
        pictureVO.setTags(JSONUtil.toList(picture.getTags(), String.class)); // 将JSON字符串转换为标签列表
        return pictureVO;
    }

}
