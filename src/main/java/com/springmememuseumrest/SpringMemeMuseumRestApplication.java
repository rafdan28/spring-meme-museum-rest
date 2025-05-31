package com.springmememuseumrest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class SpringMemeMuseumRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringMemeMuseumRestApplication.class, args);
	}

}
