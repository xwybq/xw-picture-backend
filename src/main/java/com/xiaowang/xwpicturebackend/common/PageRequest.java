package com.xiaowang.xwpicturebackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求
 */
@Data
public class PageRequest implements Serializable {
    /**
     * 当前页号
     */
    private long  current = 1;
    /**
     * 页面大小
     */
    private long pageSize = 10;
    /**
     * 排序字段
     */
    private String sortField;
    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = "descend";
    private static final long serialVersionUID = -5044865231570776363L;
}
