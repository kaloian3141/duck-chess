package com.duckchess.duck_chess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class DuckChessApplication {

	public static void main(String[] args) {
		SpringApplication.run(DuckChessApplication.class, args);
	}

}
