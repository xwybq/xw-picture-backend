package com.xiaowang.xwpicturebackend.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;


/**
 * 图片审核状态枚举
 */

@Getter
public enum PictureReviewStatusEnum {
    /**
     * 待审核
     */
    PENDING("待审核", 0),
    /**
     * 审核通过
     */
    APPROVED("审核通过", 1),
    /**
     * 审核拒绝
     */
    REJECTED("审核拒绝", 2);


    private final String text;
    private final Integer value;

    PictureReviewStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public static PictureReviewStatusEnum getEnumByValue(Integer value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (PictureReviewStatusEnum reviewStatusEnum : PictureReviewStatusEnum.values()) {
            if (reviewStatusEnum.getValue().equals(value)) {
                return reviewStatusEnum;
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
