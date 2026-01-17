package com.xiaowang.xwpicturebackend.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;


/**
 * 空间等级枚举
 */

@Getter
public enum SpaceLevelEnum {
    /**
     * 普通空间
     */
    COMMON("普通空间", 0, 1024 * 1024 * 100L, 100L),
    /**
     * 专业空间
     */
    PRO("专业空间", 1, 1024 * 1024 * 1000L, 1000L),
    /**
     * 旗舰空间
     */
    FLIGHT("旗舰空间", 2, 1024 * 1024 * 10000L, 10000L);


    private final String text;
    private final Integer value;
    private final Long maxSize;
    private final Long maxCount;

    /**
     * 空间等级枚举
     *
     * @param text     空间等级文本
     * @param value    空间等级值
     * @param maxSize  最大空间大小
     * @param maxCount 最大图片数量
     */
    SpaceLevelEnum(String text, int value, Long maxSize, Long maxCount) {
        this.text = text;
        this.value = value;
        this.maxSize = maxSize;
        this.maxCount = maxCount;
    }

    public static SpaceLevelEnum getEnumByValue(int value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (SpaceLevelEnum spaceLevelEnum : SpaceLevelEnum.values()) {
            if (spaceLevelEnum.getValue().equals(value)) {
                return spaceLevelEnum;
            }
        }
        //数据量比较大时，使用map缓存，避免每次遍历
//        Map<Integer,String> map=new HashMap<>();
//        map.put(PENDING.getValue(),PENDING.getText());
//        map.put(APPROVED.getValue(),APPROVED.getText());
//        map.put(REJECTED.getValue(),REJECTED.getText());
        return null;
    }
}
