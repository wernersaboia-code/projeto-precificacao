package com.projeto.precificacao.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@Profile("dev")  // ⚠️ ADICIONE ESTA LINHA - Só ativa em desenvolvimento
public class NgrokConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                String ngrokToken = request.getHeader("ngrok-skip-browser-warning");
                if (ngrokToken != null) {
                    response.addHeader("ngrok-skip-browser-warning", "true");
                }
                return true;
            }
        });
    }
}
