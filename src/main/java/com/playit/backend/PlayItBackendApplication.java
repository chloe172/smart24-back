package com.playit.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class PlayItBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(PlayItBackendApplication.class, args);
	}

	@GetMapping("/")
	public String index() {
		return "Incroyable";
	}

	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
		return String.format("Salut %s!", name);
	}
}