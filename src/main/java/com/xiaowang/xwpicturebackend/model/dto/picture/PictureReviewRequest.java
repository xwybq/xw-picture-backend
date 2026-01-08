package com.xiaowang.xwpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class PictureReviewRequest implements Serializable {
    private static final long serialVersionUID = -7231170286101138375L;
    /**
     * id
     */
    private Long id;

    /**
     * 审核状态: 0-待审核, 1-审核通过, 2-审核拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核消息 (审核拒绝时填写)
     */
    private String reviewMessage;
}
