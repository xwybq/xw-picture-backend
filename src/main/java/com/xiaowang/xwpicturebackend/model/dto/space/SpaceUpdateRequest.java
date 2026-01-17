package com.xiaowang.xwpicturebackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间更新请求
 *
 * @author xiaowang
 */
@Data
public class SpaceUpdateRequest implements Serializable {


    private static final long serialVersionUID = -7371636109071764009L;
    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;
    /**
     * 空间等级
     */
    private Integer spaceLevel;
    /**
     * 最大空间大小
     */
    private Long maxSize;
    /**
     * 最大文件数量
     */
    private Long maxCount;
}
