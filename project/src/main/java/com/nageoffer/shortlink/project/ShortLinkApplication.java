package com.nageoffer.shortlink.project;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@MapperScan("com.nageoffer.shortlink.project.dao.mapper")
@EnableDiscoveryClient
public class ShortLinkApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(ShortLinkApplication.class, args);
    }
}
