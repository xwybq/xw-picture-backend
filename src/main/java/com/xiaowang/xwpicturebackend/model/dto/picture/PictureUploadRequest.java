package com.xiaowang.xwpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {

    private static final long serialVersionUID = 6631065259784821166L;
    /**
     * 图片id
     */
    private Long id;
    /**
     * 图片url
     */
    private String fileUrl;


     /**
     * 图片名称
     */
    private String picName;
}
