package com.xiaowang.xwpicturebackend.model.dto.space;

import com.xiaowang.xwpicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 空间查询请求
 *
 * @author xiaowang
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceQueryRequest extends PageRequest implements Serializable {


    private static final long serialVersionUID = 1900683094472181854L;
    /**
     * 空间id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;
    /**
     * 空间名称
     */
    private String spaceName;
    /**
     * 空间等级
     */
    private Integer spaceLevel;
}
