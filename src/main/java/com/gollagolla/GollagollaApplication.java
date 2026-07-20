package com.gollagolla;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class GollagollaApplication {

    public static void main(String[] args) {
        SpringApplication.run(GollagollaApplication.class, args);
    }

}
