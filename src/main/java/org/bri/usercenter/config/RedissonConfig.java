package org.bri.usercenter.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedissonConfig {
    private String host;
    private int port;
    private String password;

    @Bean
    public RedissonClient redisson() {
        log.info("redis://%s:%d, password=%s".formatted(host, port, password));
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://%s:%d".formatted(host, port))
                .setDatabase(2)
                .setPassword(password);
        // Sync and Async API
        return Redisson.create(config);
    }
}
