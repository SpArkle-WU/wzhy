package cn.wolfcode.wolf2w;

import cn.wolfcode.wolf2w.common.security.annotation.EnableRyFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(excludeName = {
        // search 服务不连数据库：
        // 1) 排除 Spring 内置 DataSource 自动装配
        // 2) 用 excludeName(字符串) 排除 DruidDataSourceAutoConfigure 双保险；
        //    用字符串而不是 .class：search 模块 pom 不直接依赖 druid-starter，类路径可能没有该类
        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
        "com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure"
}, exclude = { DataSourceAutoConfiguration.class })
@EnableDiscoveryClient
@EnableRyFeignClients
public class SearchApplication{
    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
    }
}
