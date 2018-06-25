package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {
    @Bean
    public FilterRegistrationBean jwtFilter() {
        FilterRegistrationBean rbean = new FilterRegistrationBean();
        rbean.setFilter(new JwtFilter());
        rbean.addUrlPatterns("/user/*");
        return rbean;

    }
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
