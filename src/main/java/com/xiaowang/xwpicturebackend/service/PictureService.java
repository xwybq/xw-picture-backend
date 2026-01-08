package com.xiaowang.xwpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaowang.xwpicturebackend.model.dto.picture.PictureQueryRequest;
import com.xiaowang.xwpicturebackend.model.dto.picture.PictureReviewRequest;
import com.xiaowang.xwpicturebackend.model.dto.picture.PictureUploadRequest;
import com.xiaowang.xwpicturebackend.model.dto.user.UserQueryRequest;
import com.xiaowang.xwpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaowang.xwpicturebackend.model.entity.User;
import com.xiaowang.xwpicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author wangjialeNB
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2026-01-03 17:44:09
 */
public interface PictureService extends IService<Picture> {


    /**
     * 上传图片
     *
     * @param inputSource        图片文件或URL
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser            登录用户
     * @return 图片VO
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 校验图片是否合法（新增/更新时调用）
     *
     * @param picture 图片
     */
    void validPicture(Picture picture);


    //图片的删改查

    /**
     * 获取图片VO
     *
     * @param picture 图片
     * @param request 请求
     * @return 图片VO
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片VO分页
     *
     * @param picturePage 图片分页
     * @return 图片VO分页
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 获取查询Wrapper
     *
     * @param pictureQueryRequest 图片查询请求
     * @return 查询Wrapper
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);


    /**
     * 审核图片
     *
     * @param pictureReviewRequest 图片审核请求
     * @param loginUser            登录用户
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 校验图片审核参数
     *
     * @param picture 图片
     * @param loginUser 登录用户
     */
    void  fillReviewParams(Picture picture, User loginUser);

}
