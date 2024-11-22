package com.kcserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@Component
@EnableScheduling
@SpringBootApplication
public class KC_Server {

	public static void main(String[] args) {
		SpringApplication.run(KC_Server.class, args);
		System.out.println("KC Server is running...");
	}
}