package cn.wolfcode.wolf2w.business.env;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * travel-destination 本地兜底：强制写入数据源 4 件套（url/username/password/driver），
 * 避免 Nacos 下发内容缩进错 / 为空 / 被 shared 合并后缺字段时起不来。
 * 规则：只在当前 application.name=travel-destination 时生效，写在属性源最前面以压过 Nacos bootstrapProperties。
 */
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class DestinationLocalOverrideEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "travelDestinationLocalOverrides";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String appName = environment.getProperty("spring.application.name");
        if (StringUtils.hasText(appName) && !"travel-destination".equalsIgnoreCase(appName.trim())) {
            return;
        }

        Map<String, Object> overrides = new LinkedHashMap<>(10);
        // 统一用 Spring Boot 自带的 HikariCP，不依赖 Druid（即便将来 travel-common-datasource 改也不怕）
        overrides.put("spring.datasource.type", "com.zaxxer.hikari.HikariDataSource");
        overrides.put("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver");
        overrides.put("spring.datasource.url",
                "jdbc:mysql://127.0.0.1:3306/wolf2w-cloud?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8&useSSL=false&allowPublicKeyRetrieval=true");
        overrides.put("spring.datasource.username", "root");
        overrides.put("spring.datasource.password", "002004");

        // Hikari 参数兜底
        overrides.put("spring.datasource.hikari.minimum-idle", "10");
        overrides.put("spring.datasource.hikari.maximum-pool-size", "60");
        overrides.put("spring.datasource.hikari.idle-timeout", "60000");
        overrides.put("spring.datasource.hikari.connection-timeout", "30000");
        overrides.put("spring.datasource.hikari.connection-test-query", "SELECT 1");

        // Redis 兜底（共享配置失效时仍能连上本地）
        if (!StringUtils.hasText(environment.getProperty("spring.redis.host"))) {
            overrides.put("spring.redis.host", "localhost");
            overrides.put("spring.redis.port", "6379");
            overrides.put("spring.redis.password", "123456");
        }

        // 端口兜底
        if (!environment.containsProperty("server.port")) {
            overrides.put("server.port", "8088");
        }

        MapPropertySource overrideSource = new MapPropertySource(PROPERTY_SOURCE_NAME, overrides);
        environment.getPropertySources().addFirst(overrideSource);
    }
}
