package cn.wolfcode.wolf2w;

import cn.wolfcode.wolf2w.common.security.annotation.EnableCustomConfig;
import cn.wolfcode.wolf2w.common.security.annotation.EnableRyFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.web.context.annotation.ApplicationScope;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class}) // 禁用数据源,因为项目没用数据库,但是模块中有依赖Mybatis-Plus的,所有要排除掉
@EnableRyFeignClients                 // 开启Ry封装的Feign
@EnableCustomConfig                   // 封装MapperScan等配置
public class MessageApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessageApplication.class, args);
    }
}
