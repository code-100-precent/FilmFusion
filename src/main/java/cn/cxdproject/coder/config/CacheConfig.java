package cn.cxdproject.coder.config;

import cn.cxdproject.coder.model.entity.Article;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {


    @Bean
    public Cache<String, Object> cache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(25, TimeUnit.HOURS)
                .build();
    }



}
