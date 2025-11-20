package com.example.mg;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.example.mg.mapper")
@SpringBootApplication
public class MgApplication {

    public static void main(String[] args) {
        SpringApplication.run(MgApplication.class, args);
        System.out.println("☺木瓜编程项目启动了~");
    }

}
