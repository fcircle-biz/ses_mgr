package com.ses_mgr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SesMgrApplication {

    public static void main(String[] args) {
        SpringApplication.run(SesMgrApplication.class, args);
    }
}