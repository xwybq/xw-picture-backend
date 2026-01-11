package com.xiaowang.xwpicturebackend;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.xiaowang.xwpicturebackend.exception.BusinessException;
import com.xiaowang.xwpicturebackend.exception.ErrorCode;
import com.xiaowang.xwpicturebackend.exception.ThrowUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BingImageCrawlerTest {

    // 正则匹配mediaurl参数（核心）
    private static final Pattern MEDIA_URL_PATTERN = Pattern.compile("mediaurl=([^&]+)");

    public static void main(String[] args) {
        // 1、校验参数（保留你的逻辑）
        final Integer DEFAULT_MAX_COUNT = 30;
        String searchText = "黑丝";
        Integer count = 10;
        String namePrefix = "xw";
        ThrowUtils.throwIf(StrUtil.isBlank(searchText), ErrorCode.PARAMS_ERROR, "搜索文本为空");
        ThrowUtils.throwIf(count == null || count <= 0, ErrorCode.PARAMS_ERROR, "抓取数量错误");
        ThrowUtils.throwIf(count.compareTo(DEFAULT_MAX_COUNT) > 0, ErrorCode.PARAMS_ERROR, "抓取数量不能超过30");
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }

        // 2、发送请求，抓取图片列表页（保留你的逻辑）
        final String DEFAULT_PICTURE_FETCH_URL = "https://cn.bing.com/images/async?q=%s&mmasync=1";
        String fetchUrl = String.format(DEFAULT_PICTURE_FETCH_URL, searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            System.out.printf("图片批量抓取失败，搜索文本：%s，抓取数量：%d，异常信息：%s%n", searchText, count, e.getMessage());
            throw new BusinessException(ErrorCode.PARAMS_ERROR, e.getMessage());
        }

        // 3、获取图片跳转链接（保留你的逻辑）
        Elements jumpLinkElements = document.select("div.imgpt > a");
        if (jumpLinkElements.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未获取到图片跳转链接");
        }
        System.out.printf("共获取到 %d 个图片跳转链接%n", jumpLinkElements.size());

        // 4、核心修改：直接解析跳转链接的mediaurl参数（不用访问详情页）
        final String BING_PREFIX = "https://www.bing.com";
        List<String> originImgUrlList = new ArrayList<>();

        for (Element aElement : jumpLinkElements) {
            if (originImgUrlList.size() >= count) break; // 控制数量

            // 拼接完整跳转链接
            String relativeJumpUrl = aElement.attr("href");
            if (StrUtil.isBlank(relativeJumpUrl)) continue;
            String fullJumpUrl = BING_PREFIX + relativeJumpUrl;

            // 解析mediaurl参数（原图URL）
            String originImgUrl = parseMediaUrl(fullJumpUrl);
            if (StrUtil.isNotBlank(originImgUrl)) {
                originImgUrlList.add(originImgUrl);
                System.out.printf("解析到原图URL：%s%n", originImgUrl);
            } else {
                System.out.printf("跳转链接 %s 未解析到mediaurl%n", fullJumpUrl);
            }
        }

        // 输出最终结果
        System.out.println("==================== 抓取结果 ====================");
        System.out.printf("共成功获取 %d 张原图URL（目标抓取 %d 张）%n", originImgUrlList.size(), count);
        originImgUrlList.forEach(url -> System.out.println("原图URL：" + url));
    }

    /**
     * 从跳转链接中解析mediaurl参数（URL解码后就是原图地址）
     */
    private static String parseMediaUrl(String jumpUrl) {
        Matcher matcher = MEDIA_URL_PATTERN.matcher(jumpUrl);
        if (matcher.find()) {
            // 提取参数并URL解码（必应对参数做了编码）
            String encodedUrl = matcher.group(1);
            return URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8);
        }
        return null;
    }
}