package com.xiaowang.xwpicturebackend.controller;

import com.xiaowang.xwpicturebackend.common.BaseResponse;
import com.xiaowang.xwpicturebackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    @GetMapping("/health")
    public BaseResponse<String> health() {

        return ResultUtils.success("ok");
    }
}
