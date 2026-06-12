package com.yu.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

import java.security.Security;

@SpringBootApplication
@MapperScan("com.yu.backend.mapper")
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableAsync
public class Backend2Application {

    static {
        // 避免 OpenJDK 环境下 Hutool/BC 触发 JCE cannot authenticate the provider BC
        Security.removeProvider("BC");
    }

    public static void main(String[] args) {
        SpringApplication.run(Backend2Application.class, args);
    }

}
