package com.bookhub.hyper_reserve;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class HyperReserveApplication {
    public static void main(String[] args) {
        SpringApplication.run(HyperReserveApplication.class, args);
    }
}
