package com.musclegrow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class VoucherSeckillRedisConfiguration {

    @Bean
    public DefaultRedisScript<Long> voucherSeckillRedisScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/voucher-seckill.lua"));
        script.setResultType(Long.class);
        return script;
    }
}
