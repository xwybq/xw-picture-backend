package com.xiaowang.xwpicturebackend.manager.upload;


import cn.hutool.core.io.FileUtil;
import com.xiaowang.xwpicturebackend.config.CosClientConfig;
import com.xiaowang.xwpicturebackend.exception.BusinessException;
import com.xiaowang.xwpicturebackend.exception.ErrorCode;
import com.xiaowang.xwpicturebackend.manager.CosManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 文件图片上传
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate {

    public FilePictureUpload(CosManager cosManager, CosClientConfig cosClientConfig) {
        super(cosManager, cosClientConfig);
    }

    @Override
    protected void processFile(Object inputSource, File tempFile) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        try {
            multipartFile.transferTo(tempFile); // 上传文件到临时目录
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件上传失败");
        }
    }

    @Override
    protected String getOriginalFileName(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        //校验文件是否为空
        if (multipartFile.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        //校验文件大小
        final long ONE_MB = 1024 * 1024;
        if (multipartFile.getSize() > ONE_MB * 2) {
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
