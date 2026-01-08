package com.xiaowang.xwpicturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.xiaowang.xwpicturebackend.common.ResultUtils;
import com.xiaowang.xwpicturebackend.config.CosClientConfig;
import com.xiaowang.xwpicturebackend.exception.BusinessException;
import com.xiaowang.xwpicturebackend.exception.ErrorCode;
import com.xiaowang.xwpicturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * 文件管理类
 * 已经废弃，使用upload包里面的类
 */
@Component
@Slf4j
@Deprecated
public class FileManager {
    private final CosManager cosManager;
    private final CosClientConfig cosClientConfig;

    public FileManager(CosManager cosManager, CosClientConfig cosClientConfig) {
        this.cosManager = cosManager;
        this.cosClientConfig = cosClientConfig;
    }

    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPrefix) {
        //校验图片
        validPicture(multipartFile);
        //图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFileName = multipartFile.getOriginalFilename();
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFileName));
        String uploadFilePath = String.format("/%s/%s", uploadPrefix, uploadFileName);
        //解析图片并返回结果
        File tempFile = null;
        try {
            // 校验文件是否为空
            if (multipartFile.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件为空");
            }
            tempFile = File.createTempFile(uploadFileName, null);
            multipartFile.transferTo(tempFile); // 上传文件到临时目录
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadFilePath, tempFile);// 上传文件到 COS
            // 获取图片信息对象

            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

            int picHeight = imageInfo.getHeight();
            int picWidth = imageInfo.getWidth();
            double picScale = NumberUtil.round((double) picHeight / picWidth, 2).doubleValue();

            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + uploadFilePath);
            uploadPictureResult.setPicName(FileUtil.mainName(originalFileName));
            uploadPictureResult.setPicSize(FileUtil.size(tempFile));
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            return uploadPictureResult;

        } catch (IOException e) {
            log.error("上传文件到临时目录失败: {}", uploadFileName, e);
            throw new RuntimeException(e);
        } finally {
            // 删除临时文件
            deleteTempFile(tempFile);
        }
    }

    /**
     * 删除临时文件
     *
     * @param file 临时文件
     */
    private void deleteTempFile(File file) {
        if (file != null && file.exists()) {
            boolean deleteSuccess = file.delete();
            if (!deleteSuccess) {
                log.error("删除临时文件失败: {}", file.getAbsolutePath());
            } else {
                log.info("删除临时文件成功: {}", file.getAbsolutePath());
            }
        }
    }

    /**
     * 校验图片
     *
     * @param multipartFile 图片文件
     */
    private void validPicture(MultipartFile multipartFile) {
        //校验
        //校验文件是否为空
        if (multipartFile.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        //校验文件大小
        final long MAX_FILE_SIZE = 1024 * 1024;
        if (multipartFile.getSize() > MAX_FILE_SIZE * 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过" + "2MB");
        }
        //检验文件后缀
        final List<String> acceptFileTypes = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");
        final String fileName = multipartFile.getOriginalFilename();
        if (!acceptFileTypes.contains(FileUtil.getSuffix(fileName))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件后缀必须为" + acceptFileTypes);
        }
    }

}
