package com.xiaowang.xwpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xiaowang.xwpicturebackend.config.CosClientConfig;
import com.xiaowang.xwpicturebackend.exception.BusinessException;
import com.xiaowang.xwpicturebackend.exception.ErrorCode;
import com.xiaowang.xwpicturebackend.exception.ThrowUtils;
import com.xiaowang.xwpicturebackend.manager.CosManager;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * URL图片上传模板
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    public UrlPictureUpload(CosManager cosManager, CosClientConfig cosClientConfig) {
        super(cosManager, cosClientConfig);
    }

    @Override
    protected void processFile(Object inputSource, File tempFile) {
        String fileUrl = (String) inputSource;
        //下载文件到临时目录
        HttpUtil.downloadFile(fileUrl, tempFile);
    }

    @Override
    protected String getOriginalFileName(Object inputSource) {
        String fileUrl = (String) inputSource;

        // 可选：清理URL参数（?及之后内容），不影响核心逻辑且适配更多场景
        int paramIndex = fileUrl.indexOf("?");
        if (paramIndex != -1) {
            fileUrl = fileUrl.substring(0, paramIndex);
        }

        int lastSlashIndex = fileUrl.lastIndexOf("/");
        return lastSlashIndex == -1 ? fileUrl : fileUrl.substring(lastSlashIndex + 1);
    }


    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        //校验图片URL是否为空
        if (StringUtils.isBlank(fileUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片URL不能为空");
        }
        //校验URL格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片URL格式错误");
        }
        //校验URL协议
        //校验URL协议是否为HTTP或HTTPS
        if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片URL协议必须为HTTP或HTTPS");
        }
        //发送Head请求验证URL是否有效
        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            //校验URL是否指向图片文件
            String contentType = httpResponse.header("Content-Type");
            //校验Content-Type是否包含图片类型
            if (StrUtil.isNotBlank(contentType)) {
                final List<String> defaultImageTypes = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/jpg", "image/webp");
                ThrowUtils.throwIf(!defaultImageTypes.contains(contentType.toLowerCase()), ErrorCode.PARAMS_ERROR, "图片URL指向的文件不是图片类型");
            }
            //文件存在，校验文件大小是否超过2MB
            String contentLengthStr = httpResponse.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long ONE_MB = 1024 * 1024;
                    ThrowUtils.throwIf(contentLength > 2 * ONE_MB, ErrorCode.PARAMS_ERROR, "图片文件大小不能超过2MB");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片URL指向的文件大小格式错误");
                }
            }
        } finally {
            if (httpResponse != null) {
                httpResponse.close();
            }
        }
    }
}
