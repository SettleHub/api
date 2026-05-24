package org.settlehub.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import java.net.InetSocketAddress;

@Configuration
public class RateLimiterConfig {

    protected static final String UNKNOWN_IP_STRING_RESULT = "unknown-ip";

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            
            String clientIp = (remoteAddress != null && remoteAddress.getAddress() != null) 
                    ? remoteAddress.getAddress().getHostAddress() 
                    : UNKNOWN_IP_STRING_RESULT;
                    
            return Mono.just(clientIp);
        };
    }
}
