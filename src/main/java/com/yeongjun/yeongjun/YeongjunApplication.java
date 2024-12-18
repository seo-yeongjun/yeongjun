package com.yeongjun.yeongjun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class YeongjunApplication {

    public static void main(String[] args) {
        SpringApplication.run(YeongjunApplication.class, args);
    }

}
