package com.example.slackpoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SlackPocApplication {

	public static void main(String[] args) {
		SpringApplication.run(SlackPocApplication.class, args);
	}

}
