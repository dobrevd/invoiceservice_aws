package com.dobrev.invoicesservice.config;

import com.dobrev.invoicesservice.interceptor.InvoicesInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class InterceptorsConfig implements WebMvcConfigurer {
    private final InvoicesInterceptor invoicesInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.invoicesInterceptor).addPathPatterns("/api/invoices/**");
    }
}