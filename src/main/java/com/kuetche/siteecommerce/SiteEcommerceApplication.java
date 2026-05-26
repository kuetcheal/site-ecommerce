package com.kuetche.siteecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SiteEcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SiteEcommerceApplication.class, args);
    }
}