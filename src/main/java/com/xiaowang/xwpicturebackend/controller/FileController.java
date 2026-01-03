package com.xiaowang.xwpicturebackend.controller;

import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.xiaowang.xwpicturebackend.annotation.AutoCheck;
import com.xiaowang.xwpicturebackend.common.BaseResponse;
import com.xiaowang.xwpicturebackend.common.ResultUtils;
import com.xiaowang.xwpicturebackend.constant.UserConstant;
import com.xiaowang.xwpicturebackend.exception.BusinessException;
import com.xiaowang.xwpicturebackend.exception.ErrorCode;
import com.xiaowang.xwpicturebackend.manager.CosManager;
import com.xiaowang.xwpicturebackend.model.enums.UserRoleEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    private final CosManager cosManager;

    public FileController(CosManager cosManager) {
        this.cosManager = cosManager;
    }

    /**
     * 测试上传文件到 COS
     *
     * @param multipartFile
     * @return 文件路径
     */
    @PostMapping("/test/upload")
    @AutoCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();
        String filePath = String.format("/test/%s", fileName);
        File tempFile = null;
        try {
            // 校验文件是否为空
            if (multipartFile.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件为空");
            }
            tempFile = File.createTempFile(filePath, null);
            multipartFile.transferTo(tempFile); // 上传文件到临时目录
            cosManager.putObject(filePath, tempFile); // 上传文件到 COS
            return ResultUtils.success(filePath);
        } catch (IOException e) {
            log.error("上传文件到临时目录失败: {}", fileName, e);
            throw new RuntimeException(e);
        } finally {
            // 删除临时文件
            if (tempFile != null && tempFile.exists()) {
                boolean deleteSuccess = tempFile.delete();
                if (!deleteSuccess) {
                    log.error("删除临时文件失败: {}", tempFile.getAbsolutePath());
                } else {
                    log.info("删除临时文件成功: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 测试从 COS 下载文件
     *
     * @param filePath 文件路径
     * @param response 响应对象
     * @return 文件内容
     */
    @GetMapping("/test/download")
    @AutoCheck(mustRole = UserConstant.ADMIN_ROLE)
    public void testDownloadFile(String filePath, HttpServletResponse response) throws IOException {

        COSObjectInputStream cosObjectInputStream = null;
        try {
            COSObject cosObject = cosManager.getObject(filePath);
            cosObjectInputStream = cosObject.getObjectContent();
            // 设置响应头，指定下载文件的文件名
            response.setContentType("application/octet-stream; charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filePath);
            // 从 COS 下载文件内容并写入响应输出流
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = cosObjectInputStream.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, bytesRead);
            }
            response.flushBuffer();
        } catch (IOException e) {
            log.error("下载文件失败: {}", filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载文件失败");
        } finally {
            if (cosObjectInputStream != null) {
                cosObjectInputStream.close();
            }
        }
    }
}
