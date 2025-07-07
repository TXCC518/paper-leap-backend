package com.may.paperleap;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.may.paperleap.mapper")
public class PaperLeapApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaperLeapApplication.class, args);
    }

}
