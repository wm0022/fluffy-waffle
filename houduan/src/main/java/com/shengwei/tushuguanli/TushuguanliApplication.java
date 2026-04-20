package com.shengwei.tushuguanli;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * 圣惟书店管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.shengwei.tushuguanli.mapper")
@ServletComponentScan
public class TushuguanliApplication {

    public static void main(String[] args) {
        SpringApplication.run(TushuguanliApplication.class, args);
        System.out.println("====================================");
        System.out.println("圣惟书店管理系统启动成功！");
        System.out.println("API 地址：http://localhost:8080/api");
        System.out.println("Druid 监控：http://localhost:8080/api/druid/");
        System.out.println("====================================");
    }
}
