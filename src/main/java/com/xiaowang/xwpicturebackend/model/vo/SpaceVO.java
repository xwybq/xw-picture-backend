package com.xiaowang.xwpicturebackend.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.xiaowang.xwpicturebackend.model.entity.Picture;
import com.xiaowang.xwpicturebackend.model.entity.Space;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 空间视图
 *
 * @author xiaowang
 */
@Data
public class SpaceVO implements Serializable {

    private static final long serialVersionUID = 5654399951332553005L;
    /**
     * id
     */
    private Long id;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间等级: 0-普通空间, 1-专业空间 2-旗舰空间
     */
    private Integer spaceLevel;

    /**
     * 空间图片的最大大小 (字节)
     */
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    private Long maxCount;

    /**
     * 当前空间图片的总大小 (字节)
     */
    private Long totalSize;

    /**
     * 当前空间图片的总数量
     */
    private Long totalCount;

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
     * 用户信息
     */
    private UserVO user;

    /**
     * 将VO转换为实体类
     *
     * @param spaceVO 要转换的VO
     * @return 转换后的实体类
     */
    public static Space voToObj(SpaceVO spaceVO) {
        if (spaceVO == null) {
            return null;
        }
        Space space = new Space();
        BeanUtil.copyProperties(spaceVO, space);
        return space;
    }

    /**
     * 将实体类转换为VO
     *
     * @param space 实体类
     * @return 转换后的VO
     */
    public static SpaceVO objToVo(Space space) {
        if (space == null) {
            return null;
        }
        SpaceVO spaceVO = new SpaceVO();
        BeanUtil.copyProperties(space, spaceVO);
        return spaceVO;
    }

}
