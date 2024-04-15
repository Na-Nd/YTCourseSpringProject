package com.example.sweater.config;

import com.example.sweater.util.RedirectInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Value("${upload.path}")
    private String uploadPath;

    @Bean
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login"); // Таким образом, когда запрос приходит на "/login", Spring MVC будет искать представление с именем "login" и возвращать его клиенту
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/img/**")// Ображение к серверу по поти /img/** (** - любой путь)
                .addResourceLocations("file:" + uploadPath + "/"); // Будет перенаправлять все запросы по пути...
        registry.addResourceHandler("/static/**") // При обращении к этому пути
                .addResourceLocations("classpath:/static/"); // Ресурсы будут искаться в дереве проекта
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RedirectInterceptor());
    }
}
