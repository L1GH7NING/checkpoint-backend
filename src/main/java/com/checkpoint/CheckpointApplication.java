package com.checkpoint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class CheckpointApplication {

	public static void main(String[] args) {
		SpringApplication.run(CheckpointApplication.class, args);
	}

}
