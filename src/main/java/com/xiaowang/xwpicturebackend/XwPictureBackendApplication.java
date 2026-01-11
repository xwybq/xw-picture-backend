package com.xiaowang.xwpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.xiaowang.xwpicturebackend.mapper")
// 开启AspectJ代理，暴露代理对象
@EnableAspectJAutoProxy(exposeProxy = true)
// 开启异步任务支持
@EnableAsync
public class XwPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(XwPictureBackendApplication.class, args);
    }

}
