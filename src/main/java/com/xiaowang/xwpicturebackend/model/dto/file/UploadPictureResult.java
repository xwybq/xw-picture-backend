package com.xiaowang.xwpicturebackend.model.dto.file;

import com.xiaowang.xwpicturebackend.model.vo.UserVO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 上传图片解析后的结果
 */
@Data
public class UploadPictureResult implements Serializable {
    private static final long serialVersionUID = 403371172916716253L;


    /**
     * 图片url
     */
    private String url;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 图片大小 (字节)
     */
    private Long picSize;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片缩放比例
     */
    private Double picScale;

    /**
     * 图片格式 (jpg/png等)
     */
    private String picFormat;

}
