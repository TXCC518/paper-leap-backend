package com.may.paperleap;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@MapperScan("com.may.paperleap.mapper")
@EnableScheduling
public class PaperLeapApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaperLeapApplication.class, args);
    }

}
