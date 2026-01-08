package com.xiaowang.xwpicturebackend.manager;


import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.xiaowang.xwpicturebackend.config.CosClientConfig;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class CosManager  {
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
        picOperations.setIsPicInfo(1);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

}
