package com.xiaowang.xwpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.*;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xiaowang.xwpicturebackend.config.CosClientConfig;
import com.xiaowang.xwpicturebackend.exception.BusinessException;
import com.xiaowang.xwpicturebackend.exception.ErrorCode;
import com.xiaowang.xwpicturebackend.exception.ThrowUtils;
import com.xiaowang.xwpicturebackend.manager.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * URL图片上传模板
 */
@Service
@Slf4j
public class UrlPictureUpload extends PictureUploadTemplate {
    public UrlPictureUpload(CosManager cosManager, CosClientConfig cosClientConfig) {
        super(cosManager, cosClientConfig);
    }

    protected void processFile(Object inputSource, File tempFile) {
        String fileUrl = (String) inputSource;
        // 定义允许的重试次数
        int retryTimes = 1;
        boolean downloadSuccess = false;
        // TODO 这里要解决一下反爬的问题，现在直接用浏览器的 User-Agent 会被反爬识别 等我足够强大了再来搞吧
        for (int i = 0; i <= retryTimes; i++) {
            try (HttpResponse response = HttpRequest.get(fileUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36")
                    .header("Referer", getRefererByDomain(fileUrl)) // 按域名适配Referer
                    .header("Accept", "image/webp,image/png,image/jpeg,image/gif,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br") // 模拟浏览器编码
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8") // 模拟语言
                    .timeout(10000)
                    .setFollowRedirects(true) // 跟随302重定向
                    .execute()) {

                int statusCode = response.getStatus();
                // 只处理200状态码，其他直接标记失败
                if (statusCode == 200) {
                    FileUtil.writeBytes(response.bodyBytes(), tempFile);
                    downloadSuccess = true;
                    break;
                } else {
                    log.warn("下载图片失败（状态码{}），URL：{}，重试次数：{}", statusCode, fileUrl, i);
                    // 重试前休眠500ms，降低请求频率
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("下载重试休眠被中断，URL：{}", fileUrl);
                break;
            }
        }

        // 下载失败直接抛异常，让上层跳过该图片
        if (!downloadSuccess) {
            throw new RuntimeException("文件下载失败（多次重试仍失败），URL：" + fileUrl);
        }
    }

    /**
     * 按域名适配Referer（尝试绕过防盗链）
     */
    private String getRefererByDomain(String url) {
        if (url.contains("huanqiucdn.cn")) {
            return "https://www.huanqiu.com/"; // 适配环球网防盗链
        } else if (url.contains("myqcloud.com")) {
            return "https://cn.bing.com/"; // 适配腾讯云图片防盗链
        } else {
            return "https://cn.bing.com/images/"; // 默认Referer
        }
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
