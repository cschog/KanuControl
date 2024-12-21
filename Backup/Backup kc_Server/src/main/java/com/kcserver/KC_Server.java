package com.kcserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KC_Server {

	public static void main(String[] args) {
		SpringApplication.run(KC_Server.class, args);
		System.out.println("KC Server is running...");
	}
}