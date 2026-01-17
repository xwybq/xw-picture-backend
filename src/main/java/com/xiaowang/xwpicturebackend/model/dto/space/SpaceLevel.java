package com.xiaowang.xwpicturebackend.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class SpaceLevel {
    /**
     * 空间等级: 0-普通空间, 1-专业空间 2-旗舰空间
     */
    private int value;

    /**
     * 中文描述
     */
    private String text;
    /**
     * 空间图片的最大大小 (字节)
     */
    private long maxSize;
    /**
     * 空间图片的最大数量
     */
    private long maxCount;
}
