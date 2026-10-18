package com.stride;

import org.mybatis.spring.annotation.MapperScan; // 导入 MapperScan
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.stride.mapper") // 告诉 Spring 去哪里找 Mapper 接口
public class StrideApplication {

	public static void main(String[] args) {
		SpringApplication.run(StrideApplication.class, args);
	}

}