package com.xiaowang.xwpicturebackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间编辑请求(用户操作)
 *
 * @author xiaowang
 */
@Data
public class SpaceEditRequest implements Serializable {


    private static final long serialVersionUID = -7807287017160652913L;
    /**
     * 空间id
     */
    private Long id;
    /**
     * 空间名称
     */
    private String spaceName;
}
