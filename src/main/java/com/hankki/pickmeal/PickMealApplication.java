package com.hankki.pickmeal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration; // 이제 필요 없으니 지워도 됩니다.
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@MapperScan("com.hankki.pickmeal.mapper")
public class PickMealApplication {
	public static void main(String[] args) {
		SpringApplication.run(PickMealApplication.class, args);
	}
}