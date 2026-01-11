package com.xiaowang.xwpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaowang.xwpicturebackend.exception.BusinessException;
import com.xiaowang.xwpicturebackend.exception.ErrorCode;
import com.xiaowang.xwpicturebackend.exception.ThrowUtils;
import com.xiaowang.xwpicturebackend.manager.CosManager;
import com.xiaowang.xwpicturebackend.manager.FileManager;
import com.xiaowang.xwpicturebackend.manager.upload.FilePictureUpload;
import com.xiaowang.xwpicturebackend.manager.upload.PictureUploadTemplate;
import com.xiaowang.xwpicturebackend.manager.upload.UrlPictureUpload;
import com.xiaowang.xwpicturebackend.model.dto.file.UploadPictureResult;
import com.xiaowang.xwpicturebackend.model.dto.picture.PictureQueryRequest;
import com.xiaowang.xwpicturebackend.model.dto.picture.PictureReviewRequest;
import com.xiaowang.xwpicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.xiaowang.xwpicturebackend.model.dto.picture.PictureUploadRequest;
import com.xiaowang.xwpicturebackend.model.entity.Picture;
import com.xiaowang.xwpicturebackend.model.entity.User;
import com.xiaowang.xwpicturebackend.model.enums.PictureReviewStatusEnum;
import com.xiaowang.xwpicturebackend.model.vo.PictureVO;
import com.xiaowang.xwpicturebackend.model.vo.UserVO;
import com.xiaowang.xwpicturebackend.service.PictureService;
import com.xiaowang.xwpicturebackend.mapper.PictureMapper;
import com.xiaowang.xwpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author wangjialeNB
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2026-01-03 17:44:09
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {


    private final UrlPictureUpload urlPictureUpload;
    private final FilePictureUpload filePictureUpload;
    private final UserService userService;
    private final CosManager cosManager;


    public PictureServiceImpl(UrlPictureUpload urlPictureUpload, FilePictureUpload filePictureUpload, UserService userService, CosManager cosManager) {
        this.urlPictureUpload = urlPictureUpload;
        this.filePictureUpload = filePictureUpload;
        this.userService = userService;
        this.cosManager = cosManager;
    }

    // 正则匹配跳转链接中的mediaurl参数（原图URL）
    private static final Pattern MEDIA_URL_PATTERN = Pattern.compile("mediaurl=([^&]+)");
    private static final String BING_PREFIX = "https://www.bing.com";

    /**
     * 上传图片
     *
     * @param inputSource          图片文件或URL
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser            登录用户
     * @return 图片VO
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        //校验
        //校验登录用户
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //判断为新增还是更新操作
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        //如果是更新，还要判断图片是否存在
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 仅本人或管理员可更新
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            //清除旧图片在COS中的文件
            this.clearPictureFile(oldPicture);
        }
        //上传
        String uploadPrefix = String.format("public/%s", loginUser.getId());
        //根据inputSource判断是文件上传还是URL上传
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPrefix);
        // 补充一下图片信息
        Picture picture = new Picture();
        picture.setUserId(loginUser.getId());
        picture.setUrl(uploadPictureResult.getUrl());
        //我再次上传判断的时候，如果图片小于20K，不是会直接把缩略图清空吗？
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        String picName = uploadPictureResult.getPicName();
        //如果上传请求中包含图片名称，就用上传请求中的名称
        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picName = pictureUploadRequest.getPicName();
        }
        picture.setName(picName);
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        // 补充一下审核参数
        this.fillReviewParams(picture, loginUser);
        //数据库操作
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        //注意！！！saveOrUpdate方法会根据id是否存在来判断是新增还是更新操作，而且如果字段为null，他会自动忽略这个字段，不会更新为null
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败，数据库操作异常");
        log.info("图片对象为{}", picture);
        log.info("转换成图片VO为{}", PictureVO.objToVo(picture));
        //返回VO
        return PictureVO.objToVo(picture);
    }


    /**
     * 校验图片是否合法（新增/更新时调用）
     *
     * @param picture 图片
     */
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "图片不能为空");
        Long id = picture.getId();
        String url = picture.getUrl();
        // 修改图片的时候，要做一个参数校验
        String introduction = picture.getIntroduction();
        ThrowUtils.throwIf(ObjectUtil.isNull(id), ErrorCode.PARAMS_ERROR, "图片id不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "图片url长度不能超过1024");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 1024, ErrorCode.PARAMS_ERROR, "图片介绍长度不能超过1024");
        }
    }


    /**
     * 获取图片VO
     *
     * @param picture 图片
     * @param request 请求
     * @return 图片VO
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    /**
     * 转换Picture分页对象为PictureVO分页对象，并填充关联的用户信息
     *
     * @param picturePage Picture实体的分页对象，包含分页参数和Picture列表数据
     * @param request     HTTP请求对象（预留扩展使用，如获取请求头/参数等）
     * @return Page<PictureVO> 转换后的PictureVO分页对象，已填充完整的用户信息
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        // 1. 获取Picture分页对象中的具体数据列表
        List<Picture> pictureList = picturePage.getRecords();

        // 2. 初始化PictureVO分页对象，复用原分页的当前页、每页条数、总记录数（保证分页参数一致性）
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());

        // 3. 空值校验：如果Picture列表为空，直接返回空的VO分页对象（避免后续空指针）
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }

        // 4. 将Picture实体列表转换为PictureVO列表（基础属性转换）
        List<PictureVO> pictureVOList = pictureList.stream()
                .map(PictureVO::objToVo)  // 调用PictureVO的静态方法完成单个对象属性映射
                .collect(Collectors.toList());

        // 5. 提取所有Picture关联的用户ID（去重），用于批量查询用户信息（减少数据库查询次数）
        Set<Long> userIds = pictureList.stream()
                .map(Picture::getUserId)
                .collect(Collectors.toSet());

        // 6. 批量查询用户信息，并按用户ID分组（key：用户ID，value：对应用户列表）
        //    注：groupingBy保证同一个用户ID的用户数据聚合，此处取第一个是因为用户ID唯一
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIds).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 7. 遍历PictureVO列表，为每个VO填充关联的用户VO信息
        pictureVOList.forEach(pictureVO -> {
            // 获取当前PictureVO关联的用户ID
            Long userId = pictureVO.getUserId();
            // 初始化用户对象（默认null，避免空指针）
            User user = null;
            // 如果用户ID在查询结果中存在，取第一个用户对象（用户ID唯一）
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            // 将用户对象转换为UserVO并设置到PictureVO中
            pictureVO.setUser(userService.getUserVO(user));
        });

        // 8. 将填充好用户信息的PictureVO列表设置到分页对象中
        pictureVOPage.setRecords(pictureVOList);

        // 9. 返回最终的PictureVO分页对象
        return pictureVOPage;
    }


    /**
     * 获取查询Wrapper
     *
     * @param pictureQueryRequest 图片查询请求
     * @return 查询Wrapper
     */
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        if (pictureQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询参数为空");
        }

        Long id = pictureQueryRequest.getId();
        Long userId = pictureQueryRequest.getUserId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewId = pictureQueryRequest.getReviewId();


        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();

        //多字段查询
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or().like("introduction", searchText));
//                    .or().like("category", searchText)
//                    .or().like("tags", searchText));
        }

        queryWrapper.eq(ObjectUtil.isNotNull(id), "id", id);
        queryWrapper.eq(ObjectUtil.isNotNull(userId), "userId", userId);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjectUtil.isNotNull(picSize), "picSize", picSize);
        queryWrapper.eq(ObjectUtil.isNotNull(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjectUtil.isNotNull(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjectUtil.isNotNull(picScale), "picScale", picScale);
        queryWrapper.eq(ObjectUtil.isNotNull(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjectUtil.isNotNull(reviewId), "reviewId", reviewId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);


        //Tag 标签查询
        if (CollectionUtil.isNotEmpty(tags)) {
            //and (tags like  "%\java\"%" and like "%\python\"%)
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), StrUtil.equals(sortOrder, "ascend"), sortField);
        return queryWrapper;
    }

    /**
     * 审核图片
     *
     * @param pictureReviewRequest 图片审核请求
     * @param loginUser            登录用户
     */
    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        //1、校验参数
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR, "图片审核请求为空");
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        String reviewMessage = pictureReviewRequest.getReviewMessage();
        if (id == null || reviewStatusEnum == null || PictureReviewStatusEnum.PENDING.equals(reviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片审核请求参数错误");
        }
        //2、校验图片是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        //3、校验审核状态是否合法
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片审核状态重复");
        }
        //4、操作数据库
        Picture newPicture = new Picture();
        BeanUtil.copyProperties(oldPicture, newPicture);
        newPicture.setReviewId(loginUser.getId());
        newPicture.setReviewMessage(reviewMessage);
        newPicture.setReviewTime(new Date());
        newPicture.setReviewStatus(reviewStatusEnum.getValue());
        boolean result = this.updateById(newPicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片审核失败");
    }


    /**
     * 校验图片审核参数
     *
     * @param picture   图片
     * @param loginUser 登录用户
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            // 管理员可以直接审核通过
            picture.setReviewStatus(PictureReviewStatusEnum.APPROVED.getValue());
            picture.setReviewMessage("管理员自动审核通过");
            picture.setReviewId(loginUser.getId());
            picture.setReviewTime(new Date());
        } else {
            // 普通用户不管做任何操作，都默认是待审核
            picture.setReviewStatus(PictureReviewStatusEnum.PENDING.getValue());
        }
    }


    /**
     * 批量上传图片（修改后：抓取原图URL上传）
     *
     * @param pictureUploadByBatchRequest 图片批量上传请求
     * @param loginUser                   登录用户
     * @return 上传成功图片数量
     */
    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        // 1、校验参数（完全保留原有逻辑）
        final Integer DEFAULT_MAX_COUNT = 30;
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        ThrowUtils.throwIf(StrUtil.isBlank(searchText), ErrorCode.PARAMS_ERROR, "搜索文本为空");
        ThrowUtils.throwIf(count == null || count <= 0, ErrorCode.PARAMS_ERROR, "抓取数量错误");
        ThrowUtils.throwIf(count.compareTo(DEFAULT_MAX_COUNT) > 0, ErrorCode.PARAMS_ERROR, "抓取数量不能超过30");
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }

        // 2、发送请求，抓取图片列表页（保留原有逻辑，仅修改后续解析部分）
        final String DEFAULT_PICTURE_FETCH_URL = "https://cn.bing.com/images/async?q=%s&mmasync=1";
        String fetchUrl = String.format(DEFAULT_PICTURE_FETCH_URL, searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("图片批量上传失败，搜索文本：{}，抓取数量：{}，异常信息：{}", searchText, count, e.getMessage());
            throw new BusinessException(ErrorCode.PARAMS_ERROR, e.getMessage());
        }

        // 3、核心修改：获取图片跳转链接（替代原来的img.mimg）
        Elements jumpLinkElements = document.select("div.imgpt > a");
        // 校验跳转链接是否为空（替换原来的imgElementList校验）
        ThrowUtils.throwIf(ObjectUtil.isEmpty(jumpLinkElements), ErrorCode.OPERATION_ERROR, "获取图片跳转链接失败");

        // 4、遍历跳转链接，解析原图URL并上传（替换原来的imgElementList遍历）
        int uploadCount = 0;
        for (Element aElement : jumpLinkElements) {
            // 控制上传数量，达到目标后停止
            if (uploadCount >= count) {
                break;
            }

            // 提取跳转链接并拼接完整URL
            String relativeJumpUrl = aElement.attr("href");
            if (StrUtil.isBlank(relativeJumpUrl)) {
                log.info("当前跳转链接为空，跳过");
                continue;
            }
            String fullJumpUrl = BING_PREFIX + relativeJumpUrl;

            // 解析mediaurl参数，获取原图URL
            String fileUrl = parseMediaUrl(fullJumpUrl);
            if (StrUtil.isBlank(fileUrl)) {
                log.info("跳转链接 {} 未解析到原图URL，跳过", fullJumpUrl);
                continue;
            }

//            // 处理图片URL（保留原有过滤逻辑，防止URL带参数）
//            int questionMarkIndex = fileUrl.indexOf("?");
//            if (questionMarkIndex > -1) {
//                fileUrl = fileUrl.substring(0, questionMarkIndex);
//            }

            // 构造图片名称（保留原有逻辑）
            String picName = String.format("%s_%s", namePrefix, uploadCount + 1);
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setFileUrl(fileUrl);
            pictureUploadRequest.setPicName(picName);

            // 上传图片（保留原有上传逻辑）
            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                log.info("图片上传成功，图片ID：{}，原图URL：{}", pictureVO.getId(), fileUrl);
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败，原图URL：{}，异常信息：{}", fileUrl, e.getMessage());
            }
        }

        return uploadCount;
    }

    /**
     * 工具方法：从跳转链接中解析mediaurl参数（URL解码后得到原图URL）
     */
    private String parseMediaUrl(String jumpUrl) {
        Matcher matcher = MEDIA_URL_PATTERN.matcher(jumpUrl);
        if (matcher.find()) {
            // 提取参数并URL解码（必应对参数做了UTF-8编码）
            String encodedUrl = matcher.group(1);
            try {
                return URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.error("URL解码失败，编码URL：{}，异常信息：{}", encodedUrl, e.getMessage());
                return null;
            }
        }
        return null;
    }

    /**
     * 清理图片文件（删除COS中对应的图片和缩略图）
     *
     * @param oldPicture 要删除的图片对象
     */
    @Override
    @Async
    public void clearPictureFile(Picture oldPicture) {
        // 校验参数
        ThrowUtils.throwIf(ObjectUtil.isEmpty(oldPicture), ErrorCode.PARAMS_ERROR, "图片为空");
        String url = oldPicture.getUrl();
        ThrowUtils.throwIf(StrUtil.isBlank(url), ErrorCode.PARAMS_ERROR, "图片URL为空");

        // 判断图片是否被多条记录引用
        long count = this.lambdaQuery().eq(Picture::getUrl, url).count();
        ThrowUtils.throwIf(count > 1, ErrorCode.OPERATION_ERROR, "图片被多条记录引用，不能删除");

        // 1. 解析图片URL为COS的key并删除
        String pictureKey = parseCosUrlToKey(url);
        cosManager.deleteObject(pictureKey);

        // 2. 解析缩略图URL为COS的key并删除（如果有）
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        String thumbnailKey = "";
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            thumbnailKey = parseCosUrlToKey(thumbnailUrl);
            cosManager.deleteObject(thumbnailKey);
        }

        log.info("图片文件删除成功，图片KEY：{}，缩略图KEY：{}", pictureKey, thumbnailKey);
    }

    /**
     * 工具方法：从COS完整URL中解析出对象的key
     * 示例URL：https://xw-1382515737.cos.ap-guangzhou.myqcloud.com/public/2006966019971723266/2026-01-10_Krh0ykvrBFH32MMu.webp
     * 解析后key：public/2006966019971723266/2026-01-10_Krh0ykvrBFH32MMu.webp
     *
     * @param cosUrl COS对象的完整URL
     * @return COS对象的key
     */
    private String parseCosUrlToKey(String cosUrl) {
        return cosUrl.substring(cosUrl.indexOf(".com/") + 5);
    }
}




