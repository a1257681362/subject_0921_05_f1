package com.evops.config;

import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShiroConfig {
    @Bean
    public Realm localRealm() {
        SimpleAccountRealm realm = new SimpleAccountRealm();
        realm.addAccount("bootstrap", "bootstrap", "system");
        return realm;
    }

    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chain = new DefaultShiroFilterChainDefinition();
        chain.addPathDefinition("/api/health", "anon");
        // F1 业务接口暂不接入登录鉴权，由 X-Operator 请求头记录操作人
        chain.addPathDefinition("/api/lens/**", "anon");
        chain.addPathDefinition("/api/orders/**", "anon");
        chain.addPathDefinition("/error", "anon");
        chain.addPathDefinition("/**", "authc");
        return chain;
    }
}
