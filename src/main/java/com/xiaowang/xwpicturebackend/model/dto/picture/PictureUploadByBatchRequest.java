package com.xiaowang.xwpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;


/**
 * 图片批量上传请求
 */
@Data
public class PictureUploadByBatchRequest implements Serializable {

    private static final long serialVersionUID = -1031973841984676883L;

    /**
     * 搜索文本
     */
    private String searchText;


     /**
     * 图片名称前缀
     */
    private String namePrefix;

    /**
     * 抓取数量
     */
    private Integer count = 10;


}
