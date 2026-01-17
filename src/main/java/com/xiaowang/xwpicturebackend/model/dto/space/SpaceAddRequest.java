package com.xiaowang.xwpicturebackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间创建请求
 *
 * @author xiaowang
 */
@Data
public class SpaceAddRequest implements Serializable {


    private static final long serialVersionUID = 6217547395532647319L;
    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间等级
     */
    private Integer spaceLevel;
}
