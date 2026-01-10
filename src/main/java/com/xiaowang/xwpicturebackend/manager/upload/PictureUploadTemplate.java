package com.xiaowang.xwpicturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;

import cn.hutool.json.JSONUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.job.ProcessResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import com.xiaowang.xwpicturebackend.config.CosClientConfig;

import com.xiaowang.xwpicturebackend.manager.CosManager;
import com.xiaowang.xwpicturebackend.model.dto.file.UploadPictureResult;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Slf4j
public abstract class PictureUploadTemplate {
    private final CosManager cosManager;
    private final CosClientConfig cosClientConfig;

    public PictureUploadTemplate(CosManager cosManager, CosClientConfig cosClientConfig) {
        this.cosManager = cosManager;
        this.cosClientConfig = cosClientConfig;
    }

    /**
     * 上传图片
     *
     * @param inputSource  图片源
     * @param uploadPrefix 上传前缀
     * @return 上传结果
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPrefix) {
        //1、校验图片
        validPicture(inputSource);
        //2、获取图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFileName = getOriginalFileName(inputSource);
        //查查一下url的后缀是否是图片后缀
        // 如果不是数字或为空，默认使用 jpg 格式
        String suffix = FileUtil.getSuffix(originalFileName);
        if (!NumberUtil.isNumber(suffix) || suffix.isEmpty()) {
            suffix = "jpg";
        }
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, suffix);
        String uploadFilePath = String.format("/%s/%s", uploadPrefix, uploadFileName);
        File tempFile = null;
        try {
            //3、创建临时文件，并获取文件到服务器
            tempFile = File.createTempFile(uploadFileName, null);
            // 处理文件
            processFile(inputSource, tempFile);
            //4、上传文件到 COS
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadFilePath, tempFile);// 上传文件到 COS
            //5、获取图片信息对象，封装返回结果
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //获取图片处理后的结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            // 处理结果中获取压缩后的图片信息
            List<CIObject> objectList = processResults.getObjectList();
            // 检查处理结果是否为空
            if (CollUtil.isNotEmpty(objectList)) {
                if (objectList.size() > 1) {
                    // 处理结果中获取压缩后的图片信息
                    CIObject compressedObject = objectList.get(1);
                    // 处理结果中获取缩略图信息
                    CIObject thumbnailObject = objectList.get(0);
                    log.info("处理结果中获取压缩后的图片信息: {}", JSONUtil.toJsonStr(compressedObject));
                    log.info("处理结果中获取缩略图信息: {}", JSONUtil.toJsonStr(thumbnailObject));
                    // 构建上传结果
                    return buildResult(originalFileName, compressedObject, thumbnailObject);
                } else {
                    // 处理结果中获取压缩后的图片信息
                    CIObject compressedObject = objectList.get(0);
                    log.info("处理结果中获取压缩后的图片信息: {}", JSONUtil.toJsonStr(compressedObject));
                    // 构建上传结果
                    return buildResult(originalFileName, compressedObject, null);
                }
            }
            return buildResult(imageInfo, uploadFilePath, originalFileName, tempFile);

        } catch (IOException e) {
            log.error("上传文件到临时目录失败: {}", uploadFileName, e);
            throw new RuntimeException(e);
        } finally {
            //6、删除临时文件
            deleteTempFile(tempFile);
        }
    }


    /**
     * 处理文件
     *
     * @param inputSource 图片源
     * @param tempFile    临时文件
     */
    protected abstract void processFile(Object inputSource, File tempFile);

    /**
     * 获取原始文件名
     *
     * @param inputSource 图片源
     * @return 原始文件名
     */
    protected abstract String getOriginalFileName(Object inputSource);

    /**
     * 校验图片
     *
     * @param inputSource 图片源
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 构建上传结果
     *
     * @param imageInfo        图片信息
     * @param uploadFilePath   上传文件路径
     * @param originalFileName 原始文件名
     * @param tempFile         临时文件
     * @return 上传结果
     */
    private @NonNull UploadPictureResult buildResult(ImageInfo imageInfo, String uploadFilePath, String originalFileName, File tempFile) {
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
    }


    /**
     * 构建上传结果
     *
     * @param originalFileName 原始文件名
     * @param compressedObject 压缩后的图片信息
     * @param thumbnailObject  缩略图信息
     * @return 上传结果
     */
    private @NonNull UploadPictureResult buildResult(String originalFileName, CIObject compressedObject, CIObject thumbnailObject) {
        int picHeight = compressedObject.getHeight();
        int picWidth = compressedObject.getWidth();
        double picScale = NumberUtil.round((double) picHeight / picWidth, 2).doubleValue();
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        // compressedObject.getKey() -- 压缩后的图片路径
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressedObject.getKey());
        uploadPictureResult.setPicName(FileUtil.mainName(originalFileName));
        uploadPictureResult.setPicSize(Long.valueOf(compressedObject.getSize()));
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressedObject.getFormat());
        // thumbnailObject.getKey() -- 缩略图路径
        if (thumbnailObject != null) {
            uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailObject.getKey());
        }else{
            uploadPictureResult.setThumbnailUrl(null);
        }
        return uploadPictureResult;
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


}
