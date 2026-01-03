package com.xiaowang.xwpicturebackend.service;

import com.xiaowang.xwpicturebackend.model.dto.picture.PictureUploadRequest;
import com.xiaowang.xwpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaowang.xwpicturebackend.model.entity.User;
import com.xiaowang.xwpicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

/**
* @author wangjialeNB
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2026-01-03 17:44:09
*/
public interface PictureService extends IService<Picture> {


    /**
     * 上传图片
     * @param multipartFile 图片文件
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser            登录用户
     * @return 图片VO
     * @throws Exception 异常
     */
    PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) ;


    //TODO 图片的删改查
}
