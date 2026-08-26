package cn.wolfcode.wolf2w.business.env;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * travel-search 本地兜底：强制把远端(Nacos)下发的 datasource.type 从 DruidDataSource 改成 Spring Boot 自带的 Hikari，
 * 否则 @ConfigurationProperties 绑定阶段会 Class.forName("DruidDataSource") 直接抛 ClassNotFoundException，
 * 即便是 @SpringBootApplication(exclude = DataSourceAutoConfiguration) 也拦不住（绑定发生在 auto-config 之前）。
 *
 * 本 PostProcessor 注册：resources/META-INF/spring.factories
 *                             -> key=org.springframework.boot.env.EnvironmentPostProcessor
 */
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class SearchLocalOverrideEnvironmentPostProcessor implements EnvironmentPostProcessor {

    /** 要插入的高优先级 PropertySource 名称（放在所有 bootstrap/Nacos 属性源之前执行，即可覆盖） */
    private static final String PROPERTY_SOURCE_NAME = "travelSearchLocalOverrides";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // 只有当前应用名是 travel-search 才生效，避免将来这个类被抽到公共模块时误伤其它服务
        String appName = environment.getProperty("spring.application.name");
        if (StringUtils.hasText(appName) && !"travel-search".equalsIgnoreCase(appName.trim())) {
            return;
        }

        Map<String, Object> overrides = new HashMap<>(4);
        // 1) 强制覆盖远端下发的 druid datasource.type
        overrides.put("spring.datasource.type", "com.zaxxer.hikari.HikariDataSource");
        // 2) 顺便把 ES 配置兜底也塞进来（优先级最高，Nacos 哪怕键名错也不会炸 EsConfig @Value）
        if (!environment.containsProperty("elasticsearch.url")) {
            overrides.put("elasticsearch.url", "localhost");
        }
        if (!environment.containsProperty("elasticsearch.port")) {
            overrides.put("elasticsearch.port", "9200");
        }
        // 3) 兜底端口 / 应用名
        if (!environment.containsProperty("server.port")) {
            overrides.put("server.port", "9091");
        }

        // 加在属性源列表最前面 -> 优先级最高，直接覆盖 Nacos bootstrapProperties
        MapPropertySource overrideSource = new MapPropertySource(PROPERTY_SOURCE_NAME, overrides);
        environment.getPropertySources().addFirst(overrideSource);
    }
}
