package com.xiaowang.xwpicturebackend;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaowang.xwpicturebackend.exception.BusinessException;
import com.xiaowang.xwpicturebackend.exception.ErrorCode;
import com.xiaowang.xwpicturebackend.exception.ThrowUtils;
import com.xiaowang.xwpicturebackend.model.dto.picture.PictureUploadRequest;
import com.xiaowang.xwpicturebackend.model.vo.PictureVO;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.File;
import java.io.IOException;
import java.net.URL;

public class BingImageCrawlerTest {

    public static void main(String[] args) {
        //1、校验参数
        final Integer DEFAULT_MAX_COUNT = 30;
        String searchText = "风景";
        Integer count = 10;
        String namePrefix = "xw";
        ThrowUtils.throwIf(StrUtil.isBlank(searchText), ErrorCode.PARAMS_ERROR, "搜索文本为空");
        ThrowUtils.throwIf(count == null || count <= 0, ErrorCode.PARAMS_ERROR, "抓取数量错误");
        ThrowUtils.throwIf(count.compareTo(DEFAULT_MAX_COUNT) > 0, ErrorCode.PARAMS_ERROR, "抓取数量不能超过30");
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        //2、发送请求，抓取内容
        final String DEFAULT_PICTURE_FETCH_URL = "https://cn.bing.com/images/async?q=%s&mmasync=1";
        String fetchUrl = String.format(DEFAULT_PICTURE_FETCH_URL, searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            System.out.printf("图片批量上传失败，搜索文本：%s，抓取数量：%d，异常信息：%s%n", searchText, count, e.getMessage());
            throw new BusinessException(ErrorCode.PARAMS_ERROR, e.getMessage());
        }
        //3、处理响应
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjectUtil.isEmpty(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements imgElementList = null;
        if (div != null) {
            imgElementList = div.select("img.mimg");
        }
        //遍历元素，依次处理图片
        if (imgElementList != null) {
            System.out.printf("共获取到 %d 张图片%n", imgElementList.size());
        }

        imgElementList.forEach(image -> {
            System.out.println(image.attr("src"));
        });

//        int uploadCount = 0;
//        for (Element imgElement : imgElementList) {
//            String fileUrl = imgElement.attr("src");
//            if (StrUtil.isBlank(fileUrl)) {
//                System.out.printf("当前链接为空，跳过:%s%n", fileUrl);
//                continue;
//            }
//            //处理图片的URL，过滤掉无效的URL
//            int questionMarkIndex = fileUrl.indexOf("?");
//            if (questionMarkIndex > -1) {
//                fileUrl = fileUrl.substring(0, questionMarkIndex);
//            }
//            System.out.printf("当前图片URL:%s%n", fileUrl);
//
//            //早存到本地images目录下
//            String fileName = String.format("images/%s_%s.jpg", namePrefix, uploadCount + 1);
//            try {
//                FileUtils.copyURLToFile(new URL(fileUrl), new File(fileName));
//                System.out.printf("图片 %s 已保存到本地%n", fileName);
//            } catch (IOException e) {
//                System.out.printf("图片 %s 保存到本地失败，异常信息：%s%n", fileName, e.getMessage());
//            }
//            uploadCount++;
//            if (uploadCount >= count) {
//                System.out.printf("已成功上传 %d 张图片%n", uploadCount);
//                break;
//            }
//        }
    }

}