package com.duckchess.duck_chess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DuckChessApplication {

	public static void main(String[] args) {
		SpringApplication.run(DuckChessApplication.class, args);
	}

}
