package com.yu.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import javax.annotation.ManagedBean;

@SpringBootApplication
@MapperScan("com.yu.backend.mapper")
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class Backend2Application {

    public static void main(String[] args) {
        SpringApplication.run(Backend2Application.class, args);
    }

}
