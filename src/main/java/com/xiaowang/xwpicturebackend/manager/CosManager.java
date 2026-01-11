package com.xiaowang.xwpicturebackend.manager;


import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.xiaowang.xwpicturebackend.config.CosClientConfig;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class CosManager {
    private final COSClient cosClient;
    private final CosClientConfig clientConfig;

    public CosManager(COSClient cosClient, CosClientConfig clientConfig) {
        this.cosClient = cosClient;
        this.clientConfig = clientConfig;
    }

    /**
     * 上传对象到 COS
     *
     * @param key  对象在 COS 中的键
     * @param file 本地文件
     * @return 上传结果
     * @throws CosClientException 客户端异常
     */
    public PutObjectResult putObject(String key, File file)
            throws CosClientException {
        PutObjectRequest putObjectRequest = new PutObjectRequest(clientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }


    /**
     * 从 COS 获取对象
     *
     * @param key 对象在 COS 中的键
     * @return COSObject 对象
     * @throws CosClientException 客户端异常
     */
    public COSObject getObject(String key) throws CosClientException {
        GetObjectRequest getObjectRequest = new GetObjectRequest(clientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }


    /**
     * 上传图片到 COS
     *
     * @param key  图片在 COS 中的键
     * @param file 本地图片文件
     * @return 上传结果
     * @throws CosClientException 客户端异常
     */
    public PutObjectResult putPictureObject(String key, File file)
            throws CosClientException {
        PutObjectRequest putObjectRequest = new PutObjectRequest(clientConfig.getBucket(), key, file);
        PicOperations picOperations = new PicOperations();
        // 返回原图信息
        picOperations.setIsPicInfo(1);
        //图片压缩（webp格式）
        String webpKey = FileUtil.mainName(key) + ".webp";
        // 图片压缩规则
        List<PicOperations.Rule> rules = new ArrayList<>();
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setBucket(clientConfig.getBucket());
        compressRule.setFileId(webpKey);
        compressRule.setRule("imageMogr2/format/webp");
        rules.add(compressRule);
        //缩略图规则，仅对尺寸大于20K的图片进行生成缩略图
        if (file.length() > 20 * 1024) {
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            thumbnailRule.setBucket(clientConfig.getBucket());
            String thumbnailKey = FileUtil.mainName(key) + "-thumbnail." + FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            ///imageMogr2/thumbnail/<Width>x<Height>>
            // 缩略图尺寸
            final String thumbnailWidth = "256";
            final String thumbnailHeight = "256";
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", thumbnailWidth, thumbnailHeight));
            rules.add(thumbnailRule);
        }
        picOperations.setRules(rules);
        // 开启图片处理功能
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }


    /**
     * 删除 COS 中的对象
     *
     * @param key 对象在 COS 中的键
     * @throws CosClientException 客户端异常
     */
    public void deleteObject(String key) throws CosClientException {
        cosClient.deleteObject(clientConfig.getBucket(), key);
    }


}
